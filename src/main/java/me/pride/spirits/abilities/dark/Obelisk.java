package me.pride.spirits.abilities.dark;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import me.pride.spirits.Spirits;
import me.pride.spirits.api.ability.DarkSpiritAbility;
import me.pride.spirits.util.Filter;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class Obelisk extends DarkSpiritAbility implements AddonAbility {
	private final String path = Tools.path(this, Path.ABILITIES);

	public enum ObeliskState {
		FOUND_SOURCE, SEARCHING, RELEASED;
	}
	private ObeliskState state;
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.SELECT_RANGE)
	private double selectRange;
	@Attribute(Attribute.SELECT_RANGE)
	private double findRange;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.KNOCKBACK)
	private double knockback;

	private double radius;
	private double spikeDecrease;
	
	private Block target;
	private Location origin, location;
	private Vector direction;
	
	public Obelisk(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			return;
		} else if (RegionProtection.isRegionProtected(player, player.getLocation())) {
			return;
		}
		this.cooldown = Spirits.instance.getConfig().getLong(path + "Cooldown");
		this.selectRange = Spirits.instance.getConfig().getDouble(path + "SelectRange");
		this.findRange = Spirits.instance.getConfig().getDouble(path + "FindTargetRange");
		this.range = Spirits.instance.getConfig().getDouble(path + "ObeliskRange");
		this.speed = Spirits.instance.getConfig().getDouble(path + "Speed");
		this.damage = Spirits.instance.getConfig().getDouble(path + "Damage");
		this.knockback = Spirits.instance.getConfig().getDouble(path + "Knockback");
		
		this.target = Tools.rayTraceBlock(player, selectRange);
		if (this.target == null) {
			return;
		} else if (RegionProtection.isRegionProtected(player, player.getLocation(), this)) {
			return;
		}
		if (!target.hasMetadata("spirits:corrupted_blocks")) {
			return;
		}
		this.state = ObeliskState.FOUND_SOURCE;

		this.origin = this.target.getLocation().clone().add(0.5, 0.5, 0.5);
		this.location = this.origin.clone();

		this.radius = 3;
		this.spikeDecrease = (3 - 1.25) / (range / speed);
		
		start();
	}
	
	@Override
	public void progress() {
		switch (state) {
			case FOUND_SOURCE -> {
				if (player.getLocation().distanceSquared(origin) > selectRange * selectRange) {
					remove();
					return;
				}
				break;
			}
			case SEARCHING -> {
				if (!player.isSneaking()) {
					direction = GeneralMethods.getDirection(origin, target()).normalize();
					state = ObeliskState.RELEASED;
				}
				break;
			}
			case RELEASED -> {
				if (location.distanceSquared(origin) > range * range) {
					bPlayer.addCooldown(this);
					remove();
					return;
				} else if (RegionProtection.isRegionProtected(player, location)) {
					bPlayer.addCooldown(this);
					remove();
					return;
				}
				location.add(direction.multiply(speed));

				if (ThreadLocalRandom.current().nextInt(3) == 0) {
					location.getWorld().playSound(location, Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 1, 0.5F);
				}
				obelisk(location, radius, block -> {
					if (!Filter.filterIndestructible(block) && !RegionProtection.isRegionProtected(player, block.getLocation(), this)) {
						if (GeneralMethods.isSolid(block)) {
							player.getWorld().spawnParticle(Particle.BLOCK_CRACK, block.getLocation(), 3, 0.125, 0.125, 0.125, 0, block.getBlockData());
						}
						new TempBlock(block, Material.OBSIDIAN.createBlockData(), 10000, this);
					}
				});
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 1.5)) {
					if (entity instanceof LivingEntity && Filter.filterGeneralEntity(entity, player, this)) {
						DamageHandler.damageEntity(entity, damage, this);
						entity.setVelocity(direction.multiply(knockback));
					}
				}
				radius -= spikeDecrease;
				break;
			}
		}
	}

	public Location target() {
		for (double i = 0; i < findRange; i += 0.5) {
			Location location = GeneralMethods.getTargetedLocation(player, i, getTransparentMaterials());
			if (location == null) continue;
			
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 1.25)) {
				if (entity instanceof LivingEntity && entity.getUniqueId() != player.getUniqueId()) {
					return entity.getLocation().clone().add(0, 1, 0);
				}
			}
		}
		return GeneralMethods.getTargetedLocation(player, findRange);
	}
	
	private void obelisk(Location location, double radius, Consumer<Block> shape) {
		for (Block block : GeneralMethods.getBlocksAroundPoint(location, radius)) {
			if (RegionProtection.isRegionProtected(this, location)) continue;
			
			shape.accept(block);
		}
	}
	public ObeliskState state() {
		return state;
	}
	public static ObeliskState state(Player player) {
		return getAbility(player, Obelisk.class).state();
	}
	public boolean foundSource() {
		return this.state == ObeliskState.FOUND_SOURCE;
	}
	public static boolean foundSource(Player player) {
		return getAbility(player, Obelisk.class).foundSource();
	}
	public void startSearching() {
		this.state = ObeliskState.SEARCHING;
	}
	public static void startSearching(Player player) {
		getAbility(player, Obelisk.class).startSearching();
	}
	
	public boolean searching() {
		return this.state == ObeliskState.SEARCHING;
	}
	public static boolean searching(Player player) {
		return getAbility(player, Obelisk.class).searching();
	}
	public boolean released() {
		return state == ObeliskState.RELEASED;
	}
	public static boolean released(Player player) {
		return getAbility(player, Obelisk.class).released();
	}

	@Override
	public boolean isEnabled() {
		return Spirits.instance.getConfig().getBoolean("Dark.Abilities.Obelisk.Enabled", true);
	}
	
	@Override
	public boolean isSneakAbility() {
		return true;
	}
	
	@Override
	public boolean isHarmlessAbility() {
		return false;
	}
	
	@Override
	public long getCooldown() {
		return cooldown;
	}
	
	@Override
	public String getName() {
		return "Obelisk";
	}
	
	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public String getAuthor() {
		return Spirits.getAuthor(this.getElement());
	}

	@Override
	public String getVersion() {
		return Spirits.getVersion();
	}
	
	@Override
	public void load() { }
	
	@Override
	public void stop() { }
}

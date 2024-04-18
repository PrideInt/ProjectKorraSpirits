package me.pride.spirits.abilities.dark;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import me.pride.spirits.Spirits;
import me.pride.spirits.api.ability.DarkSpiritAbility;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class Obelisk extends DarkSpiritAbility implements AddonAbility {
	private final String path = Tools.path(this, Path.ABILITIES);
	
	private int STATE = 0;
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.SELECT_RANGE)
	private double select_range;
	@Attribute(Attribute.SELECT_RANGE)
	private double find_range;
	@Attribute(Attribute.RANGE)
	private double range;
	
	private Block target;
	private Location origin, location, destination;
	
	public Obelisk(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			return;
		} else if (RegionProtection.isRegionProtected(player, player.getLocation())) {
			return;
		}
		cooldown = Spirits.instance.getConfig().getLong(path + "Cooldown");
		select_range = Spirits.instance.getConfig().getDouble(path + "SelectRange");
		find_range = Spirits.instance.getConfig().getDouble(path + "FindTargetRange");
		range = Spirits.instance.getConfig().getDouble(path + "ObeliskRange");
		
		RayTraceResult result = player.getWorld().rayTraceBlocks(player.getLocation().clone(), player.getLocation().getDirection(), select_range, FluidCollisionMode.ALWAYS);
		if (result == null) return;
		
		target = result.getHitBlock();
		if (target == null) {
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, target.getLocation())) {
			return;
		}
		if (!target.hasMetadata("spirits:corrupted_blocks")) {
			return;
		}
		origin = target.getLocation().clone().add(0.5, 0.5, 0.5);
		location = origin.clone();
		
		start();
	}
	
	@Override
	public void progress() {
		switch (STATE) {
			case 1 -> {
				if (player.getLocation().distanceSquared(origin) > select_range * select_range) {
					remove();
					return;
				}
				break;
			}
			case 2 -> {
				destination = target().clone();
				if (!player.isSneaking()) {
					STATE = 3;
				}
				break;
			}
			case 3 -> {
				if (location.distanceSquared(origin) > range * range) {
					remove();
					return;
				}
				location.add(GeneralMethods.getDirection(origin, destination).multiply(1));
				if (ThreadLocalRandom.current().nextInt(20) == 0) {
					// play sound
				}
				obelisk(location, block -> {
					if (!isTransparent(block)) {
						player.getWorld().playSound(block.getLocation(), Sound.BLOCK_STONE_BREAK, 1, 1);
						player.getWorld().spawnParticle(Particle.BLOCK_CRACK, block.getLocation(), 3, 0.125, 0.125, 0.125, 0, block.getBlockData());
					}
					new TempBlock(block, Material.OBSIDIAN.createBlockData());
				});
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 1.5)) {
					if (entity instanceof LivingEntity && entity.getUniqueId() != player.getUniqueId()) {
						DamageHandler.damageEntity(entity, 3, this);
						GeneralMethods.setVelocity(this, entity, location.getDirection().multiply(1));
					}
				}
				break;
			}
		}
	}
	
	public Location target() {
		for (double i = 0; i < find_range; i += 0.5) {
			Location location = GeneralMethods.getTargetedLocation(player, i, getTransparentMaterials());
			if (location == null) continue;
			
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 1.25)) {
				if (entity instanceof LivingEntity && entity.getUniqueId() != player.getUniqueId()) {
					return entity.getLocation().clone().add(0, 1, 0);
				}
			}
		}
		return GeneralMethods.getTargetedLocation(player, find_range);
	}
	
	private void obelisk(Location location, Consumer<Block> shape) {
		for (Block block : GeneralMethods.getBlocksAroundPoint(location, 1.5)) {
			if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) continue;
			
			shape.accept(block);
		}
	}
	public int state() { return STATE; }
	public static int state(Player player) { return getAbility(player, Obelisk.class).state(); }
	
	public void startSearching() { STATE = 2; }
	public static void startSearching(Player player) { getAbility(player, Obelisk.class).startSearching(); }
	
	public boolean foundSource() { return STATE == 1; }
	public static boolean foundSource(Player player) { return getAbility(player, Obelisk.class).foundSource(); }
	
	public boolean searching() { return STATE == 2; }
	public static boolean searching(Player player) { return getAbility(player, Obelisk.class).searching(); }
	
	public boolean released() { return STATE == 3; }
	public static boolean released(Player player) { return getAbility(player, Obelisk.class).released(); }
	
	@Override
	public boolean isSneakAbility() {
		return true;
	}
	
	@Override
	public boolean isHarmlessAbility() {
		return false;
	}
	
	@Override
	public boolean isIgniteAbility() { return false; }
	
	@Override
	public boolean isExplosiveAbility() { return false; }
	
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
		return null;
	}
	
	@Override
	public void load() { }
	
	@Override
	public void stop() { }
	
	@Override
	public boolean isEnabled() {
		return Spirits.instance.getConfig().getBoolean("Dark.Abilities.Obelisk.Enabled", true);
	}
	
	@Override
	public String getAuthor() {
		return Spirits.getAuthor(this.getElement());
	}
	
	@Override
	public String getVersion() {
		return Spirits.getVersion();
	}
}

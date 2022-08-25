package me.pride.spirits.abilities.neutral.combos;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import me.pride.spirits.Spirits;
import me.pride.spirits.game.SpiritAbility;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.TempBlock;

public class Skyrocket extends SpiritAbility implements AddonAbility, ComboAbility {
	
	private final String path = Tools.path(this, Path.COMBOS);
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute("Launch")
	private double launch;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.RADIUS)
	private double radius;
	@Attribute("RevertTime")
	private long revert_time;
	
	private long delay;
	
	private Location origin;
	
	public Skyrocket(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		} else if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, player.getLocation())) {
			return;
		}
		if (!bPlayer.isOnCooldown("Soar")) return;
		
		cooldown = Spirits.instance.getConfig().getLong(path + "Cooldown");
		launch = Spirits.instance.getConfig().getDouble(path + "Launch");
		range = Spirits.instance.getConfig().getDouble(path + "Range");
		radius = Spirits.instance.getConfig().getDouble(path + "SlamRadius");
		revert_time = Spirits.instance.getConfig().getLong(path + "RevertTime");
		
		delay = System.currentTimeMillis() + 2000;
		origin = player.getLocation().clone();
		player.setVelocity(player.getEyeLocation().getDirection().multiply(launch));
		
		for (int i = 0; i < 360; i += 9) {
			Vector circle = GeneralMethods.getOrthogonalVector(player.getEyeLocation().getDirection(), i, 3.0);
			player.getWorld().spawnParticle(Particle.CLOUD, origin.clone().add(GeneralMethods.getOrthogonalVector(player.getEyeLocation().getDirection(), i, 0.2)), 0, circle.getX(), circle.getY(), circle.getZ(), 0.10);
		}
		player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 2F, 0.2F);
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.3F, 1F);
		
		start();
		bPlayer.addCooldown(this);
	}
	
	@Override
	public void progress() {
		progressLaunch();
		
		if (System.currentTimeMillis() > delay) {
			if (slamCheck(player.getLocation())) {
				for (Block block : GeneralMethods.getBlocksAroundPoint(player.getLocation(), radius)) {
					if (isAir(block.getType()) || block.isLiquid() || GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
						continue;
					}
					player.getWorld().spawnParticle(Particle.BLOCK_CRACK, block.getLocation(), 3, 0.25, 0.25, 0.25, 0, block.getBlockData());
					
					ThreadLocalRandom random = ThreadLocalRandom.current();
					double x = random.nextDouble() * 3.0;
					double y = random.nextDouble() * 3.0;
					double z = random.nextDouble() * 3.0;
					
					x = random.nextBoolean() ? x : -x;
					y = random.nextBoolean() ? y : -y;
					z = random.nextBoolean() ? z : -z;
					
					new TempBlock(block, Material.AIR.createBlockData(), revert_time);
					
					FallingBlock fallingBlock = player.getWorld().spawnFallingBlock(block.getLocation(), block.getBlockData());
					fallingBlock.setDropItem(false);
					fallingBlock.setVelocity(new Vector(x, y, z).multiply(0.3));
					fallingBlock.setMetadata("slam_blocks", new FixedMetadataValue(Spirits.instance, 0));
				}
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 1.5F, 0.1F);
				player.setVelocity(new Vector(0, 0, 0));
				remove();
				return;
			}
		}
	}
	
	private void progressLaunch() {
		player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().clone().add(0, 1, 0), 3, 0.25, 0.25, 0.25, 0);
		
		if (origin.distanceSquared(player.getLocation()) > range * range) {
			remove();
			return;
		}
	}
	
	private boolean slamCheck(Location location) {
		for (Block b : GeneralMethods.getBlocksAroundPoint(location, 1.35)) {
			if (isTransparent(b)) continue;
			
			if (b != null) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean isSneakAbility() {
		return false;
	}
	
	@Override
	public boolean isHarmlessAbility() {
		return false;
	}
	
	@Override
	public boolean isIgniteAbility() {
		return false;
	}
	
	@Override
	public boolean isExplosiveAbility() {
		return false;
	}
	
	@Override
	public long getCooldown() {
		return cooldown;
	}
	
	@Override
	public String getName() {
		return "Skyrocket";
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
		return Spirits.instance.getConfig().getBoolean(path + "Enabled", true);
	}
	
	@Override
	public String getAuthor() {
		return Spirits.getAuthor(this.getElement());
	}
	
	@Override
	public String getVersion() {
		return Spirits.getVersion(this.getElement());
	}
	
	@Override
	public String getDescription() {
		return Tools.getOppositeColor(this.getElement()) + Spirits.instance.getConfig().getString(path + "Description");
	}
	
	@Override
	public String getInstructions() {
		return ChatColor.GOLD + Spirits.instance.getConfig().getString(path + "Instructions");
	}
	
	@Override
	public Object createNewComboInstance(Player player) {
		return new Skyrocket(player);
	}
	
	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> info = new ArrayList<>();
		info.add(new AbilityInformation("Agility", ClickType.LEFT_CLICK));
		info.add(new AbilityInformation("Agility", ClickType.LEFT_CLICK));
		return info;
	}
	
}
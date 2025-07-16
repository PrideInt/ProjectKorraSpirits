package me.pride.spirits.abilities.spirit;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.ActionBar;
import me.pride.spirits.Spirits;
import me.pride.spirits.api.ability.SpiritAbility;
import me.pride.spirits.api.ability.SpiritElement;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class Rematerialize extends SpiritAbility implements AddonAbility {
	private final String path = Tools.path(this, Path.ABILITIES);

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.RADIUS)
	private int minRadius, radius, maxRadius;
	private int delay;

	private int time;
	private int delayInterval;
	private boolean teleported;

	private Location teleportLocation;

	public Rematerialize(Player player) {
		super(player);

		if (!bPlayer.canBend(this)) {
			return;
		}  else if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (CoreAbility.hasAbility(player, Rematerialize.class)) {
			return;
		} else if (RegionProtection.isRegionProtected(player, player.getLocation(), this)) {
			return;
		}
		this.cooldown = Spirits.instance.getConfig().getLong(path + "Cooldown");
		this.minRadius = Spirits.instance.getConfig().getInt(path + "MinSearchRadius");
		this.maxRadius = Spirits.instance.getConfig().getInt(path + "MaxSearchRadius");
		this.delay = Spirits.instance.getConfig().getInt(path + "Delay");

		this.radius = this.minRadius;
		this.teleportLocation = findTeleportLocation();

		if (this.teleportLocation == null) {
			return;
		}
		start();
	}
	
	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		} else if (!bPlayer.canBendIgnoreBinds(this) && !teleported) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		if (!teleported) {
			time = time > 10 ? 0 : time + 1;
			if (time == 0) {
				radius++;
			}
			ActionBar.sendActionBar(SpiritElement.SPIRIT.getColor() + "Searching for a safe location...: " + (int) radius, player);

			teleportLocation = findTeleportLocation();
		}
		if (!player.isSneaking()) {
			if (teleportLocation != null) {
				teleport();
			}
		}
	}

	private void teleport() {
		if (teleportLocation != null) {
			if (!teleported) {
				if (delay > 0) {
					PotionEffectType.DARKNESS.createEffect(delay, 1).apply(player);
					PotionEffectType.NIGHT_VISION.createEffect(delay, 1).apply(player);
					PotionEffectType.INVISIBILITY.createEffect(delay, 1).apply(player);
				}
				player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CONDUIT_ATTACK_TARGET, 1.35F, 2F);
			}
			teleported = true;

			// Disable bending when dematerializing and rematerializing
			bPlayer.blockChi();

			for (int i = 0; i < 2; i++) {
				player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation().clone().add(0, i, 0), 3, 0.5, 0.5, 0.5, 0.1);
			}
			if (delayInterval < delay) {
				player.teleport(teleportLocation);
				delayInterval++;

				ActionBar.sendActionBar(SpiritElement.SPIRIT.getColor() + "Rematerializing...", player);
			} else {
				player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CONDUIT_ATTACK_TARGET, 1.35F, 2F);

				// Re-enable bending
				bPlayer.unblockChi();

				bPlayer.addCooldown(this);
				remove();
				return;
			}
		} else {
			remove();
			return;
		}
	}

	private boolean isTeleportable(Block block) {
		Block[] blocks = { block, block.getRelative(BlockFace.UP), block.getRelative(BlockFace.UP, 2) };

		for (Block test : blocks) {
			if (RegionProtection.isRegionProtected(player, test.getLocation(), this)) {
				return false;
			} else if (GeneralMethods.isSolid(test)) {
				return false;
			}
		}
		return true;
	}

	private Location findTeleportLocation() {
		for (Block block : GeneralMethods.getBlocksAroundPoint(player.getLocation(), radius)) {
			if (isTeleportable(block)) {
				return block.getLocation().add(0.5, 0.5, 0.5);
			}
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		return Spirits.instance.getConfig().getBoolean("Spirit.Abilities.Rematerialize.Enabled", true);
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
		return "Rematerialize";
	}
	
	@Override
	public Location getLocation() {
		return null;
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

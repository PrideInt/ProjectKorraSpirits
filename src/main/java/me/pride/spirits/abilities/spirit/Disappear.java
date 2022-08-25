package me.pride.spirits.abilities.spirit;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.TempPotionEffect;
import me.pride.spirits.Spirits;
import me.pride.spirits.game.SpiritAbility;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Disappear extends SpiritAbility implements AddonAbility {
	private final String path = Tools.path(this, Path.ABILITIES);
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.SELECT_RANGE)
	private double select_range;
	@Attribute(Attribute.DURATION)
	private long duration;
	
	private Location target;
	private TempPotionEffect potionEffect;
	
	public Disappear(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			return;
		} else if (CoreAbility.hasAbility(player, Disappear.class)) {
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, player.getLocation())) {
			return;
		}
		cooldown = Spirits.instance.getConfig().getLong(path + "Cooldown");
		select_range = Spirits.instance.getConfig().getDouble(path + "SelectRange");
		duration = Spirits.instance.getConfig().getLong(path + "Duration");
		
		player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CONDUIT_AMBIENT, 1, 1);
		player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 4, 0.125, 0.3, 0.125, 0.01);
		
		start();
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		}
		new TempPotionEffect(player, new PotionEffect(PotionEffectType.SLOW_FALLING, 10, 1));
		new TempPotionEffect(player, new PotionEffect(PotionEffectType.INVISIBILITY, 10, 1));
		
		target = GeneralMethods.getTargetedLocation(player, select_range);
		
		if (!player.isSneaking()) {
			if (target != null) {
				if (GeneralMethods.isRegionProtectedFromBuild(this, target)) {
					remove();
					return;
				}
				player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CONDUIT_AMBIENT, 1, 1);
				player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 4, 0.125, 0.3, 0.125, 0.01);
				player.teleport(target);
			}
			remove();
			return;
		}
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
	public boolean isIgniteAbility() { return false; }
	
	@Override
	public boolean isExplosiveAbility() { return false; }
	
	@Override
	public long getCooldown() {
		return cooldown;
	}
	
	@Override
	public String getName() {
		return "Disappear";
	}
	
	@Override
	public Location getLocation() {
		return null;
	}
	
	@Override
	public void remove() {
		bPlayer.addCooldown(this);
		super.remove();
	}
	
	@Override
	public void load() { }
	
	@Override
	public void stop() { }
	
	@Override
	public String getAuthor() {
		return Spirits.getAuthor(this.getElement());
	}
	
	@Override
	public String getVersion() {
		return Spirits.getVersion(this.getElement());
	}
}

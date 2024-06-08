package me.pride.spirits.abilities.spirit;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.region.RegionProtection;
import me.pride.spirits.Spirits;
import me.pride.spirits.abilities.spirit.combos.Possess;
import me.pride.spirits.api.ability.SpiritAbility;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;

public class Disappear extends SpiritAbility implements AddonAbility {
	private final String path = Tools.path(this, Path.ABILITIES);
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.SELECT_RANGE)
	private double selectRange;
	@Attribute(Attribute.RADIUS)
	private double invisibilityRadius;
	@Attribute(Attribute.DURATION)
	private long duration;
	
	private Location target;
	private Set<Player> hiddenFromPlayers;
	
	public Disappear(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			return;
		} else if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (CoreAbility.hasAbility(player, Disappear.class) || CoreAbility.hasAbility(player, Possess.class)) {
			return;
		} else if (RegionProtection.isRegionProtected(this, player.getLocation())) {
			return;
		}
		this.cooldown = Spirits.instance.getConfig().getLong(path + "Cooldown");
		this.selectRange = Spirits.instance.getConfig().getDouble(path + "SelectRange");
		this.invisibilityRadius = Spirits.instance.getConfig().getDouble(path + "InvisibilityRadius");
		this.duration = Spirits.instance.getConfig().getLong(path + "Duration");

		this.hiddenFromPlayers = new HashSet<>();

		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), invisibilityRadius)) {
			if (entity instanceof Player && entity.getUniqueId() != player.getUniqueId()) {
				Player p = (Player) entity;

				p.hidePlayer(Spirits.instance, player);
				this.hiddenFromPlayers.add(p);
			}
		}
		player.getWorld().playSound(player.getLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1, 1);
		player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CONDUIT_AMBIENT, 1, 1);

		for (int i = 0; i < 3; i++) {
			player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().clone().add(0, i, 0), 5, 0.25, 0.25, 0.25, 1);
		}
		start();
	}
	
	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		} else if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		} else if (RegionProtection.isRegionProtected(player, player.getLocation(), this)) {
			remove();
			return;
		} else if (System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		PotionEffectType.LEVITATION.createEffect(10, 1).apply(player);
		
		target = GeneralMethods.getTargetedLocation(player, selectRange);
		
		if (!player.isSneaking()) {
			if (target != null) {
				if (RegionProtection.isRegionProtected(player, target, this)) {
					remove();
					return;
				}
				player.getWorld().playSound(player.getLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1, 1);
				player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CONDUIT_AMBIENT, 1, 1);
				for (int i = 0; i < 3; i++) {
					player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().clone().add(0, i, 0), 5, 0.25, 0.25, 0.25, 1);
				}
				player.teleport(target);
			}
			remove();
			return;
		}
	}

	@Override
	public boolean isEnabled() {
		return Spirits.instance.getConfig().getBoolean("Spirit.Abilities.Disappear.Enabled", true);
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
		return "Disappear";
	}
	
	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public void remove() {
		bPlayer.addCooldown(this);
		for (Player p : hiddenFromPlayers) {
			p.showPlayer(Spirits.instance, player);
		}
		super.remove();
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

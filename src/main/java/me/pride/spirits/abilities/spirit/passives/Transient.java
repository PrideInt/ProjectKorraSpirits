package me.pride.spirits.abilities.spirit.passives;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.util.ActionBar;
import me.pride.spirits.Spirits;
import me.pride.spirits.api.ability.SpiritAbility;
import me.pride.spirits.api.ability.SpiritElement;
import me.pride.spirits.util.GhostFactory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class Transient extends SpiritAbility implements AddonAbility, PassiveAbility {
	public Transient(Player player) {
		super(player);
	}
	
	@Override
	public void progress() {
	
	}

	public void sendTransientEffects() {
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1, 0.5F);
		ActionBar.sendActionBar(SpiritElement.SPIRIT.getSubColor() + "* Transience phased the damage away. *", player);
	}

	/**
	 * Sends the transient effects to the player and attacker.
	 *
	 * @param attacker
	 */
	@Deprecated
	public void sendTransientEffects(Player attacker) {
		sendTransientEffects();
		if (GhostFactory.isGhostEnabled()) {
			Spirits.instance.getGhostFactory().setGhostTime(player, attacker, 1000);
		}
	}

	public void sendTransientEffectsAround(double radius) {
		sendTransientEffects();
		Set<Player> viewers = new HashSet<>();
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), radius)) {
			if (entity.getType() != EntityType.PLAYER) {
				continue;
			}
			Player viewer = (Player) entity;
			viewers.add(viewer);
		}
		Spirits.instance.getGhostFactory().setGhostTime(player, viewers, 1000);
	}

	public static void sendTransience(Player player) {
		if (hasAbility(player, Transient.class)) {
			getAbility(player, Transient.class).sendTransientEffects();
		}
	}

	@Deprecated
	public static void sendTransience(Player player, Player attacker) {
		if (hasAbility(player, Transient.class)) {
			getAbility(player, Transient.class).sendTransientEffects(attacker);
		}
	}

	public static void sendTransience(Player player, double radius) {
		if (hasAbility(player, Transient.class)) {
			getAbility(player, Transient.class).sendTransientEffectsAround(radius);
		}
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
	public long getCooldown() {
		return 0;
	}
	
	@Override
	public String getName() {
		return "Transient";
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
	public String getDescription() {
		return SpiritElement.SPIRIT.getSubColor() + "Spirits experience transience within their physical bodies, allowing them to naturally phase through certain attacks at times. Additionally, they are immune to falling block damage, suffocation, cramming and drowning.";
	}
	
	@Override
	public boolean isInstantiable() {
		return true;
	}
	
	@Override
	public boolean isProgressable() {
		return true;
	}

	@Override
	public void load() { }

	@Override
	public void stop() { }
}

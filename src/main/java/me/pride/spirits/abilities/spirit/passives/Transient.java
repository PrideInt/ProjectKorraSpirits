package me.pride.spirits.abilities.spirit.passives;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import me.pride.spirits.Spirits;
import me.pride.spirits.api.ability.SpiritAbility;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Transient extends SpiritAbility implements AddonAbility, PassiveAbility {
	public Transient(Player player) {
		super(player);
	}
	
	@Override
	public void progress() {
	
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
	public void load() { }
	
	@Override
	public void stop() { }
	
	@Override
	public String getAuthor() {
		return Spirits.getAuthor(this.getElement());
	}
	
	@Override
	public String getVersion() {
		return Spirits.getVersion();
	}
	
	@Override
	public boolean isInstantiable() {
		return false;
	}
	
	@Override
	public boolean isProgressable() {
		return false;
	}
}

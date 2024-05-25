package me.pride.spirits.abilities.light.passives;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import me.pride.spirits.Spirits;
import me.pride.spirits.api.ability.LightSpiritAbility;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Lightborn extends LightSpiritAbility implements AddonAbility, PassiveAbility {
	public static final Map<UUID, Double> LIGHTS = new HashMap<>();

	public Lightborn(Player player) {
		super(player);

		LIGHTS.put(player.getUniqueId(), 0.0);

		start();
	}
	
	@Override
	public void progress() {
		if (LIGHTS.get(player.getUniqueId()) < 100.0) {
			LIGHTS.put(player.getUniqueId(), LIGHTS.get(player.getUniqueId()) + 0.1);
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
		return "Lightborn";
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
		return true;
	}
	
	@Override
	public boolean isProgressable() {
		return true;
	}
}

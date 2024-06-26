package me.pride.spirits.abilities.spirit;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.MultiAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import me.pride.spirits.api.ability.SpiritAbility;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Summon extends SpiritAbility implements AddonAbility, MultiAbility {
	public Summon(Player player) {
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
	public long getCooldown() {
		return 0;
	}

	@Override
	public String getName() {
		return "Summon";
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getAuthor() {
		return null;
	}

	@Override
	public String getVersion() {
		return null;
	}

	@Override
	public void load() { }

	@Override
	public void stop() { }

	@Override
	public ArrayList<MultiAbilityManager.MultiAbilityInfoSub> getMultiAbilities() {
		return null;
	}
}

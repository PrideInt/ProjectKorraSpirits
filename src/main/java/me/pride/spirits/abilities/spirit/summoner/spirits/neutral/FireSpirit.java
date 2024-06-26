package me.pride.spirits.abilities.spirit.summoner.spirits.neutral;

import com.projectkorra.projectkorra.Element;
import me.pride.spirits.abilities.spirit.summoner.SummonedSpirit;
import me.pride.spirits.api.SpiritType;
import me.pride.spirits.api.ability.SpiritElement;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

public class FireSpirit extends SummonedSpirit {
	public FireSpirit(World world, Location location, String name, EntityType entityType, SpiritType spiritType, long revertTime) {
		super(world, location, name, entityType, spiritType, revertTime);
	}

	@Override
	public boolean progress() {
		return true;
	}

	public static String getName(Element element) {
		if (element.equals(SpiritElement.SPIRIT)) {
			return "Firestarter";
		} else if (element.equals(SpiritElement.LIGHT_SPIRIT)) {
			return "Serapha";
		} else if (element.equals(SpiritElement.DARK_SPIRIT)) {
			return "Ifrit";
		}
		return "EarthSpirit";
	}
}

package me.pride.spirits.abilities.spirit.summoner.spirits.neutral;

import com.projectkorra.projectkorra.Element;
import me.pride.spirits.abilities.spirit.summoner.SummonedSpirit;
import me.pride.spirits.api.SpiritType;
import me.pride.spirits.api.ability.SpiritElement;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

public class FireSpirit extends SummonedSpirit {
	public FireSpirit() {
		super(null, null, null, null, null, 0);
	}
	public FireSpirit(World world, Location location, String name, EntityType entityType, SpiritType spiritType, long revertTime) {
		super(world, location, name, entityType, spiritType, revertTime);
	}

	@Override
	public boolean progress() {
		return true;
	}
	@Override
	public SpiritType defaultSpiritType() {
		return SpiritType.SPIRIT;
	}
	@Override
	public EntityType defaultEntityType() {
		return EntityType.STRIDER;
	}
	@Override
	public EntityType defaultLightEntityType() {
		return EntityType.FOX;
	}
	@Override
	public EntityType defaultDarkEntityType() {
		return EntityType.BLAZE;
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

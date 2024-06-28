package me.pride.spirits.abilities.spirit.summoner.spirits.neutral;

import com.projectkorra.projectkorra.Element;
import me.pride.spirits.abilities.spirit.summoner.SummonedSpirit;
import me.pride.spirits.api.SpiritType;
import me.pride.spirits.api.ability.SpiritElement;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

public class WaterSpirit extends SummonedSpirit {
	public WaterSpirit() {
		super(null, null, null, null, null, 0);
	}
	public WaterSpirit(World world, Location location, String name, EntityType entityType, SpiritType spiritType, long revertTime) {
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
		return EntityType.DOLPHIN;
	}
	@Override
	public EntityType defaultLightEntityType() {
		return EntityType.HORSE;
	}
	@Override
	public EntityType defaultDarkEntityType() {
		return EntityType.DROWNED;
	}

	public static String getName(Element element) {
		if (element.equals(SpiritElement.SPIRIT)) {
			return "Suio";
		} else if(element.equals(SpiritElement.LIGHT_SPIRIT)) {
			return "Huanghema";
		} else if (element.equals(SpiritElement.DARK_SPIRIT)) {
			return "Maqua";
		}
		return "WaterSpirit";
	}
}

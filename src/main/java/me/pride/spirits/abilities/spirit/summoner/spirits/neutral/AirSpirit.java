package me.pride.spirits.abilities.spirit.summoner.spirits.neutral;

import com.projectkorra.projectkorra.Element;
import me.pride.spirits.abilities.spirit.summoner.SummonedSpirit;
import me.pride.spirits.api.SpiritType;
import me.pride.spirits.api.ability.SpiritElement;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

public class AirSpirit extends SummonedSpirit {
	public AirSpirit() {
		super(null, null, null, null, null, 0);
	}
	public AirSpirit(World world, Location location, String name, EntityType entityType, SpiritType spiritType, long revertTime) {
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
		return EntityType.BEE;
	}
	@Override
	public EntityType defaultLightEntityType() {
		return EntityType.ALLAY;
	}
	@Override
	public EntityType defaultDarkEntityType() {
		return EntityType.VEX;
	}
	@Override
	public String getSpiritName(SpiritType type) {
		return SpiritElement.SPIRIT.getColor() + "" + ChatColor.BOLD + getName(type);
	}

	public static String getName(SpiritType type) {
		if (type == SpiritType.SPIRIT) {
			return "Tianniu";
		} else if (type == SpiritType.LIGHT) {
			return "Sidhe";
		} else if (type == SpiritType.DARK) {
			return "Miasma";
		}
		return "AirSpirit";
	}
	public static String getName(Element element) {
		if (element.equals(SpiritElement.SPIRIT)) {
			return "Tianniu";
		} else if (element.equals(SpiritElement.LIGHT_SPIRIT)) {
			return "Sidhe";
		} else if (element.equals(SpiritElement.DARK_SPIRIT)) {
			return "Miasma";
		}
		return "AirSpirit";
	}
}

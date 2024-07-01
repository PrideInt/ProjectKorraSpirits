package me.pride.spirits.abilities.spirit.summoner.spirits.dark;

import me.pride.spirits.abilities.spirit.summoner.SummonedSpirit;
import me.pride.spirits.api.SpiritType;
import me.pride.spirits.api.ability.SpiritElement;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

public class TerrorSpirit extends SummonedSpirit {
	public TerrorSpirit() {
		super(null, null, null, null, null, 0);
	}
	public TerrorSpirit(World world, Location location, String name, EntityType entityType, SpiritType spiritType, long revertTime) {
		super(world, location, name, entityType, spiritType, revertTime);
	}

	@Override
	public boolean progress() {
		return true;
	}
	@Override
	public SpiritType defaultSpiritType() {
		return SpiritType.DARK;
	}
	@Override
	public EntityType defaultEntityType() {
		return EntityType.WITHER;
	}
	@Override
	public EntityType defaultLightEntityType() {
		return defaultEntityType();
	}
	@Override
	public EntityType defaultDarkEntityType() {
		return defaultEntityType();
	}
	@Override
	public String getSpiritName(SpiritType type) {
		return SpiritElement.DARK_SPIRIT.getColor() + "" + ChatColor.BOLD + getName();
	}

	public static String getName() {
		return "Darkness";
	}
}

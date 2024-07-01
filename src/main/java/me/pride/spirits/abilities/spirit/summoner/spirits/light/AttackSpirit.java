package me.pride.spirits.abilities.spirit.summoner.spirits.light;

import com.projectkorra.projectkorra.Element;
import me.pride.spirits.abilities.spirit.summoner.SummonedSpirit;
import me.pride.spirits.api.SpiritType;
import me.pride.spirits.api.ability.SpiritElement;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

public class AttackSpirit extends SummonedSpirit {
	public AttackSpirit() {
		super(null, null, null, null, null, 0);
	}
	public AttackSpirit(World world, Location location, String name, EntityType entityType, SpiritType spiritType, long revertTime) {
		super(world, location, name, entityType, spiritType, revertTime);
	}

	@Override
	public boolean progress() {
		return true;
	}
	@Override
	public SpiritType defaultSpiritType() {
		return SpiritType.LIGHT;
	}
	@Override
	public EntityType defaultEntityType() {
		return EntityType.PANDA;
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
		return SpiritElement.LIGHT_SPIRIT.getColor() + "" + ChatColor.BOLD + getName();
	}

	public static String getName() {
		return "Lightbringer";
	}
}

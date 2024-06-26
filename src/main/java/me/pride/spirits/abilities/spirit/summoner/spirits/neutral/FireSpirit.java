package me.pride.spirits.abilities.spirit.summoner.spirits.neutral;

import me.pride.spirits.abilities.spirit.summoner.SummonedSpirit;
import me.pride.spirits.api.SpiritType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

public class FireSpirit extends SummonedSpirit {
	public FireSpirit(World world, Location location, String name, EntityType entityType, SpiritType spiritType, long revertTime) {
		super(world, location, name, entityType, spiritType, revertTime);
	}
}

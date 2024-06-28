package me.pride.spirits.abilities.spirit.summoner.spirits.light;

import me.pride.spirits.abilities.spirit.summoner.SummonedSpirit;
import me.pride.spirits.api.SpiritType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

public class GuardSpirit extends SummonedSpirit {
	public GuardSpirit() {
		super(null, null, null, null, null, 0);
	}
	public GuardSpirit(World world, Location location, String name, EntityType entityType, SpiritType spiritType, long revertTime) {
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
		return EntityType.IRON_GOLEM;
	}
	@Override
	public EntityType defaultLightEntityType() {
		return defaultEntityType();
	}
	@Override
	public EntityType defaultDarkEntityType() {
		return defaultEntityType();
	}
}

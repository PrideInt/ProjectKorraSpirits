package me.pride.spirits.api;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class NeutralSpirit extends ReplaceableSpirit {
	public NeutralSpirit(World world, Location location, String name, EntityType entityType, long revertTime) {
		super(world, location, name, entityType, SpiritType.SPIRIT, revertTime);
	}
	public NeutralSpirit(World world, Entity entity, String name, EntityType entityType, long revertTime) {
		super(world, entity, name, entityType, SpiritType.SPIRIT, revertTime);
	}
	public NeutralSpirit(World world, Location location) {
		this(world, location, SpiritType.SPIRIT.name(), SpiritType.SPIRIT.entityType(), -1);
	}
	public NeutralSpirit(World world, Location location, EntityType entityType) {
		this(world, location, SpiritType.SPIRIT.name(), entityType, -1);
	}
	public NeutralSpirit(World world, Location location, long revertTime) {
		this(world, location, SpiritType.SPIRIT.name(), SpiritType.SPIRIT.entityType(), revertTime);
	}
	public NeutralSpirit(World world, Location location, String name, EntityType entityType) {
		this(world, location, name, entityType, -1);
	}
	public NeutralSpirit(World world, Location location, String name, long revertTime) {
		this(world, location, name, SpiritType.SPIRIT.entityType(), revertTime);
	}
}

package me.pride.spirits.api;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class DarkSpirit extends ReplaceableSpirit {
	public DarkSpirit(World world, Location location, String name, EntityType entityType, long revertTime) {
		super(world, location, name, entityType, SpiritType.DARK, revertTime);
	}
	public DarkSpirit(World world, Entity entity, String name, EntityType entityType, long revertTime) {
		super(world, entity, name, entityType, SpiritType.DARK, revertTime);
	}
	public DarkSpirit(World world, Location location) {
		this(world, location, SpiritType.DARK.name(), SpiritType.DARK.entityType(), -1);
	}
	public DarkSpirit(World world, Location location, EntityType entityType) {
		this(world, location, SpiritType.DARK.name(), entityType, -1);
	}
	public DarkSpirit(World world, Location location, long revertTime) {
		this(world, location, SpiritType.DARK.name(), SpiritType.DARK.entityType(), revertTime);
	}
	public DarkSpirit(World world, Location location, String name, EntityType entityType) {
		this(world, location, name, entityType, -1);
	}
	public DarkSpirit(World world, Location location, String name, long revertTime) {
		this(world, location, name, SpiritType.DARK.entityType(), revertTime);
	}
}

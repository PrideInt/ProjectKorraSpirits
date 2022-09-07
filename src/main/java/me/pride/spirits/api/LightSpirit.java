package me.pride.spirits.api;

import me.pride.spirits.api.event.EntitySpiritReplaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class LightSpirit extends Spirit {
	private String name;
	private EntityType entityType;
	private SpiritType spiritType;
	private long revertTime;
	
	public LightSpirit(World world, Location location, String name, EntityType entityType, long revertTime) {
		super(world, location);
		this.name = name;
		this.entityType = entityType;
		this.spiritType = SpiritType.LIGHT;
		this.revertTime = revertTime;
		super.spawnEntity();
	}
	public LightSpirit(World world, Entity entity, String name, EntityType entityType, long revertTime) {
		super(world, entity.getLocation());
		this.name = name;
		this.entityType = entityType;
		this.spiritType = SpiritType.LIGHT;
		this.revertTime = revertTime;
		super.replaceEntity(entity);
	}
	public LightSpirit(World world, Location location) {
		this(world, location, SpiritType.LIGHT.name(), SpiritType.LIGHT.entityType(), -1);
	}
	public LightSpirit(World world, Location location, EntityType entityType) {
		this(world, location, SpiritType.LIGHT.name(), entityType, -1);
	}
	public LightSpirit(World world, Location location, long revertTime) {
		this(world, location, SpiritType.LIGHT.name(), SpiritType.LIGHT.entityType(), revertTime);
	}
	public LightSpirit(World world, Location location, String name, EntityType entityType) {
		this(world, location, name, entityType, -1);
	}
	public LightSpirit(World world, Location location, String name, long revertTime) {
		this(world, location, name, SpiritType.LIGHT.entityType(), revertTime);
	}
	
	@Override
	public SpiritType type() { return this.spiritType; }
	@Override
	public EntityType entityType() { return this.entityType; }
	@Override
	public String spiritName() { return this.name; }
	@Override
	public long revertTime() { return this.revertTime; }
}

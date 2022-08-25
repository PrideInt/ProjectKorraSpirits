package me.pride.spirits.api;

import me.pride.spirits.api.event.EntitySpiritReplaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class LightSpirit extends Spirit implements Replaceable {
	private String name;
	private EntityType entityType;
	private SpiritType spiritType;
	private long revertTime;
	
	private EntityType replacedEntity;
	private SpiritType replacedSpiritType;
	
	public LightSpirit(World world, Location location, String name, EntityType entityType, long revertTime) {
		super(world, location);
		this.name = name;
		this.entityType = entityType;
		this.spiritType = SpiritType.LIGHT;
		this.revertTime = revertTime;
	}
	
	public LightSpirit(World world, Entity entity, String name, EntityType entityType, long revertTime) {
		super(world, entity);
		this.name = name;
		this.entityType = entityType;
		this.spiritType = SpiritType.LIGHT;
		this.revertTime = revertTime;
	}
	
	public LightSpirit(World world, Location location) {
		this(world, location, SpiritType.LIGHT.name(), SpiritType.LIGHT.entityType(), 0);
	}
	
	public LightSpirit(World world, Location location, EntityType entityType) {
		this(world, location, SpiritType.LIGHT.name(), entityType, 0);
	}
	
	public LightSpirit(World world, Location location, long revertTime) {
		this(world, location, SpiritType.LIGHT.name(), SpiritType.LIGHT.entityType(), revertTime);
	}
	
	public LightSpirit(World world, Location location, String name, EntityType entityType) {
		this(world, location, name, entityType, 0);
	}
	
	public LightSpirit(World world, Location location, String name, long revertTime) {
		this(world, location, name, SpiritType.LIGHT.entityType(), revertTime);
	}
	
	public void replaceWith(EntityType entityType, SpiritType spiritType) {
		replaceWithEntity(entityType);
		replaceWithSpirit(spiritType);
		
		String name = this.name;
		if (name.equalsIgnoreCase(SpiritType.LIGHT.name())) {
			name = spiritType.name();
		}
		long time = (startTime() + this.revertTime) - startTime();
		
		EntitySpiritReplaceEvent replaceEvent = new EntitySpiritReplaceEvent(this, new LightSpirit(this.world(), this.entity(), name, entityType, time));
		Bukkit.getServer().getPluginManager().callEvent(replaceEvent);
	}
	
	@Override
	public SpiritType type() { return this.spiritType; }
	@Override
	public EntityType entityType() { return this.entityType; }
	@Override
	public String spiritName() { return this.name; }
	@Override
	public long revertTime() { return this.revertTime; }
	
	@Override
	public void replaceWithEntity(EntityType entityType) {
		this.entityType = entityType; this.replacedEntity = entityType;
	}
	@Override
	public void replaceWithSpirit(SpiritType spiritType) {
		this.spiritType = spiritType; this.replacedSpiritType = spiritType;
	}
	@Override
	public EntityType replacedEntity() { return this.replacedEntity; }
	@Override
	public SpiritType replacedSpirit() { return this.replacedSpiritType; }
}

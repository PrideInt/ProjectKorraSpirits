package me.pride.spirits.api;

import me.pride.spirits.api.event.EntitySpiritReplaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class NeutralSpirit extends Spirit implements Replaceable {
	private String name;
	private EntityType entityType;
	private SpiritType spiritType;
	private long revertTime;
	
	private EntityType replacedEntity;
	private SpiritType replacedSpiritType;
	
	public NeutralSpirit(World world, Location location, String name, EntityType entityType, long revertTime) {
		super(world, location);
		this.name = name;
		this.entityType = entityType;
		this.spiritType = SpiritType.SPIRIT;
		this.revertTime = revertTime;
	}
	
	public NeutralSpirit(World world, Entity entity, String name, EntityType entityType, long revertTime) {
		super(world, entity);
		this.name = name;
		this.entityType = entityType;
		this.spiritType = SpiritType.SPIRIT;
		this.revertTime = revertTime;
	}
	
	public NeutralSpirit(World world, Location location) {
		this(world, location, SpiritType.SPIRIT.name(), SpiritType.SPIRIT.entityType(), 0);
	}
	
	public NeutralSpirit(World world, Location location, EntityType entityType) {
		this(world, location, SpiritType.SPIRIT.name(), entityType, 0);
	}
	
	public NeutralSpirit(World world, Location location, long revertTime) {
		this(world, location, SpiritType.SPIRIT.name(), SpiritType.SPIRIT.entityType(), revertTime);
	}
	
	public NeutralSpirit(World world, Location location, String name, EntityType entityType) {
		this(world, location, name, entityType, 0);
	}
	
	public NeutralSpirit(World world, Location location, String name, long revertTime) {
		this(world, location, name, SpiritType.SPIRIT.entityType(), revertTime);
	}
	
	public void replaceWith(EntityType entityType, SpiritType spiritType) {
		replaceWithEntity(entityType);
		replaceWithSpirit(spiritType);
		
		String name = this.name;
		if (name.equalsIgnoreCase(SpiritType.SPIRIT.name())) {
			name = spiritType.name();
		}
		long time = (startTime() + this.revertTime) - startTime();
		
		EntitySpiritReplaceEvent replaceEvent = new EntitySpiritReplaceEvent(this, new NeutralSpirit(this.world(), this.entity(), name, entityType, time));
		Bukkit.getServer().getPluginManager().callEvent(replaceEvent);
	}
	
	@Override
	public SpiritType type() { return SpiritType.SPIRIT; }
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

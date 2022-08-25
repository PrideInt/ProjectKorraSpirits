package me.pride.spirits.api;

import me.pride.spirits.api.event.EntitySpiritReplaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class DarkSpirit extends Spirit implements Replaceable {
	private String name;
	private EntityType entityType;
	private SpiritType spiritType;
	private long revertTime;
	
	private EntityType replacedEntity;
	private SpiritType replacedSpiritType;
	
	public DarkSpirit(World world, Location location, String name, EntityType entityType, long revertTime) {
		super(world, location);
		this.name = name;
		this.entityType = entityType;
		this.spiritType = SpiritType.DARK;
		this.revertTime = revertTime;
	}
	
	public DarkSpirit(World world, Entity entity, String name, EntityType entityType, long revertTime) {
		super(world, entity);
		this.name = name;
		this.entityType = entityType;
		this.spiritType = SpiritType.DARK;
		this.revertTime = revertTime;
	}
	
	public DarkSpirit(World world, Location location) {
		this(world, location, SpiritType.DARK.name(), SpiritType.DARK.entityType(), 0);
	}
	
	public DarkSpirit(World world, Location location, EntityType entityType) {
		this(world, location, SpiritType.DARK.name(), entityType, 0);
	}
	
	public DarkSpirit(World world, Location location, long revertTime) {
		this(world, location, SpiritType.DARK.name(), SpiritType.DARK.entityType(), revertTime);
	}
	
	public DarkSpirit(World world, Location location, String name, EntityType entityType) {
		this(world, location, name, entityType, 0);
	}
	
	public DarkSpirit(World world, Location location, String name, long revertTime) {
		this(world, location, name, SpiritType.DARK.entityType(), revertTime);
	}
	
	public void replaceWith(EntityType entityType, SpiritType spiritType) {
		replaceWithEntity(entityType);
		replaceWithSpirit(spiritType);
		
		String name = this.name;
		if (name.equalsIgnoreCase(SpiritType.DARK.name())) {
			name = spiritType.name();
		}
		long time = (startTime() + this.revertTime) - startTime();
		
		EntitySpiritReplaceEvent replaceEvent = new EntitySpiritReplaceEvent(this, new DarkSpirit(this.world(), this.entity(), name, entityType, time));
		Bukkit.getServer().getPluginManager().callEvent(replaceEvent);
	}
	
	@Override
	public SpiritType type() { return SpiritType.DARK; }
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

package me.pride.spirits.api;

import me.pride.spirits.api.event.EntitySpiritReplaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class NeutralSpirit extends Spirit {
	private String name;
	private EntityType entityType;
	private SpiritType spiritType;
	private long revertTime;
	
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
	
	@Override
	public SpiritType type() { return SpiritType.SPIRIT; }
	@Override
	public EntityType entityType() { return this.entityType; }
	@Override
	public String spiritName() { return this.name; }
	@Override
	public long revertTime() { return this.revertTime; }
}

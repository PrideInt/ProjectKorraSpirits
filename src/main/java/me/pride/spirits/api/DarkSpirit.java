package me.pride.spirits.api;

import me.pride.spirits.api.event.EntitySpiritReplaceEvent;
import me.pride.spirits.api.record.SpiritRecord;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class DarkSpirit extends Spirit {
	private SpiritRecord record;
	
	public DarkSpirit(World world, Location location, String name, EntityType entityType, long revertTime) {
		super(world, location);
		this.record = new SpiritRecord(name, entityType, SpiritType.DARK, revertTime);
		super.spawnEntity();
	}
	public DarkSpirit(World world, Entity entity, String name, EntityType entityType, long revertTime) {
		super(world, entity.getLocation());
		if (isReplacedEntity(entity)) {
			super.removeFromCache();
			return;
		}
		this.record = new SpiritRecord(name, entityType, SpiritType.DARK, revertTime);
		super.replaceEntity(entity);
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
	
	@Override
	public SpiritType type() { return SpiritType.DARK; }
	@Override
	public EntityType entityType() { return this.record.entityType(); }
	@Override
	public String spiritName() { return this.record.spiritName(); }
	@Override
	public long revertTime() { return this.record.revertTime(); }
}

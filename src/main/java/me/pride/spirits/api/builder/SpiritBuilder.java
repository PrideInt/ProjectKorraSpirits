package me.pride.spirits.api.builder;

import me.pride.spirits.api.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.function.Supplier;

public class SpiritBuilder {
	private SpiritType spiritType;
	private World world;
	private Location location;
	private String name;
	private EntityType entityType;
	private long revertTime;
	
	private boolean replace;
	private Entity replacedEntity;
	private EntityType replaceWithEntity;
	private SpiritType replaceWithSpirit;
	
	public SpiritBuilder(SpiritType spiritType) {
		this.spiritType = spiritType;
		this.name = spiritType.spiritName();
		this.entityType = spiritType.entityType();
		this.revertTime = -1;
	}
	public SpiritBuilder(SpiritType spiritType, World world, Location location) {
		this(spiritType);
		this.world = world;
		this.location = location;
	}
	public static SpiritBuilder builder(SpiritType spiritType) {
		return new SpiritBuilder(spiritType);
	}
	public static SpiritBuilder light() {
		return new SpiritBuilder(SpiritType.LIGHT);
	}
	public static SpiritBuilder dark() {
		return new SpiritBuilder(SpiritType.DARK);
	}
	public static SpiritBuilder spirit() {
		return new SpiritBuilder(SpiritType.SPIRIT);
	}
	public SpiritBuilder spawn(World world, Location location) {
		this.world = world; this.location = location;
		return this;
	}
	public SpiritBuilder world(World world) {
		this.world = world;
		return this;
	}
	public SpiritBuilder location(Location location) {
		this.location = location;
		return this;
	}
	public SpiritBuilder spiritName(String name) {
		this.name = name;
		return this;
	}
	public SpiritBuilder entityType(EntityType entityType) {
		this.entityType = entityType;
		return this;
	}
	public SpiritBuilder revertTime(long revertTime) {
		this.revertTime = revertTime;
		return this;
	}
	public SpiritBuilder replace(Entity replacedEntity) {
		this.replace = true;
		this.replacedEntity = replacedEntity;
		return this;
	}
	public <T extends Spirit> T build() {
		switch (spiritType) {
			case LIGHT -> {
				return (T) (replace ? new LightSpirit(world, replacedEntity, name, entityType, revertTime) : new LightSpirit(world, location, name, entityType, revertTime));
			}
			case DARK -> {
				return (T) (replace ? new DarkSpirit(world, replacedEntity, name, entityType, revertTime) : new DarkSpirit(world, location, name, entityType, revertTime));
			}
			case SPIRIT -> {
				return (T) (replace ? new NeutralSpirit(world, replacedEntity, name, entityType, revertTime) : new NeutralSpirit(world, location, name, entityType, revertTime));
			}
		}
		return null;
	}
	public <T extends Spirit> T build(Supplier<T> supplier) {
		return (T) (replace ? supplier.get().replaceEntity(world, replacedEntity, entityType, spiritType, revertTime, e -> {
					e.setCustomName(name);
					e.setCustomNameVisible(true);
				}) : supplier.get().spawnEntity(world, location, entityType, spiritType, revertTime, e -> {}));
	}
}

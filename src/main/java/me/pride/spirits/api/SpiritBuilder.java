package me.pride.spirits.api;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.function.Consumer;
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
	public SpiritBuilder replace(boolean replace) {
		this.replace = replace;
		return this;
	}
	public Spirit build() {
		switch (this.spiritType) {
			case LIGHT -> {
				return replace ? new LightSpirit(world, location, name, entityType, revertTime) : new DarkSpirit(world, replacedEntity, name, entityType, revertTime);
			}
			case DARK -> {
				return replace ? new DarkSpirit(world, location, name, entityType, revertTime) : new DarkSpirit(world, replacedEntity, name, entityType, revertTime);
			}
			case SPIRIT -> {
				return replace ? new NeutralSpirit(world, location, name, entityType, revertTime) : new NeutralSpirit(world, replacedEntity, name, entityType, revertTime);
			}
		}
		return null;
	}
	public <T extends Spirit> void build(Supplier<T> supplier) {
		supplier.get().override(spiritType, entityType, name, revertTime);
	}
}

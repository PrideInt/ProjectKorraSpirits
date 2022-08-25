package me.pride.spirits.api;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.function.Supplier;

public class SpiritBuilder {
	private SpiritType spiritType;
	private World world;
	private Location location;
	private String name;
	private EntityType entityType;
	private long revertTime;
	private boolean canReplace;
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
	public SpiritBuilder(SpiritType spiritType, String name, EntityType entityType, long revertTime) {
		this.spiritType = spiritType;
		this.name = name;
		this.entityType = entityType;
		this.revertTime = revertTime;
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
	public SpiritBuilder canReplace(boolean canReplace) {
		this.canReplace = canReplace;
		return this;
	}
	public SpiritBuilder replaceWithEntity(EntityType replaceWithEntity) {
		this.replaceWithEntity = replaceWithEntity;
		return this;
	}
	public SpiritBuilder replaceWithSpirit(SpiritType replaceWithSpirit) {
		this.replaceWithSpirit = replaceWithSpirit;
		return this;
	}
	public Spirit build() {
		switch (this.spiritType) {
			case LIGHT -> {
				LightSpirit lightSpirit = new LightSpirit(this.world, this.location, this.name, this.entityType, this.revertTime);
				if (this.canReplace) {
					lightSpirit.replaceWith(this.replaceWithEntity, this.replaceWithSpirit);
				}
				return lightSpirit;
			}
			case DARK -> {
				DarkSpirit darkSpirit = new DarkSpirit(this.world, this.location, this.name, this.entityType, this.revertTime);
				if (this.canReplace) {
					darkSpirit.replaceWith(this.replaceWithEntity, this.replaceWithSpirit);
				}
				return darkSpirit;
			}
			case SPIRIT -> {
				NeutralSpirit neutralSpirit = new NeutralSpirit(this.world, this.location, this.name, this.entityType, this.revertTime);
				if (this.canReplace) {
					neutralSpirit.replaceWith(this.replaceWithEntity, this.replaceWithSpirit);
				}
				return neutralSpirit;
			}
		}
		return new Spirit(this.world, this.location) {
			@Override
			public SpiritType type() { return spiritType; }
			@Override
			public EntityType entityType() { return entityType; }
			@Override
			public String spiritName() { return name; }
			@Override
			public long revertTime() { return revertTime; }
		};
	}
	public <T extends Spirit> void build(T spirit) {
	}
}

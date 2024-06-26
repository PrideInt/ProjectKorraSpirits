package me.pride.spirits.abilities.spirit.summoner;

import me.pride.spirits.api.Spirit;
import me.pride.spirits.api.SpiritType;
import me.pride.spirits.api.record.SpiritRecord;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.HashSet;
import java.util.Set;

public abstract class SummonedSpirit extends Spirit {
	public static final Set<SummonedSpirit> SUMMONED_SPIRITS = new HashSet<>();

	private SpiritRecord record;

	public SummonedSpirit(World world, Location location, String name, EntityType entityType, SpiritType spiritType, long revertTime) {
		super(world, location);

		this.record = new SpiritRecord(name, entityType, spiritType, revertTime);
	}

	public SummonedSpirit(World world, Location location, String name, EntityType entityType, SpiritType spiritType) {
		this(world, location, name, entityType, spiritType, -1);
	}

	public abstract boolean progress();

	public String name() {
		return record.name();
	}

	public EntityType entityType() {
		return record.entityType();
	}

	public SpiritType spiritType() {
		return record.spiritType();
	}

	public long revertTime() {
		return record.revertTime();
	}

	@Override
	public SpiritRecord record() {
		return record;
	}

	public static void handle() {
		SUMMONED_SPIRITS.removeIf(spirit -> !spirit.progress());
	}
}

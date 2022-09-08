package me.pride.spirits.api.record;

import me.pride.spirits.api.SpiritType;
import org.bukkit.entity.EntityType;

public record SpiritRecord(String name, EntityType entityType, SpiritType spiritType, long revertTime) {
	public String spiritName() { return this.name; }
	public EntityType entityType() { return this.entityType; }
	public SpiritType type() { return this.spiritType; }
	public long revertTime() { return this.revertTime; }
}

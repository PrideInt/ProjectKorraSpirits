package me.pride.spirits.api;

import org.bukkit.entity.EntityType;

public interface Replaceable {
	void replaceWithEntity(EntityType entityType);
	void replaceWithSpirit(SpiritType spiritType);
	EntityType replacedEntity();
	SpiritType replacedSpirit();
}

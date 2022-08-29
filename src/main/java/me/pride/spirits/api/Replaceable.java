package me.pride.spirits.api;

import me.pride.spirits.api.event.EntitySpiritReplaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;

import java.util.function.Supplier;

public interface Replaceable {
	void replaceWithEntity(EntityType entityType);
	void replaceWithSpirit(SpiritType spiritType);
	EntityType replacedEntity();
	SpiritType replacedSpirit();
}

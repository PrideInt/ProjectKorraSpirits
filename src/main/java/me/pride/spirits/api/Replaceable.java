package me.pride.spirits.api;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.entity.Entity;

import java.util.Optional;

public interface Replaceable {
	record ReplacedCache(boolean replaced, boolean invulnerable, Pair<Entity, Integer> cache) {
		public boolean replaced() { return this.replaced; }
		public boolean invulnerable() { return this.invulnerable; }
		public Pair<Entity, Integer> cache() { return this.cache; }

		public Entity getReplaced() { return cache.getLeft(); }

		public int getReplacedID() { return cache.getRight(); }
	}
	
	ReplaceableSpirit replaceEntity(Entity entity);
	Optional<ReplacedCache> replacedCache();
}

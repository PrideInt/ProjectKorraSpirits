package me.pride.spirits.api;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;

public enum SpiritType {
	LIGHT(Particle.ELECTRIC_SPARK, EntityType.SHEEP, Pair.of("lightspirit", Spirit.LIGHT_SPIRIT_KEY), Spirit.LIGHT_SPIRIT_NAME),
	DARK(Particle.SPELL_WITCH, EntityType.SPIDER, Pair.of("darkspirit", Spirit.DARK_SPIRIT_KEY), Spirit.DARK_SPIRIT_NAME),
	SPIRIT(Particle.CRIT_MAGIC, EntityType.COW, Pair.of("spirit", Spirit.SPIRIT_KEY), Spirit.SPIRIT_NAME);
	
	private Particle particle;
	private EntityType entityType;
	private Pair<String, NamespacedKey> keys;
	private String name;
	
	SpiritType(Particle particle, EntityType entityType, Pair<String, NamespacedKey> keys, String name) {
		this.particle = particle;
		this.entityType = entityType;
		this.name = name;
		this.keys = keys;
	}
	
	public Particle particles() {
		return this.particle;
	}
	public EntityType entityType() {
		return this.entityType;
	}
	public String spiritName() {
		return this.name;
	}
	public Pair<String, NamespacedKey> keys() {
		return this.keys;
	}
}

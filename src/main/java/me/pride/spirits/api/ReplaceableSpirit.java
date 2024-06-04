package me.pride.spirits.api;

import me.pride.spirits.Spirits;
import me.pride.spirits.api.event.EntityReplacedBySpiritEvent;
import me.pride.spirits.api.event.EntitySpiritReplaceEvent;
import me.pride.spirits.api.record.SpiritRecord;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ReplaceableSpirit extends Spirit implements Replaceable {
	protected static final Map<Entity, ReplaceableSpirit> REPLACED = new HashMap<>();
	
	private SpiritRecord record;

	// ReplacedDefinitions contains information on
	// * Entity that was replaced: use getReplaced()
	// * Entity's UUID: use getReplacedID()
	// * Whether the entity was replaced: use replaced()
	// * Whether the entity was invulnerable: use invulnerable()
	private Optional<ReplacedDefinitions> definitions;
	
	public ReplaceableSpirit(World world, Location location, String name, EntityType entityType, SpiritType spiritType, long revertTime) {
		super(world, location);
		this.record = new SpiritRecord(name, entityType, spiritType, revertTime);
		this.definitions = Optional.empty();
		super.spawnEntity(world, location);
	}
	
	public ReplaceableSpirit(World world, Entity entity, String name, EntityType entityType, SpiritType spiritType, long revertTime) {
		super(world, entity.getLocation());

		Entity e = entity;

		// If the entity is a replaced entity, reverse it, replace it
		if (isReplacedEntity(entity)) {
			e = fromEntity(entity).entityInRecord().get();
			ReplaceableSpirit.reverse(Spirit.of(entity).get());
		}
		this.record = new SpiritRecord(name, entityType, spiritType, revertTime);
		
		replaceEntity(world, e);
	}
	
	/**
	 *
	 * @param entity - Entity to be replaced
	 *
	 * @return this Spirit
	 */
	@Override
	public ReplaceableSpirit replaceEntity(Entity entity) {
		replaceEntity(world(), entity);
		return this;
	}
	
	/**
	 * @param world - World to spawn spirit that replaces the old spirit
	 * @param entity - Entity to be replaced
	 *
	 * @return this Spirit
	 */
	public ReplaceableSpirit replaceEntity(World world, Entity entity) {
		replaceEntity(world, entity, entityType(), type(), revertTime(), e -> {
			e.setCustomName(spiritName());
			e.setCustomNameVisible(true);
		});
		return this;
	}
	
	/**
	 * @param world - World to spawn spirit that replaces the old spirit
	 * @param entity - Entity to be replaced
	 * @param entityType - Type of entity to spawn
	 * @param spiritType - type of the Spirit
	 * @param revertTime - Sets revert time of spirit; -1 does not revert
	 * @param consumer - Returns spawned entity, allows developers to perform whatever action to alter entity
	 *
	 * @return this Spirit
	 */
	public ReplaceableSpirit replaceEntity(World world, Entity entity, EntityType entityType, SpiritType spiritType, long revertTime, Consumer<Entity> consumer) {
		REPLACED.put(spawnEntity(world, entity.getLocation(), entityType, spiritType, revertTime, consumer).entity(), this);
		
		this.definitions = Optional.of(new ReplacedDefinitions(true, entity.isInvulnerable(), Pair.of(entity, entity.getEntityId())));
		
		entity.setInvulnerable(true);
		entity.getPersistentDataContainer().set(REPLACED_KEY, PersistentDataType.STRING, "replacedentity");

		// hide original Entity
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			player.hideEntity(Spirits.instance, entity);
		}
		Event event = new EntityReplacedBySpiritEvent(entity, this);
		
		Optional<Spirit> spiritOf = of(entity);
		if (spiritOf.isPresent()) {
			event = new EntitySpiritReplaceEvent(spiritOf.get(), this);
		}
		Bukkit.getServer().getPluginManager().callEvent(event);
		return this;
	}

	/** Any hidden spirits or entities that are stored in the cache will be unhidden from players' client
	 */
	protected static void reverse(Spirit spirit) {
		if (spirit instanceof ReplaceableSpirit) {
			if (ReplaceableSpirit.containsKey(spirit.entity())) {
				ReplaceableSpirit.fromEntity(spirit.entity()).definitions().ifPresent(definitions -> {
					Entity replaced = definitions.getReplaced();

					for (Player player : Bukkit.getServer().getOnlinePlayers()) {
						player.showEntity(Spirits.instance, replaced);
					}
					replaced.teleport(spirit.entity().getLocation());
					// replaced.setInvulnerable(definitions.invulnerable());
					// replaced.setCustomName(replaced.getCustomName());
					// replaced.setCustomNameVisible(true);
					// replaced.getPersistentDataContainer().remove(REPLACED_KEY);
				});
				ReplaceableSpirit.remove(spirit.entity(), ReplaceableSpirit.fromEntity(spirit.entity()));
			}
		}
	}
	
	public static boolean isReplacedEntity(Entity entity) {
		return entity.getPersistentDataContainer().has(REPLACED_KEY, PersistentDataType.STRING);
	}
	public static ReplaceableSpirit fromEntity(Entity entity) {
		return REPLACED.get(entity);
	}
	public static boolean containsKey(Entity entity) {
		return REPLACED.containsKey(entity);
	}
	public static boolean remove(Entity entity, ReplaceableSpirit spirit) {
		REPLACED.get(entity).getReplacedDefinitions().getReplaced().getPersistentDataContainer().remove(REPLACED_KEY);
		return REPLACED.remove(entity, spirit);
	}
	public void remove() {
		REPLACED.entrySet().removeIf(entry -> {
			if (entry.getValue().equals(this)) {
				entry.getValue().getReplacedDefinitions().getReplaced().getPersistentDataContainer().remove(REPLACED_KEY);
				return true;
			}
			return false;
		});
		super.remove();
	}
	
	@Override
	public Optional<ReplacedDefinitions> definitions() {
		return this.definitions;
	}
	@Override
	public SpiritRecord record() {
		return this.record;
	}
	public ReplacedDefinitions getReplacedDefinitions() {
		return definitions.get();
	}
	public void ifCachePresent(Consumer<ReplacedDefinitions> consumer) {
		definitions.ifPresent(consumer);
	}
	public Optional<Entity> entityInRecord() {
		if (definitions.isPresent()) {
			return Optional.of(definitions.get().getReplaced());
		}
		return Optional.empty();
	}
	public static Iterable<ReplaceableSpirit> replaced() {
		return REPLACED.values();
	}
}

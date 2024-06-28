package me.pride.spirits.api;

import me.pride.spirits.Spirits;
import me.pride.spirits.api.ability.SpiritElement;
import me.pride.spirits.api.event.EntitySpiritDestroyEvent;
import me.pride.spirits.api.event.EntitySpiritRevertEvent;
import me.pride.spirits.api.event.EntitySpiritSpawnEvent;
import me.pride.spirits.api.record.SpiritRecord;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataType;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class Spirit {
	/*
	TODO: ID assignment to Spirits for efficient searching
	 */
	public static final Map<UUID, Spirit> SPIRIT_CACHE = new ConcurrentHashMap<>();
	public static final Queue<Spirit> RECOLLECTION = new LinkedList<>();

	// private static int SPIRIT_ID = 0;

	/**
	 * Record object that contains information about the Spirit.
	 *
	 * For developers, you will need to implement a record that stores references
	 * to the Spirit's name, type, entity type, and revert time in any class that extends
	 * Spirit. See ReplaceableSpirit for an example.
	 */
	public abstract SpiritRecord record();

	// private Spirit spirit;
	private World world;
	private Location location;
	private Entity entity;
	private long start, end;

	// private int id;

	public Spirit() { }

	public Spirit(World world, Location location) {
		this.world = world;
		this.location = location.clone();
	}

	public Spirit(World world, Entity entity) {
		// this.spirit = this;
		this.world = world;
		this.entity = entity;
		// SPIRIT_CACHE.put(entity.getUniqueId(), this);
	}

	/**
	 * @return World that this spirit was spawned in
	 */
	public World world() {
		return this.world;
	}

	/**
	 * @return The entity spawned that represents this Spirit object
	 */
	public Entity entity() {
		return this.entity;
	}

	/**
	 * @return The id assigned to the Spirit
	 */
	/*
	public int id() {
		return this.id;
	}
	 */

	/**
	 * @return The current location of the spirit
	 */
	public Location location() {
		return this.entity.getLocation();
	}

	/**
	 * @return Custom name of the spirit
	 */
	public String spiritName() {
		return record().spiritName();
	}

	/**
	 * @return Type of entity representing the spirit
	 */
	public EntityType entityType() {
		return record().entityType();
	}

	/**
	 * @return Type of the spirit
	 */
	public SpiritType type() {
		return record().spiritType();
	}

	/**
	 * @return Revert time of the spirit
	 */
	public long revertTime() {
		return record().revertTime();
	}

	/**
	 * @return System time when this spirit was created
	 */
	public long startTime() {
		return this.start;
	}

	/**
	 * @return Applicable only to spirits that revert; system time when this spirit will revert
	 */
	public long endTime() {
		return this.end;
	}

	/**
	 * @param time - provided system time
	 * @return Applicable only to spirits that revert; time that will be left before this spirit reverts
	 */
	public long timeLeft(long time) {
		return (this.start + time) - this.start;
	}

	/**
	 * @return Applicable only to spirits that revert; true if the current system time reaches the time in which this spirit will revert
	 */
	public boolean timesUp() {
		return System.currentTimeMillis() > this.end;
	}

	/**
	 * Removes this spirit from the cache
	 */
	public void removeFromCache() {
		RECOLLECTION.remove(this);
		SPIRIT_CACHE.remove(this.entity.getUniqueId());
	}

	/**
	 * Removes this spirit from the cache, as well as the entity representing the spirit
	 */
	public void remove() {
		removeFromCache();
		this.entity.remove();
		Spirit spirit = this; spirit = null;
	}

	/**
	 * Spawns an entity with world and location provided beforehand prior to calling this method
	 * @return this Spirit
	 */
	public Spirit spawnEntity() {
		spawnEntity(world(), location);
		return this;
	}

	/**
	 * @param world - World to spawn spirit
	 * @param location - Location to spawn spirit
	 *
	 * @return this Spirit
	 */
	public Spirit spawnEntity(World world, Location location) {
		spawnEntity(world, location, entityType(), type(), revertTime(), e -> {
			e.setCustomName(spiritName());
			e.setCustomNameVisible(true);
		});
		return this;
	}

	/**
	 * @param world - World to spawn spirit
	 * @param location - Location to spawn spirit
	 * @param consumer - Returns spawned entity, allows developers to perform whatever action to alter entity
	 *
	 * @return this Spirit
	 */
	public Spirit spawnEntity(World world, Location location, Consumer<Entity> consumer) {
		spawnEntity(world, location, entityType(), type(), revertTime(), consumer);
		return this;
	}

	/**
	 * @param world - World to spawn spirit
	 * @param location - Location to spawn spirit
	 * @param entityType - Type of entity to spawn
	 * @param spiritType - type of the Spirit
	 * @param revertTime - Sets revert time of spirit; -1 does not revert
	 * @param consumer - Returns spawned entity, allows developers to perform whatever action to alter entity
	 *
	 * @return this Spirit
	 */
	public Spirit spawnEntity(World world, Location location, EntityType entityType, SpiritType spiritType, long revertTime, Consumer<Entity> consumer) {
		if (this.entity != null) {
			if (SPIRIT_CACHE.containsKey(this.entity.getUniqueId())) {
				SPIRIT_CACHE.put(this.entity.getUniqueId(), this);
			}
			return this;
		}
		this.entity = world.spawnEntity(location, entityType);
		this.start = System.currentTimeMillis();
		this.end = System.currentTimeMillis() + revertTime;

		consumer.andThen(e -> e.getPersistentDataContainer().set(spiritType.keys().getRight(), PersistentDataType.STRING, spiritType.keys().getLeft() + "-" + this.entity.getUniqueId())).accept(this.entity);

		EntitySpiritSpawnEvent spawnEvent = new EntitySpiritSpawnEvent(this);
		Bukkit.getServer().getPluginManager().callEvent(spawnEvent);

		SPIRIT_CACHE.put(this.entity.getUniqueId(), this);

		if (revertTime >= 0) RECOLLECTION.add(this);
		return this;
	}

	/**
	 * @param entity - Entity mapped to find spirit associated with the entity
	 * @return an Optional of the Spirit that is found, if any
	 */
	public static Optional<Spirit> of(Entity entity) {
		return SPIRIT_CACHE.get(entity.getUniqueId()) == null ? Optional.empty() : Optional.of(SPIRIT_CACHE.get(entity.getUniqueId()));
	}
	public static boolean exists(Entity entity) {
		if (of(entity).isPresent()) {
			return true;
		}
		return entity.getPersistentDataContainer().has(LIGHT_SPIRIT_KEY, PersistentDataType.STRING) || entity.getPersistentDataContainer().has(DARK_SPIRIT_KEY, PersistentDataType.STRING) || entity.getPersistentDataContainer().has(SPIRIT_KEY, PersistentDataType.STRING);
	}
	public static boolean destroy(Spirit spirit) {
		if (spirit == null) return false;

		ReplaceableSpirit.reverse(spirit);
		Bukkit.getServer().getPluginManager().callEvent(new EntitySpiritDestroyEvent(spirit));
		spirit.entity().remove();
		return (!RECOLLECTION.contains(spirit) ? true : RECOLLECTION.remove(spirit)) && SPIRIT_CACHE.remove(spirit.entity().getUniqueId(), spirit);
	}
	public static void handle() {
		// System.out.println(RECOLLECTION.size() + ", " + SPIRIT_CACHE.keySet().size() + ", " + SPIRIT_CACHE.values().size());
		if (!RECOLLECTION.isEmpty()) {
			Spirit spirit = RECOLLECTION.peek();

			if (spirit != null) {
				if (spirit.timesUp()) {
					Bukkit.getServer().getPluginManager().callEvent(new EntitySpiritDestroyEvent(spirit));
					Bukkit.getServer().getPluginManager().callEvent(new EntitySpiritRevertEvent(spirit, System.currentTimeMillis()));

					spirit.entity().remove();
					RECOLLECTION.poll();
				}
			}
			RECOLLECTION.removeIf(s -> {
				boolean condition = !s.entity().isDead() && !s.entity().isValid();
				if (condition) {
					Bukkit.getServer().getPluginManager().callEvent(new EntitySpiritDestroyEvent(s));

					ReplaceableSpirit.reverse(s);
					s.entity().remove();
				}
				return condition;
			});
		}
	}
	public static void cleanup() {
		SPIRIT_CACHE.values().forEach(spirit -> {
			ReplaceableSpirit.reverse(spirit);
			spirit.remove();
		});
		SPIRIT_CACHE.clear();
		RECOLLECTION.clear();
	}
	public static final NamespacedKey LIGHT_SPIRIT_KEY = new NamespacedKey(Spirits.instance, "lightspirit");
	public static final NamespacedKey DARK_SPIRIT_KEY = new NamespacedKey(Spirits.instance, "darkspirit");
	public static final NamespacedKey SPIRIT_KEY = new NamespacedKey(Spirits.instance, "spirit");
	public static final NamespacedKey REPLACED_KEY = new NamespacedKey(Spirits.instance, "replacedentity");
	public static final String LIGHT_SPIRIT_NAME = SpiritElement.LIGHT_SPIRIT.getColor() + "" + ChatColor.BOLD + "Light spirit";
	public static final String DARK_SPIRIT_NAME = SpiritElement.DARK_SPIRIT.getColor() + "" + ChatColor.BOLD + "Dark spirit";
	public static final String SPIRIT_NAME = SpiritElement.SPIRIT.getColor() + "" + ChatColor.BOLD + "Spirit";
	public static final String BLESSED_SOURCE = "spirits:blessed_source";
	public static final String BLESSED_ENTITY = "spirits:blessed_entity";
	public static final String CORRUPTED_SOURCE = "spirits:corrupted_source";
}

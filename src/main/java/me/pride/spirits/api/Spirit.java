package me.pride.spirits.api;

import me.pride.spirits.Spirits;
import me.pride.spirits.api.event.*;
import me.pride.spirits.game.SpiritElement;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.world.entity.EntityLiving;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.function.Consumer;

public abstract class Spirit {
	public static final NamespacedKey LIGHT_SPIRIT_KEY = new NamespacedKey(Spirits.instance, "lightspirit");
	public static final NamespacedKey DARK_SPIRIT_KEY = new NamespacedKey(Spirits.instance, "darkspirit");
	public static final NamespacedKey SPIRIT_KEY = new NamespacedKey(Spirits.instance, "spirit");
	public static final String LIGHT_SPIRIT_NAME = SpiritElement.LIGHT_SPIRIT.getColor() + "" + ChatColor.BOLD + "Light spirit";
	public static final String DARK_SPIRIT_NAME = SpiritElement.DARK_SPIRIT.getColor() + "" + ChatColor.BOLD + "Dark spirit";
	public static final String SPIRIT_NAME = SpiritElement.SPIRIT.getColor() + "" + ChatColor.BOLD + "Spirit";
	
	public static final Map<Spirit, Pair<Entity, Integer>> SPIRIT_CACHE = new HashMap<>();
	public static final Stack<Spirit> RECOLLECTION = new Stack<>();
	
	public abstract SpiritType type();
	public abstract EntityType entityType();
	public abstract String spiritName();
	public abstract long revertTime();
	
	private World world;
	private Location location;
	private Entity entity;
	private long start, end;
	
	public Spirit() { }
	public Spirit(World world, Location location) {
		this.world = world;
		this.location = location.clone();
	}
	
	public World world() {
		return this.world;
	}
	public Location location() {
		return this.location;
	}
	public Entity entity() {
		return this.entity;
	}
	public long startTime() {
		return this.start;
	}
	public long endTime() {
		return this.end;
	}
	public long timeLeft(long time) {
		return (this.start + time) - this.start;
	}
	public boolean timesUp() {
		return System.currentTimeMillis() > this.end;
	}
	
	public void spawnEntity() {
		spawnEntity(world(), location());
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
		this.entity = world.spawnEntity(location, entityType);
		this.start = System.currentTimeMillis();
		this.end = System.currentTimeMillis() + revertTime;
		
		consumer.andThen(e -> e.getPersistentDataContainer().set(spiritType.keys().getRight(), PersistentDataType.STRING, spiritType.keys().getLeft() + "-" + this.entity.getUniqueId())).accept(this.entity);
		
		EntitySpiritSpawnEvent spawnEvent = new EntitySpiritSpawnEvent(this);
		Bukkit.getServer().getPluginManager().callEvent(spawnEvent);
		
		if (revertTime >= 0) RECOLLECTION.add(this);
		return this;
	}
	
	/**
	 *
	 * @param entity - Entity to be replaced
	 *
	 * @return this Spirit
	 */
	public Spirit replaceEntity(Entity entity) {
		replaceEntity(world(), entity);
		return this;
	}
	
	/**
	 * @param world - World to spawn spirit that replaces the old spirit
	 * @param entity - Entity to be replaced
	 *
	 * @return this Spirit
	 */
	public Spirit replaceEntity(World world, Entity entity) {
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
	public Spirit replaceEntity(World world, Entity entity, EntityType entityType, SpiritType spiritType, long revertTime, Consumer<Entity> consumer) {
		spawnEntity(world, entity.getLocation(), entityType, spiritType, revertTime, consumer);
		
		SPIRIT_CACHE.put(this, Pair.of(entity, entity.getEntityId()));
		// Do packets stuff to hide and unhide original entity
		PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(entity.getEntityId());
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			((CraftPlayer) player).getHandle().b.a(packet);
		}
		Event event = new EntityReplacedBySpiritEvent(entity, this);
		
		Optional<Spirit> ofSpirit = of(entity);
		if (ofSpirit.isPresent()) {
			Spirit spirit = ofSpirit.get();
			event = new EntitySpiritReplaceEvent(spirit, this);
		}
		Bukkit.getServer().getPluginManager().callEvent(event);
		return this;
	}
	
	/* Any hidden spirits or entities that are stored in the cache will be unhidden from players' client
	 */
	private static void showEntity(Spirit spirit) {
		SPIRIT_CACHE.computeIfPresent(spirit, (k, v) -> {
			EntitySpiritDestroyEvent destroyEvent = new EntitySpiritDestroyEvent(k);
			Bukkit.getServer().getPluginManager().callEvent(destroyEvent);
			
			Entity original = SPIRIT_CACHE.get(k).getLeft();
			original.teleport(k.location());
			
			if (original instanceof LivingEntity) {
				((LivingEntity) original).getLocation().setPitch(k.location().getPitch());
				((LivingEntity) original).getLocation().setYaw(k.location().getYaw());
			}
			PacketPlayOutSpawnEntity packet = new PacketPlayOutSpawnEntity((net.minecraft.world.entity.Entity) original, SPIRIT_CACHE.get(k).getRight());
			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				((CraftPlayer) player).getHandle().b.a(packet);
			}
			return SPIRIT_CACHE.remove(k);
		});
	}
	
	/**
	 * @param entity - Entity mapped to find spirit associated with the entity
	 * @return an Optional of the Spirit that is found, if any
	 */
	public static Optional<Spirit> of(Entity entity) {
		return RECOLLECTION.stream().filter(s -> s.entity().getUniqueId() == entity.getUniqueId()).findAny();
	}
	public static boolean exists(Entity entity) {
		return of(entity).isPresent();
	}
	public static boolean destroy(Spirit spirit) {
		showEntity(spirit);
		Pair<Entity, Integer> cache = SPIRIT_CACHE.get(spirit);
		Bukkit.getServer().getPluginManager().callEvent(new EntitySpiritDestroyEvent(spirit));
		spirit.entity().remove();
		return RECOLLECTION.remove(spirit) && (SPIRIT_CACHE.get(spirit) == null ? true : SPIRIT_CACHE.remove(spirit, cache));
	}
	public static void handle() {
		if (!RECOLLECTION.isEmpty()) {
			Spirit spirit = RECOLLECTION.peek();
			
			if (spirit != null) {
				if (spirit.timesUp()) {
					Bukkit.getServer().getPluginManager().callEvent(new EntitySpiritRevertEvent(spirit, System.currentTimeMillis()));
					destroy(spirit);
					RECOLLECTION.pop();
				}
			}
			// TODO: concurrency issues
			RECOLLECTION.removeIf(s -> {
				boolean condition = s.entity().isDead() || !s.entity().isValid();
				if (condition) {
					showEntity(s);
				}
				destroy(s);
				
				return condition;
			});
		}
	}
	public static void cleanup() {
		SPIRIT_CACHE.keySet().forEach(spirit -> {
			showEntity(spirit);
		});
		SPIRIT_CACHE.clear();
		RECOLLECTION.clear();
	}
}

package me.pride.spirits.api;

import me.pride.spirits.Spirits;
import me.pride.spirits.api.event.*;
import me.pride.spirits.game.SpiritElement;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.world.entity.EntityLiving;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.function.Consumer;

public abstract class Spirit {
	public static final NamespacedKey LIGHT_SPIRIT_KEY = new NamespacedKey(Spirits.instance, "lightspirit");
	public static final NamespacedKey DARK_SPIRIT_KEY = new NamespacedKey(Spirits.instance, "darkspirit");
	public static final NamespacedKey SPIRIT_KEY = new NamespacedKey(Spirits.instance, "spirit");
	public static final NamespacedKey REPLACED_KEY = new NamespacedKey(Spirits.instance, "replacedentity");
	public static final String LIGHT_SPIRIT_NAME = SpiritElement.LIGHT_SPIRIT.getColor() + "" + ChatColor.BOLD + "Light spirit";
	public static final String DARK_SPIRIT_NAME = SpiritElement.DARK_SPIRIT.getColor() + "" + ChatColor.BOLD + "Dark spirit";
	public static final String SPIRIT_NAME = SpiritElement.SPIRIT.getColor() + "" + ChatColor.BOLD + "Spirit";
	
	public static final Map<Spirit, Pair<Entity, Integer>> SPIRIT_CACHE = new HashMap<>();
	public static final Queue<Spirit> RECOLLECTION = new LinkedList<>();
	
	public abstract SpiritType type();
	public abstract EntityType entityType();
	public abstract String spiritName();
	public abstract long revertTime();
	
	private World world;
	private Location location;
	private Entity entity;
	private long start, end;
	
	private boolean replaced;
	private boolean invulnerable;
	
	public Spirit() { SPIRIT_CACHE.put(this, null); }
	
	public Spirit(World world, Location location) {
		this.world = world;
		this.location = location.clone();
		SPIRIT_CACHE.put(this, null);
	}
	private boolean wasInvulnerable() { return this.invulnerable; }
	
	/**
	 * @return World that this spirit was spawned in
	 */
	public World world() {
		return this.world;
	}
	
	/**
	 * @return Location that this spirit was spawned at
	 */
	public Location location() {
		return this.location;
	}
	
	/**
	 * @return The entity spawned that represents this Spirit object
	 */
	public Entity entity() {
		return this.entity;
	}
	
	/**
	 * @return Whether an entity/spirit was replaced by this spirit
	 */
	public boolean replaced() {
		return this.replaced;
	}
	
	/**
	 * @return The current location of the spirit
	 */
	public Location currentLocation() {
		return this.entity.getLocation();
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
		SPIRIT_CACHE.remove(this);
	}
	
	/**
	 * Removes this spirit from the cache, as well as the entity representing the spirit
	 */
	public void remove() {
		removeFromCache();
		this.entity.remove();
	}
	
	/**
	 * Spawns an entity with world and location provided beforehand prior to calling this method
	 * @return this Spirit
	 */
	public Spirit spawnEntity() {
		spawnEntity(world(), location());
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
		
		this.replaced = true;
		this.invulnerable = entity.isInvulnerable();
		
		entity.setInvulnerable(true);
		entity.getPersistentDataContainer().set(REPLACED_KEY, PersistentDataType.STRING, "replacedentity");
		
		SPIRIT_CACHE.put(this, Pair.of(entity, entity.getEntityId()));
		// Do packets stuff to hide and unhide original entity
		PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(entity.getEntityId());
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			((CraftPlayer) player).getHandle().b.a(packet);
		}
		Event event = new EntityReplacedBySpiritEvent(entity, this);
		
		Optional<Spirit> spiritOf = of(entity);
		if (spiritOf.isPresent()) {
			event = new EntitySpiritReplaceEvent(spiritOf.get(), this);
		}
		Bukkit.getServer().getPluginManager().callEvent(event);
		return this;
	}
	
	/* Any hidden spirits or entities that are stored in the cache will be unhidden from players' client
	 */
	private static void showEntity(Spirit spirit) {
		SPIRIT_CACHE.computeIfPresent(spirit, (k, v) -> {
			// TODO: teleporting weird, show metadata
			if (v != null) {
				Entity original = v.getLeft();
				PacketPlayOutSpawnEntity packet = new PacketPlayOutSpawnEntity(((CraftEntity) original).getHandle(), SPIRIT_CACHE.get(k).getRight());
				
				for (Player player : Bukkit.getServer().getOnlinePlayers()) {
					((CraftPlayer) player).getHandle().b.a(packet);
				}
				original.teleport(k.currentLocation());
				original.setInvulnerable(k.wasInvulnerable());
				
				if (v.getLeft() instanceof LivingEntity) {
					original.getLocation().setPitch(k.location().getPitch());
					original.getLocation().setYaw(k.location().getYaw());
				}
				original.getPersistentDataContainer().remove(REPLACED_KEY);
			}
			return v;
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
		if (spirit == null) return false;
		
		showEntity(spirit);
		Pair<Entity, Integer> cache = SPIRIT_CACHE.get(spirit);
		Bukkit.getServer().getPluginManager().callEvent(new EntitySpiritDestroyEvent(spirit));
		spirit.entity().remove();
		return RECOLLECTION.remove(spirit) || (SPIRIT_CACHE.get(spirit) == null ? SPIRIT_CACHE.keySet().remove(spirit) : SPIRIT_CACHE.remove(spirit, cache));
	}
	public static void handle() {
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
					showEntity(s);
					s.entity().remove();
				}
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

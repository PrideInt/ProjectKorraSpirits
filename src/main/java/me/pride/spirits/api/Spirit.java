package me.pride.spirits.api;

import me.pride.spirits.Spirits;
import me.pride.spirits.api.event.*;
import me.pride.spirits.api.ability.SpiritElement;
import me.pride.spirits.api.record.SpiritRecord;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class Spirit {
	public static final Map<UUID, Spirit> SPIRIT_CACHE = new HashMap();
	public static final Queue<Spirit> RECOLLECTION = new LinkedList<>();
	
	public abstract SpiritRecord record();
	
	private Spirit spirit;
	private World world;
	private Location location;
	private Entity entity;
	private long start, end;
	
	public Spirit(World world, Location location) {
		this.world = world;
		this.location = location.clone();
		SPIRIT_CACHE.put(null, this);
	}
	
	public Spirit(World world, Entity entity) {
		this.spirit = this;
		this.world = world;
		this.entity = entity;
		SPIRIT_CACHE.put(entity.getUniqueId(), this);
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
		SPIRIT_CACHE.remove(this);
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
			return this;
		}
		this.entity = world.spawnEntity(location, entityType);
		this.start = System.currentTimeMillis();
		this.end = System.currentTimeMillis() + revertTime;
		
		SPIRIT_CACHE.replace(this.entity.getUniqueId(), this);
		
		consumer.andThen(e -> e.getPersistentDataContainer().set(spiritType.keys().getRight(), PersistentDataType.STRING, spiritType.keys().getLeft() + "-" + this.entity.getUniqueId())).accept(this.entity);
		
		EntitySpiritSpawnEvent spawnEvent = new EntitySpiritSpawnEvent(this);
		Bukkit.getServer().getPluginManager().callEvent(spawnEvent);
		
		if (revertTime >= 0) RECOLLECTION.add(this);
		return this;
	}
	
	/* Any hidden spirits or entities that are stored in the cache will be unhidden from players' client
	 */
	private static void showEntity(Spirit spirit) {
		if (spirit instanceof ReplaceableSpirit) {
			if (ReplaceableSpirit.containsKey(spirit.entity())) {
				ReplaceableSpirit.fromEntity(spirit.entity()).replacedCache().ifPresent(replacedCache -> {
					Entity replaced = replacedCache.cache().getLeft();
					
					net.minecraft.world.entity.Entity spawn = ((CraftEntity) replaced).getHandle();
					spawn.a(spirit.location().getX(), spirit.location().getY(), spirit.location().getZ());
					
					PacketPlayOutSpawnEntity packet = new PacketPlayOutSpawnEntity(spawn, replacedCache.cache().getRight());
					
					for (Player player : Bukkit.getServer().getOnlinePlayers()) {
						((CraftPlayer) player).getHandle().b.a(packet);
					}
					Entity entity = spawn.getBukkitEntity();
					entity.setInvulnerable(replacedCache.invulnerable());
					entity.setCustomName(replaced.getCustomName());
					entity.setCustomNameVisible(true);
					entity.getPersistentDataContainer().remove(REPLACED_KEY);
				});
				ReplaceableSpirit.remove(spirit.entity(), ReplaceableSpirit.fromEntity(spirit.entity()));
			}
		}
	}
	
	/**
	 * @param entity - Entity mapped to find spirit associated with the entity
	 * @return an Optional of the Spirit that is found, if any
	 */
	public static Optional<Spirit> of(Entity entity) {
		// TODO: gonna need a better way to search
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
		
		showEntity(spirit);
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
					showEntity(s);
					s.entity().remove();
				}
				return condition;
			});
		}
	}
	public static void cleanup() {
		SPIRIT_CACHE.values().forEach(spirit -> {
			showEntity(spirit);
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
}

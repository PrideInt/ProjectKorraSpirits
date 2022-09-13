package me.pride.spirits.api;

import me.pride.spirits.api.event.EntityReplacedBySpiritEvent;
import me.pride.spirits.api.event.EntitySpiritReplaceEvent;
import me.pride.spirits.api.record.SpiritRecord;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.function.Consumer;

public class ReplaceableSpirit extends Spirit implements Replaceable {
	protected static final Map<Entity, ReplaceableSpirit> REPLACED = new HashMap<>();
	
	private SpiritRecord record;
	private Optional<ReplacedCache> replacedCache;
	
	public ReplaceableSpirit(World world, Location location, String name, EntityType entityType, SpiritType spiritType, long revertTime) {
		super(world, location);
		this.record = new SpiritRecord(name, entityType, spiritType, revertTime);
		this.replacedCache = Optional.empty();
		super.spawnEntity(world, location);
	}
	
	public ReplaceableSpirit(World world, Entity entity, String name, EntityType entityType, SpiritType spiritType, long revertTime) {
		super(world, entity.getLocation());
		if (isReplacedEntity(entity)) {
			super.removeFromCache();
			REPLACED.remove(this);
			return;
		}
		this.record = new SpiritRecord(name, entityType, spiritType, revertTime);
		
		replaceEntity(world, entity);
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
		
		this.replacedCache = Optional.of(new ReplacedCache(true, entity.isInvulnerable(), Pair.of(entity, entity.getEntityId())));
		
		entity.setInvulnerable(true);
		entity.getPersistentDataContainer().set(REPLACED_KEY, PersistentDataType.STRING, "replacedentity");
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
		return REPLACED.remove(entity, spirit);
	}
	public void remove() {
		REPLACED.entrySet().removeIf(entry -> entry.getValue().equals(this));
		super.remove();
	}
	
	@Override
	public Optional<ReplacedCache> replacedCache() { return this.replacedCache; }
	@Override
	public SpiritRecord record() { return this.record; }
	
	public ReplacedCache getReplacedCache() { return replacedCache.get(); }
	public void ifCachePresent(Consumer<ReplacedCache> consumer) {
		replacedCache.ifPresent(consumer);
	}
	public Optional<Entity> entityInCache() {
		if (replacedCache.isPresent()) {
			return Optional.of(replacedCache.get().cache().getLeft());
		}
		return Optional.empty();
	}
	public static Iterable<ReplaceableSpirit> replaced() { return REPLACED.values(); }
}

package me.pride.spirits.api;

import me.pride.spirits.Spirits;
import me.pride.spirits.api.event.EntityReplacedBySpiritEvent;
import me.pride.spirits.api.event.EntitySpiritDestroyEvent;
import me.pride.spirits.api.event.EntitySpiritReplaceEvent;
import me.pride.spirits.api.event.EntitySpiritSpawnEvent;
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

import java.util.*;

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
	protected abstract void override(SpiritType type, EntityType entityType, String spiritName, long revertTime);
	
	private World world;
	private Location location;
	private Entity entity;
	private long start, end;
	
	public Spirit(World world, Location location) {
		this.entity = world.spawnEntity(location, entityType());
		this.start = System.currentTimeMillis();
		this.end = System.currentTimeMillis() + revertTime();
		
		this.entity.setCustomName(spiritName());
		this.entity.setCustomNameVisible(true);
		
		EntitySpiritSpawnEvent spawnEvent = new EntitySpiritSpawnEvent(this);
		Bukkit.getServer().getPluginManager().callEvent(spawnEvent);
		
		RECOLLECTION.add(this);
	}
	// Used if we're replacing an entity
	public Spirit(World world, Entity entity) {
		this(world, entity.getLocation());
		
		SPIRIT_CACHE.put(this, Pair.of(entity, entity.getEntityId()));
		// Do packets stuff to hide and unhide original entity
		PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(entity.getEntityId());
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			((CraftPlayer) player).getHandle().b.a(packet);
		}
		Event event = new EntityReplacedBySpiritEvent(entity, this);
		if (exists(entity)) {
			Spirit spirit = of(entity).get();
			if (spirit instanceof Replaceable) event = new EntitySpiritReplaceEvent(spirit, this);
		}
		Bukkit.getServer().getPluginManager().callEvent(event);
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
	public static Optional<Spirit> of(Entity entity) {
		return RECOLLECTION.stream().filter(s -> s.entity().getUniqueId() == entity.getUniqueId()).findFirst();
	}
	public static boolean exists(Entity entity) {
		return Spirit.of(entity).isPresent();
	}
	public static boolean destroy(Spirit spirit) {
		showEntity(spirit);
		Pair<Entity, Integer> cache = SPIRIT_CACHE.get(spirit);
		return RECOLLECTION.remove(spirit) && (SPIRIT_CACHE.get(spirit) == null ? true : SPIRIT_CACHE.remove(spirit, cache));
	}
	public static void handle() {
		Spirit spirit = RECOLLECTION.peek();
		
		if (spirit != null) {
			if (spirit.timesUp() && spirit.revertTime() >= 0) {
				showEntity(spirit);
				RECOLLECTION.pop();
			}
		}
		RECOLLECTION.removeIf(s -> {
			boolean condition = s.entity().isDead() || !s.entity().isValid();
			if (condition) {
				showEntity(s);
			}
			return condition;
		});
	}
	public static void cleanup() {
		// concurrency issues might arise
		SPIRIT_CACHE.keySet().forEach(spirit -> {
			showEntity(spirit);
		});
		SPIRIT_CACHE.clear();
		RECOLLECTION.clear();
	}
}

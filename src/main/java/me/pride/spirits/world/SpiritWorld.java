package me.pride.spirits.world;

import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class SpiritWorld {
	private static final Map<World, SpiritWorld> WORLDS = new HashMap<>();
	
	private World world;
	
	public SpiritWorld(World world) { this.world = world; }
	
	public static SpiritWorld create(World world) {
		return new SpiritWorld(world);
	}
	public static SpiritWorld of(World world) {
		return WORLDS.get(world);
	}
	public static void handle() {
	
	}
	public static Map<World, SpiritWorld> spiritWorlds() {
		return WORLDS;
	}
}

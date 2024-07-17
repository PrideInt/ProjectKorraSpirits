package me.pride.spirits.world;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import java.util.HashMap;
import java.util.Map;

public class SpiritWorld {
	private static final Map<World, SpiritWorld> WORLDS = new HashMap<>();
	
	private World world;
	private BossBar bar;
	
	public SpiritWorld(World world) {
		this.world = world;
		WORLDS.put(world, this);

		this.bar = Bukkit.createBossBar(ChatColor.of("#6bb589") + "Spirit World", BarColor.BLUE, BarStyle.SOLID);
		this.bar.addFlag(BarFlag.DARKEN_SKY);
		this.bar.addFlag(BarFlag.CREATE_FOG);

		world.getPlayers().forEach(player -> this.bar.addPlayer(player));
	}
	public World getWorld() {
		return world;
	}
	public BossBar getBossBar() {
		return bar;
	}
	public void setDrySun() {

	}
	public void setHeavyRain() {

	}
	
	public static SpiritWorld create(World world) {
		return new SpiritWorld(world);
	}
	public static SpiritWorld of(World world) {
		return WORLDS.get(world);
	}
	public static boolean isSpiritWorld(World world) {
		return WORLDS.containsKey(world);
	}
	public static boolean remove(World world) {
		return WORLDS.remove(world) != null;
	}
	public static void handle() {
	
	}
	public static Map<World, SpiritWorld> spiritWorlds() {
		return WORLDS;
	}
}

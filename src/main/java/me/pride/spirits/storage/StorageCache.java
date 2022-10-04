package me.pride.spirits.storage;

import com.google.gson.Gson;
import me.pride.spirits.Spirits;
import org.bukkit.World;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class StorageCache {
	private static final Set<UUID> UUID_CACHE = new HashSet<>();
	private static final Map<String, List<int[]>> LOCATIONS = new HashMap<>();
	
	private static final File STATION = new File(Spirits.instance.getDataFolder().getAbsolutePath() + File.separator + "stations.json");
	private static final File STORAGE = new File(Spirits.instance.getDataFolder().getAbsolutePath() + File.separator + "storage.db");
	
	public static void queryUUIDs() {
	
	}
	public static void removeUUIDFromCache(UUID uuid) {
		UUID_CACHE.remove(uuid);
	}
	public static void addUUIDToCache(UUID uuid) {
		UUID_CACHE.add(uuid);
	}
	public static Iterable<UUID> uuidCache() {
		return UUID_CACHE;
	}
	
	public static void queryLocations() {
		Gson gson = new Gson();
		
		try {
			Reader reader = new FileReader(STATION);
			Map<String, List<int[]>> map = gson.fromJson(reader, Map.class);
			
			LOCATIONS.putAll(map);
		} catch (Exception e) {
			try {
				STATION.createNewFile();
			} catch (IOException ioe) {
			
			}
		}
	}
	public static void updateLocations() {
		Gson gson = new Gson();
		
		try {
			Writer writer = new FileWriter(STATION, false);
			gson.toJson(LOCATIONS, writer);
			writer.flush();
			writer.close();
		} catch (IOException e) {
		
		}
	}
	public static void removeLocationsFromCache(World world, int[] coordinates) {
		LOCATIONS.computeIfPresent(world.toString(), (wrld, list) -> { list.remove(coordinates); return list; });
	}
	public static void addLocationsToCache(World world, int[] coordinates) {
		if (LOCATIONS.containsKey(world.toString())) {
			LOCATIONS.get(world).add(coordinates);
		} else {
			LOCATIONS.put(world.toString(), new ArrayList<int[]>(Arrays.asList(coordinates)));
		}
	}
}
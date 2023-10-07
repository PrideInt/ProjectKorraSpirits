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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StorageCache {
	private static final Set<UUID> UUID_CACHE = ConcurrentHashMap.newKeySet();
	private static final Map<String, List<int[]>> LOCATIONS = new ConcurrentHashMap<>();
	
	protected static final File STATION = new File(Spirits.instance.getDataFolder().getAbsolutePath() + File.separator + "stations.json");
	protected static final File STORAGE = new File(Spirits.instance.getDataFolder().getAbsolutePath() + File.separator + "storage.db");
	
	public static void queryUUIDs(SQLite sql) {
		try {
			ResultSet set = sql.set();
			
			while (set.next()) {
				UUID_CACHE.add(UUID.fromString(set.getString("id")));
			}
		} catch (SQLException e) { }
	}
	public static void updateUUIDs(SQLite sql) {
		Set<UUID> dbSet = ConcurrentHashMap.newKeySet();
		
		try {
			ResultSet set = sql.set();
			
			while (set.next()) {
				String suuid = set.getString("id");
				UUID uuid = UUID.fromString(suuid);
				
				dbSet.add(uuid);
				if (!UUID_CACHE.contains(uuid)) {
					sql.delete(suuid);
				}
			}
			for (UUID uuid : UUID_CACHE) {
				if (!dbSet.contains(uuid.toString())) {
					sql.insert(uuid.toString());
				}
			}
		} catch (SQLException e) { }
	}
	public static void removeUUIDFromCache(UUID uuid) {
		UUID_CACHE.remove(uuid);
	}
	public static void addUUIDToCache(UUID uuid) {
		UUID_CACHE.add(uuid);
	}
	public static Set<UUID> uuidCache() {
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
			} catch (IOException ioe) { }
		}
	}
	public static void updateLocations() {
		Gson gson = new Gson();
		
		try {
			Writer writer = new FileWriter(STATION, false);
			gson.toJson(LOCATIONS, writer);
			writer.flush();
			writer.close();
		} catch (IOException e) { }
	}
	public static Map<String, List<int[]>> locations() {
		return LOCATIONS;
	}
	public static void removeLocationsFromCache(World world, int[] coordinates) {
		LOCATIONS.computeIfPresent(world.getName(), (wrld, list) -> { list.remove(coordinates); return list; });
	}
	public static void addLocationsToCache(World world, int[] coordinates) {
		if (LOCATIONS.containsKey(world.getName())) {
			LOCATIONS.get(world).add(coordinates);
		} else {
			LOCATIONS.put(world.toString(), new ArrayList<int[]>(List.of(coordinates)));
		}
	}
}

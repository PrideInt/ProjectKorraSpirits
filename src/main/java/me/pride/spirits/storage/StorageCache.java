package me.pride.spirits.storage;

import com.google.gson.Gson;
import me.pride.spirits.Spirits;
import me.pride.spirits.api.Spirit;
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
import java.util.function.Consumer;

public class StorageCache {
	private static final Set<UUID> UUID_CACHE = ConcurrentHashMap.newKeySet();
	private static final Map<UUID, Integer> TOTEM_STACK_CACHE = new ConcurrentHashMap<>();
	private static final Map<String, List<int[]>> LOCATIONS = new ConcurrentHashMap<>();
	
	protected static final File STATION = new File(Spirits.instance.getDataFolder().getAbsolutePath() + File.separator + "stations.json");
	protected static final File STORAGE = new File(Spirits.instance.getDataFolder().getAbsolutePath() + File.separator + "storage.db");

	public static void queryTotemStacks(SQLite sql) {
		query(sql, Database.SELECT_ALL_TOTEM_STACK, set -> {
			try {
				TOTEM_STACK_CACHE.put(UUID.fromString(set.getString("uuid")), set.getInt("stack"));
			} catch (SQLException e) { }
		});
	}
	public static void updateTotemStacks(SQLite sql) {
		Map<UUID, Integer> dbMap = new ConcurrentHashMap<>();

		try {
			ResultSet set = sql.set(Database.SELECT_ALL_TOTEM_STACK);

			while (set.next()) {
				String suuid = set.getString("uuid");
				UUID uuid = UUID.fromString(suuid);
				int stack = set.getInt("stack");

				dbMap.put(uuid, stack);
				if (!TOTEM_STACK_CACHE.containsKey(uuid)) {
					sql.deleteTotemStack(suuid);
				} else {
					if (TOTEM_STACK_CACHE.get(uuid) != stack) {
						sql.updateTotemStack(suuid, String.valueOf(TOTEM_STACK_CACHE.get(uuid)));
					}
				}
			}
			for (UUID uuid : TOTEM_STACK_CACHE.keySet()) {
				if (!dbMap.keySet().contains(uuid)) {
					sql.insertTotemStack(uuid.toString() + "," + TOTEM_STACK_CACHE.get(uuid));
				}
			}
		} catch (SQLException e) { }
	}
	public static void queryUUIDs(SQLite sql) {
		query(sql, Database.SELECT_ALL_BOSS, set -> {
			try {
				UUID_CACHE.add(UUID.fromString(set.getString("id")));
			} catch (SQLException e) { }
		});
	}
	public static void updateUUIDs(SQLite sql) {
		Set<UUID> dbSet = ConcurrentHashMap.newKeySet();
		
		try {
			ResultSet set = sql.set(Database.SELECT_ALL_BOSS);
			
			while (set.next()) {
				String suuid = set.getString("id");
				UUID uuid = UUID.fromString(suuid);
				
				dbSet.add(uuid);
				if (!UUID_CACHE.contains(uuid)) {
					sql.deleteBoss(suuid);
				}
			}
			for (UUID uuid : UUID_CACHE) {
				if (!dbSet.contains(uuid.toString())) {
					sql.insertBoss(uuid.toString());
				}
			}
		} catch (SQLException e) { }
	}
	public static void querySpirits(SQLite sql) {
		query(sql, Database.SELECT_ALL_SPIRIT, set -> {
			try {
				Spirit.SPIRIT_CACHE.keySet().add(UUID.fromString(set.getString("spirit_id")));
			} catch (SQLException e) { }
		});
	}
	public static void updateSpirits(SQLite sql) {
		Set<UUID> dbSet = ConcurrentHashMap.newKeySet();

		try {
			ResultSet set = sql.set(Database.SELECT_ALL_SPIRIT);

			while (set.next()) {
				String suuid = set.getString("spirit_id");
				UUID uuid = UUID.fromString(suuid);

				dbSet.add(uuid);
				if (!Spirit.SPIRIT_CACHE.keySet().contains(uuid)) {
					sql.deleteSpirit(suuid);
				}
			}
			for (UUID uuid : Spirit.SPIRIT_CACHE.keySet()) {
				if (!dbSet.contains(uuid.toString())) {
					sql.insertSpirit(uuid.toString());
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

	public static void query(SQLite sql, String query, Consumer<ResultSet> consumer) {
		try {
			ResultSet set = sql.set(query);

			while (set.next()) {
				consumer.accept(set);
			}
		} catch (SQLException e) { }
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
			LOCATIONS.get(world.getName()).add(coordinates);
		} else {
			LOCATIONS.put(world.getName(), new ArrayList<int[]>(List.of(coordinates)));
		}
	}
}

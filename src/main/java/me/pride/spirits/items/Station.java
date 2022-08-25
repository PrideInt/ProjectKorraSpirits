package me.pride.spirits.items;

import me.pride.spirits.Spirits;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class Station {
	// create JSON to store ancient spirit-science station blocks
	public static ItemStack STATION;
	public static Recipe RECIPE;
	
	public static final NamespacedKey ANCIENT_STATION_KEY = new NamespacedKey(Spirits.instance, "ancient_station");
	
	private static void setupStation() {
		ItemStack station = new ItemStack(Material.CRAFTING_TABLE, 1);
		
		ItemMeta meta = station.getItemMeta();
		meta.setDisplayName("§fAncient Spirit-Science Station");
		
		PersistentDataContainer container = meta.getPersistentDataContainer();
		container.set(ANCIENT_STATION_KEY, PersistentDataType.STRING, "ancient_station");
		
		station.setItemMeta(meta);
		
		STATION = station;
		
		ShapedRecipe recipe = new ShapedRecipe(ANCIENT_STATION_KEY, STATION);
		recipe.shape(" T ", "TST", " T ");
		recipe.setIngredient('T', Material.RAW_GOLD);
		recipe.setIngredient('S', Material.CRAFTING_TABLE);
		
		Bukkit.getServer().addRecipe(recipe);
		
		RECIPE = recipe;
	}
	
	public static void setup() {
		setupStation();
	}
}
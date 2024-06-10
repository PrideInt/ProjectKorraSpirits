package me.pride.spirits.game;

import me.pride.spirits.Spirits;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class Atrium {
	public static ItemStack SOULLESS_ATRIUM;

	public static final NamespacedKey SOULLESS_ATRIUM_KEY = new NamespacedKey(Spirits.instance, "soulless_atrium");

	private static void setupAtrium() {
		ItemStack atrium = new ItemStack(Material.ANCIENT_DEBRIS, 1);

		ItemMeta meta = atrium.getItemMeta();
		meta.setDisplayName("§fSoulless Atrium");

		PersistentDataContainer container = meta.getPersistentDataContainer();
		container.set(SOULLESS_ATRIUM_KEY, PersistentDataType.STRING, "soulless_atrium");

		atrium.setItemMeta(meta);

		SOULLESS_ATRIUM = atrium;
	}

	public static void setup() {
		setupAtrium();
	}
}

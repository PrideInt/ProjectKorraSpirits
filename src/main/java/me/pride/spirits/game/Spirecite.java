package me.pride.spirits.game;

import me.pride.spirits.Spirits;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class Spirecite {
	public static ItemStack SPIRECITE, SPIRECITE_FRAGMENTS, SPIRECITE_BLOCK;
	public static ItemStack SPIRECITE_CROWN, SPIRECITE_CLUB, SPIRECITE_MEDALLION;
	
	public static final NamespacedKey SPIRECITE_KEY = new NamespacedKey(Spirits.instance, "spirecite");
	public static final NamespacedKey FRAGMENTS_KEY = new NamespacedKey(Spirits.instance, "spirecite_fragments");
	public static final NamespacedKey SPIRECITE_BLOCK_KEY = new NamespacedKey(Spirits.instance, "spirecite_block");
	
	public static final NamespacedKey SPIRECITE_CROWN_KEY = new NamespacedKey(Spirits.instance, "spirecite_crown");
	public static final NamespacedKey SPIRECITE_CLUB_KEY = new NamespacedKey(Spirits.instance, "spirecite_club");
	public static final NamespacedKey SPIRECITE_MEDALLION_KEY = new NamespacedKey(Spirits.instance, "spirecite_armor");
	
	public static final NamespacedKey[] KEYS = { SPIRECITE_KEY, FRAGMENTS_KEY, SPIRECITE_CROWN_KEY, SPIRECITE_CLUB_KEY, SPIRECITE_MEDALLION_KEY };
	
	private static void setupItem() {
		ItemStack spirecite = new ItemStack(Material.RAW_GOLD, 1), fragments = new ItemStack(Material.GOLD_NUGGET, 1), spireciteBlock = new ItemStack(Material.RAW_GOLD_BLOCK, 1);
		
		ItemMeta spireciteMeta = spirecite.getItemMeta(), fragmentsMeta = fragments.getItemMeta(), spireciteBlockMeta = spireciteBlock.getItemMeta();
		
		spireciteMeta.setDisplayName("§fSpirecite");
		fragmentsMeta.setDisplayName("§fSpirecite Fragments");
		spireciteBlockMeta.setDisplayName("§fSpirecite Block");
		
		PersistentDataContainer spireciteTag = spireciteMeta.getPersistentDataContainer(), fragmentsTag = fragmentsMeta.getPersistentDataContainer(), spireciteBlockTag = spireciteBlockMeta.getPersistentDataContainer();
		spireciteTag.set(SPIRECITE_KEY, PersistentDataType.STRING, "spirecite");
		fragmentsTag.set(FRAGMENTS_KEY, PersistentDataType.STRING, "spirecite_fragments");
		spireciteBlockTag.set(SPIRECITE_BLOCK_KEY, PersistentDataType.STRING, "spirecite_block");
		
		spirecite.setItemMeta(spireciteMeta);
		fragments.setItemMeta(fragmentsMeta);
		spireciteBlock.setItemMeta(spireciteBlockMeta);
		
		SPIRECITE = spirecite;
		SPIRECITE_FRAGMENTS = fragments;
		SPIRECITE_BLOCK = spireciteBlock;
		
		createSpireciteRecipe(SPIRECITE);
		createSpireciteBlockRecipe(SPIRECITE_BLOCK);
	}
	
	private static void setupSpireciteItem() {
		ItemStack crown = new ItemStack(Material.WAXED_CUT_COPPER_SLAB, 1);
		ItemStack club = new ItemStack(Material.GOLDEN_SHOVEL, 1);
		ItemStack medallion = new ItemStack(Material.GOLDEN_CHESTPLATE, 1);
		
		ItemStack[] spireciteItems = {crown, medallion, club};
		
		ItemMeta crownMeta = crown.getItemMeta(), clubMeta = club.getItemMeta(), medallionMeta = medallion.getItemMeta();
		
		crownMeta.setDisplayName("§fSpirecite Crown");
		clubMeta.setDisplayName("§fSpirecite Club");
		medallionMeta.setDisplayName("§fSpirecite Medallion");
		
		ItemMeta[] thuleciteMetas = {crownMeta, clubMeta, medallionMeta};
		
		crownMeta.getPersistentDataContainer().set(SPIRECITE_CROWN_KEY, PersistentDataType.STRING, "spirecite_crown");
		clubMeta.getPersistentDataContainer().set(SPIRECITE_CLUB_KEY, PersistentDataType.STRING, "spirecite_club");
		medallionMeta.getPersistentDataContainer().set(SPIRECITE_MEDALLION_KEY, PersistentDataType.STRING, "spirecite_medallion");
		
		for (int i = 0; i < spireciteItems.length; i++) {
			spireciteItems[i].setItemMeta(thuleciteMetas[i]);
		}
		SPIRECITE_CROWN = crown;
		SPIRECITE_CLUB = club;
		SPIRECITE_MEDALLION = medallion;
		
		createSpireciteItemRecipe();
	}
	
	private static void createSpireciteRecipe(ItemStack spirecite) {
		ShapedRecipe spireciteRecipe = new ShapedRecipe(SPIRECITE_KEY, spirecite).shape("TTT", "TTT", "TTT").setIngredient('T', Material.GOLD_NUGGET);
		Bukkit.getServer().addRecipe(spireciteRecipe);
	}
	
	private static void createSpireciteItemRecipe() {
		ShapedRecipe crownRecipe = new ShapedRecipe(SPIRECITE_CROWN_KEY, SPIRECITE_CROWN).shape("   ", "TTT", "   ").setIngredient('T', Material.RAW_GOLD);
		ShapedRecipe clubRecipe = new ShapedRecipe(SPIRECITE_MEDALLION_KEY, SPIRECITE_MEDALLION).shape(" T ", "TTT", " S ");
		clubRecipe.setIngredient('T', Material.RAW_GOLD).setIngredient('S', Material.GOLDEN_SWORD);
		ShapedRecipe medallionRecipe = new ShapedRecipe(SPIRECITE_CLUB_KEY, SPIRECITE_CLUB).shape("T T", "TTT", " T ").setIngredient('T', Material.RAW_GOLD);
		
		ShapedRecipe[] recipes = {crownRecipe, clubRecipe, medallionRecipe};
		for (ShapedRecipe r : recipes) {
			Bukkit.getServer().addRecipe(r);
		}
	}

	private static void createSpireciteBlockRecipe(ItemStack spireciteBlock) {
		ShapedRecipe spireciteBlockRecipe = new ShapedRecipe(SPIRECITE_BLOCK_KEY, spireciteBlock).shape("TTT", "TTT", "TTT").setIngredient('T', Material.RAW_GOLD);
		Bukkit.getServer().addRecipe(spireciteBlockRecipe);
	}
	
	public static void setup() {
		setupItem();
		setupSpireciteItem();
	}
}
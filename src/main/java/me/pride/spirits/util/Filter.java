package me.pride.spirits.util;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.region.RegionProtection;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class Filter {
	public static boolean filterEntityLight(Entity entity) {
		switch (entity.getType()) {
			case ALLAY:
			case AXOLOTL:
			case CAT:
			case COW:
			case FOX:
			case HORSE:
			case PIG:
			case RABBIT:
			case SHEEP:
			case SLIME:
			case WOLF:
				return true;
		}
		return false;
	}
	public static boolean filterEntityDark(Entity entity) {
		switch (entity.getType()) {
			case BLAZE:
			case CAVE_SPIDER:
			case CREEPER:
			case DROWNED:
			case ENDERMAN:
			case ENDERMITE:
			case EVOKER:
			case ILLUSIONER:
			case MAGMA_CUBE:
			case PILLAGER:
			case PHANTOM:
			case RAVAGER:
			case SKELETON:
			case SPIDER:
			case VEX:
			case WARDEN:
			case WITCH:
			case WITHER:
			case ZOGLIN:
			case ZOMBIE:
			case ZOMBIE_HORSE:
			case ZOMBIE_VILLAGER:
			case ZOMBIFIED_PIGLIN:
				return true;
		}
		return false;
	}
	public static boolean filterEntityNeutral(Entity entity) {
		return !filterEntityLight(entity) && !filterEntityDark(entity);
	}
	public static boolean filterEntityFromAbility(Entity entity, Player player, CoreAbility ability) {
		return entity.getUniqueId() != player.getUniqueId() && !RegionProtection.isRegionProtected(ability, entity.getLocation());
	}
	public static boolean filterGeneralEntity(Entity entity, Player player, CoreAbility ability) {
		return filterEntityFromAbility(entity, player, ability) && entity.getType() != EntityType.ARMOR_STAND && !Commands.invincible.contains(entity.getName());
	}
	public static boolean filterGeneralSolidBlock(Material material) {
		return material.isSolid() && !material.isInteractable() && material != Material.BARRIER;
	}
	public static boolean filterGeneralSolidBlock(Block block) {
		return filterGeneralSolidBlock(block.getType());
	}
	public static boolean filterIndestructible(Material material) {
		switch (material) {
			case BEDROCK:
			case BARRIER:
			case COMMAND_BLOCK:
			case COMMAND_BLOCK_MINECART:
			case CHAIN_COMMAND_BLOCK:
			case END_GATEWAY:
			case END_PORTAL_FRAME:
			case END_PORTAL:
			case JIGSAW:
			case NETHER_PORTAL:
			case REPEATING_COMMAND_BLOCK:
			case STRUCTURE_BLOCK:
			case STRUCTURE_VOID:
				return true;
		}
		return false;
	}
	public static boolean filterIndestructible(Block block) {
		return filterIndestructible(block.getType());
	}
	public static boolean filterFlowers(Material material) {
		switch (material) {
			case DANDELION:
			case POPPY:
			case BLUE_ORCHID:
			case ALLIUM:
			case AZURE_BLUET:
			case RED_TULIP:
			case ORANGE_TULIP:
			case PINK_TULIP:
			case WHITE_TULIP:
			case OXEYE_DAISY:
			case CORNFLOWER:
			case LILY_OF_THE_VALLEY:
			case WITHER_ROSE:
			case SUNFLOWER:
			case LILAC:
			case ROSE_BUSH:
			case PEONY:
				return true;
		}
		return false;
	}
	public static boolean filterFlowers(Block block) {
		return filterFlowers(block.getType());
	}
	public static boolean filterCrops(Material material, boolean includeFarmland) {
		if (includeFarmland && material == Material.FARMLAND) {
			return true;
		}
		switch (material) {
			case BEETROOTS:
			case CARROTS:
			case CAVE_VINES:
			case COCOA:
			case MELON_STEM:
			case NETHER_WART:
			case POTATOES:
			case PUMPKIN_STEM:
			case WHEAT:
				return true;
		}
		return false;
	}
	public static boolean filterCrops(Block block, boolean includeFarmland) {
		if (block.getBlockData() instanceof Ageable) {
			return true;
		}
		return filterCrops(block.getType(), includeFarmland);
	}
	public static boolean filterCrops(Material material) {
		return filterCrops(material, false);
	}
	public static boolean filterCrops(Block block) {
		return filterCrops(block, false);
	}
	public static boolean filterLogs(Material material) {
		switch (material) {
			case ACACIA_LOG:
			case BIRCH_LOG:
			case DARK_OAK_LOG:
			case JUNGLE_LOG:
			case OAK_LOG:
			case SPRUCE_LOG:
				return true;
		}
		return false;
	}
	public static boolean filterLogs(Block block) {
		return filterLogs(block.getType());
	}
	public static boolean filterLeaves(Material material) {
		switch (material) {
			case ACACIA_LEAVES:
			case BIRCH_LEAVES:
			case DARK_OAK_LEAVES:
			case JUNGLE_LEAVES:
			case OAK_LEAVES:
			case SPRUCE_LEAVES:
				return true;
		}
		return false;
	}
	public static boolean filterLeaves(Block block) {
		return filterLeaves(block.getType());
	}
}

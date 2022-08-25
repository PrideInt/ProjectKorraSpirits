package me.pride.spirits.util;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.Material;
import org.bukkit.entity.*;

public class Filter {
	public static boolean filterEntityLight(Entity entity) {
		return entity instanceof Sheep || entity instanceof Cow || entity instanceof Pig || entity instanceof Rabbit || entity instanceof Cat || entity instanceof Wolf || entity instanceof Horse || (entity instanceof Slime && !(entity instanceof MagmaCube)) || entity instanceof Fox;
	}
	public static boolean filterEntityDark(Entity entity) {
		return entity instanceof Spider || entity instanceof Enderman || entity instanceof Zoglin || entity instanceof CaveSpider || entity instanceof Skeleton || entity instanceof Zombie || entity instanceof ZombieHorse || (entity instanceof MagmaCube && !(entity instanceof Slime)) || entity instanceof Blaze;
	}
	public static boolean filterEntityNeutral(Entity entity) {
		return !filterEntityLight(entity) && !filterEntityDark(entity);
	}
	public static boolean filterEntityFromAbility(Entity entity, Player player, CoreAbility ability) {
		return entity.getUniqueId() != player.getUniqueId() && !GeneralMethods.isRegionProtectedFromBuild(ability, entity.getLocation());
	}
	public static boolean filterBlockFlowers(Material material) {
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
}

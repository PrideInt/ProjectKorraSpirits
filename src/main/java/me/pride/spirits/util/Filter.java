package me.pride.spirits.util;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.region.RegionProtection;
import org.bukkit.Material;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Cat;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Horse;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zoglin;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieHorse;

public class Filter {
	public static boolean filterEntityLight(Entity entity) {
		return entity instanceof Sheep || entity instanceof Cow || entity instanceof Pig || entity instanceof Rabbit || entity instanceof Cat || entity instanceof Wolf || entity instanceof Horse || (entity instanceof Slime && !(entity instanceof MagmaCube)) || entity instanceof Fox || entity instanceof Allay;
	}
	public static boolean filterEntityDark(Entity entity) {
		return entity instanceof Spider || entity instanceof Enderman || entity instanceof Zoglin || entity instanceof CaveSpider || entity instanceof Skeleton || entity instanceof Zombie || entity instanceof Creeper || entity instanceof ZombieHorse || (entity instanceof MagmaCube && !(entity instanceof Slime)) || entity instanceof Blaze;
	}
	public static boolean filterEntityNeutral(Entity entity) {
		return !filterEntityLight(entity) && !filterEntityDark(entity);
	}
	public static boolean filterEntityFromAbility(Entity entity, Player player, CoreAbility ability) {
		return entity.getUniqueId() != player.getUniqueId() && !RegionProtection.isRegionProtected(ability, entity.getLocation());
	}
	public static boolean filterGeneral(Entity entity, Player player, CoreAbility ability) {
		return filterEntityFromAbility(entity, player, ability) && entity.getType() != EntityType.ARMOR_STAND && !Commands.invincible.contains(entity.getName());
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

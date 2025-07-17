package me.pride.spirits.util;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import me.pride.spirits.api.DarkSpirit;
import me.pride.spirits.api.LightSpirit;
import me.pride.spirits.api.ability.SpiritElement;
import me.pride.spirits.util.objects.TetraConsumer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Tools {
	public enum Path { ABILITIES("Abilities"), COMBOS("Combos"), PASSIVES("Passives");
		private String path;
		
		Path(String path) { this.path = path; }
		public String getPath() { return this.path; }
	}
	
	public static String path(CoreAbility ability, Path path) {
		StringBuilder element = new StringBuilder();
		
		if (ability.getElement().equals(SpiritElement.DARK_SPIRIT)) {
			element.append("Dark.");
		} else if (ability.getElement().equals(SpiritElement.LIGHT_SPIRIT)) {
			element.append("Light.");
		} else if (ability.getElement().equals(SpiritElement.SPIRIT)) {
			element.append("Spirit.");
		}
		return element.append(path.getPath() + "." + ability.getName() + ".").toString();
	}
	
	public static ChatColor getOppositeColor(Element element) {
		if (element.equals(SpiritElement.SPIRIT)) {
			return SpiritElement.SPIRIT.getSubColor();
		} else if (element.equals(SpiritElement.LIGHT_SPIRIT)) {
			return SpiritElement.DARK_SPIRIT.getColor();
		} else if (element.equals(SpiritElement.DARK_SPIRIT)) {
			return SpiritElement.LIGHT_SPIRIT.getColor();
		}
		return ChatColor.RESET;
	}
	
	public static void trackEntitySpirit(Location location, double radius, Predicate<Entity> filter, TetraConsumer<LivingEntity, Boolean, Boolean, Boolean> tetra) {
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, radius)) {
			if (!filter.test(entity) || entity.hasMetadata(Keys.ORB_KEY)) continue;
			
			boolean light = false, dark = false, neutral = true;
			boolean lightAccept = false, darkAccept = false, neutralAccept = false;
			
			if (entity instanceof LivingEntity) {
				LivingEntity e = (LivingEntity) entity;

				if (LightSpirit.isLightSpirit(e)) {
					light = true;
				} else if (DarkSpirit.isDarkSpirit(e)) {
					dark = true;
				}
				if (light) {
					lightAccept = true;
				} else if ((neutral && !dark) || (light && dark)) {
					neutralAccept = true;
				} else if (dark || (dark && neutral)) {
					darkAccept = true;
				}
				tetra.accept(e, lightAccept, darkAccept, neutralAccept);
			}
		}
	}

	public static Block rayTraceBlock(World world, Location start, Vector direction, double range, boolean traceFluid) {
		RayTraceResult result = traceFluid ?
				world.rayTraceBlocks(start.clone(), direction, range, FluidCollisionMode.ALWAYS, true) :
				world.rayTraceBlocks(start.clone(), direction, range);
		if (result != null) {
			return result.getHitBlock();
		}
		return null;
	}

	public static Block rayTraceBlock(World world, Location start, Vector direction, double range) {
		return rayTraceBlock(world, start, direction, range, false);
	}

	public static Block rayTraceBlock(Player player, double range, boolean traceFluid) {
		return rayTraceBlock(player.getWorld(), player.getEyeLocation(), player.getEyeLocation().getDirection(), range, traceFluid);
	}

	public static Block rayTraceBlock(Player player, double range) {
		return rayTraceBlock(player.getWorld(), player.getEyeLocation(), player.getEyeLocation().getDirection(), range);
	}

	public static Entity rayTraceEntity(World world, Player player, Location start, Vector direction, double range, double radius) {
		RayTraceResult result = world.rayTraceEntities(start.clone(), direction, range, radius, e -> e.getUniqueId() != player.getUniqueId() && !e.hasMetadata(Keys.ORB_KEY));
		if (result != null) {
			return result.getHitEntity();
		}
		return null;
	}

	public static Entity rayTraceEntity(Player player, double range, double radius) {
		return rayTraceEntity(player.getWorld(), player, player.getEyeLocation(), player.getEyeLocation().getDirection(), range, radius);
	}

	public static Entity rayTraceEntity(Player player, double range) {
		return rayTraceEntity(player.getWorld(), player, player.getEyeLocation(), player.getEyeLocation().getDirection(), range, 1.2);
	}

	public static RayTraceResult rayTrace(World world, Location start, Vector direction, double range, double raySize, Predicate<Entity> predicate) {
		Predicate<Entity> pred = predicate;
		if (predicate == null) {
			pred = e -> true;
		}
		RayTraceResult result = world.rayTrace(
				start.clone(),
				direction,
				range,
				FluidCollisionMode.NEVER,
				false,
				raySize,
				pred);

		if (result != null) {
			return result;
		}
		return null;
	}

	public static RayTraceResult rayTrace(World world, Location start, Vector direction, double range, double raySize) {
		return rayTrace(world, start, direction, range, raySize, null);
	}

	public static RayTraceResult rayTrace(World world, Location start, Vector direction, double range, Predicate<Entity> predicate) {
		return rayTrace(world, start, direction, range, 1, predicate);
	}

	public static RayTraceResult rayTrace(Player player, double range, Predicate<Entity> predicate) {
		return rayTrace(player.getWorld(), player.getEyeLocation(), player.getEyeLocation().getDirection(), range, 1, predicate);
	}

	public static RayTraceResult rayTrace(Player player, double range) {
		return rayTrace(player.getWorld(), player.getEyeLocation(), player.getEyeLocation().getDirection(), range, 1, null);
	}

	public static void createCircle(Location location, double size, int points, Consumer<Location> consumer) {
		for (int angle = 180; angle >= 0; angle -= 5) {
			double x = size * Math.cos(Math.toRadians(angle - points) * 2);
			double z = size * Math.sin(Math.toRadians(angle - points) * 2);

			consumer.accept(location.clone().add(x, 0, z));
		}
	}

	public static void generateDirectionalCircle(Location location, Vector direction, double radius, int points, Consumer<Location> consumer) {
		for (int i = 0; i < 360; i += points) {
			Vector circle = GeneralMethods.getOrthogonalVector(direction.clone(), i, radius);
			consumer.accept(location.clone().add(circle));
		}
	}

	public static void generateSpirals(Location location, Vector direction, double radius, int spirals, int change, boolean clockwise, Consumer<Location> consumer) {
		int gap = 360 / spirals;
		List<Integer> points = new ArrayList<>();

		for (int i = 0; i < spirals; i++) {
			points.add(i * gap);
		}
		for (int point : points) {
			int newPoint = clockwise ? point + change : point - change;
			if (clockwise && point + change > 360) {
				newPoint = point + change - 360;
			} else if (!clockwise && point - change < 0) {
				newPoint = point - change + 360;
			}
			Vector circle = GeneralMethods.getOrthogonalVector(direction.clone(), newPoint, radius);
			consumer.accept(location.clone().add(circle));
		}
	}

	public static void generateSphere(double size, Location location, Consumer<Location> consumer) {
		for (double i = 0; i <= Math.PI; i += Math.PI / 15) {
			double y = size * Math.cos(i);

			for (double j = 0; j <= 2 * Math.PI; j += Math.PI / 30) {
				double x = size * Math.cos(j) * Math.sin(i);
				double z = size * Math.sin(j) * Math.sin(i);

				location.add(x, y, z);
				consumer.accept(location);
				location.subtract(x, y, z);
			}
		}
	}
	
	public static PotionEffectType[] getPositiveEffects() {
		return new PotionEffectType[] {
				PotionEffectType.ABSORPTION, PotionEffectType.CONDUIT_POWER, PotionEffectType.RESISTANCE,
				PotionEffectType.DOLPHINS_GRACE, PotionEffectType.HASTE, PotionEffectType.FIRE_RESISTANCE, PotionEffectType.HEALTH_BOOST,
				PotionEffectType.HERO_OF_THE_VILLAGE, PotionEffectType.INVISIBILITY, PotionEffectType.STRENGTH, PotionEffectType.JUMP_BOOST,
				PotionEffectType.LUCK, PotionEffectType.NIGHT_VISION, PotionEffectType.REGENERATION, PotionEffectType.SPEED, PotionEffectType.WATER_BREATHING
		};
	}
	
	public static PotionEffectType[] getNegativeEffects() {
		return new PotionEffectType[] {
				PotionEffectType.BAD_OMEN, PotionEffectType.BLINDNESS, PotionEffectType.NAUSEA, PotionEffectType.HUNGER,
				PotionEffectType.POISON, PotionEffectType.SLOWNESS, PotionEffectType.MINING_FATIGUE, PotionEffectType.UNLUCK, PotionEffectType.WEAKNESS,
				PotionEffectType.WITHER
		};
	}

	public static Set<PotionEffectType> getPositiveEffectsSet() {
		return Set.of(getPositiveEffects());
	}

	public static Set<PotionEffectType> getNegativeEffectsSet() {
		return Set.of(getNegativeEffects());
	}

	public static TreeType getTreeType(Material material) {
		switch (material) {
			case ACACIA_SAPLING:
				return TreeType.ACACIA;
			case BIRCH_SAPLING:
				return TreeType.BIRCH;
			case CHERRY_SAPLING:
				return TreeType.CHERRY;
			case DARK_OAK_SAPLING:
				return TreeType.DARK_OAK;
			case JUNGLE_SAPLING:
				return TreeType.SMALL_JUNGLE;
			case MANGROVE_PROPAGULE:
				return TreeType.MANGROVE;
			case OAK_SAPLING:
				return TreeType.TREE;
			case SPRUCE_SAPLING:
				return TreeType.REDWOOD;
		}
		return null;
	}

	public static TreeType getBigTreeType(Material material) {
		switch (material) {
			case BIRCH_SAPLING:
				return TreeType.TALL_BIRCH;
			case JUNGLE_SAPLING:
				return TreeType.JUNGLE;
			case MANGROVE_PROPAGULE:
				return TreeType.TALL_MANGROVE;
			case OAK_SAPLING:
				return TreeType.BIG_TREE;
			case SPRUCE_SAPLING:
				return TreeType.TALL_REDWOOD;
		}
		return null;
	}
}

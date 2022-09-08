package me.pride.spirits.util;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import me.pride.spirits.api.Spirit;
import me.pride.spirits.game.SpiritElement;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;

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
			if (!filter.test(entity)) continue;
			
			boolean light = false, dark = false, neutral = true;
			boolean lightAccept = false, darkAccept = false, neutralAccept = false;
			
			if (entity instanceof LivingEntity) {
				LivingEntity e = (LivingEntity) entity;
				
				if (e instanceof Player) {
					Player player = (Player) e;
					BendingPlayer bendingPlayer = BendingPlayer.getBendingPlayer(player);
					
					if (bendingPlayer.hasElement(SpiritElement.LIGHT_SPIRIT)) {
						light = true;
					} else if (bendingPlayer.hasElement(SpiritElement.DARK_SPIRIT)) {
						dark = true;
					}
				}
				if (Filter.filterEntityLight(e)) {
					light = true;
				} else if (Filter.filterEntityDark(e)) {
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
	
	public static void createCircle(Location location, double size, int points, Consumer<Location> consumer) {
		for (int angle = 180; angle >= 0; angle -= 5) {
			double x = size * Math.cos(Math.toRadians(angle - points) * 2);
			double z = size * Math.sin(Math.toRadians(angle - points) * 2);
			
			consumer.accept(location.clone().add(x, 0, z));
		}
	}
	
	public static PotionEffectType[] getPositiveEffects() {
		return new PotionEffectType[] {
				PotionEffectType.ABSORPTION, PotionEffectType.CONDUIT_POWER, PotionEffectType.DAMAGE_RESISTANCE,
				PotionEffectType.DOLPHINS_GRACE, PotionEffectType.FAST_DIGGING, PotionEffectType.FIRE_RESISTANCE, PotionEffectType.HEALTH_BOOST,
				PotionEffectType.HERO_OF_THE_VILLAGE, PotionEffectType.INVISIBILITY, PotionEffectType.INCREASE_DAMAGE, PotionEffectType.JUMP,
				PotionEffectType.LUCK, PotionEffectType.NIGHT_VISION, PotionEffectType.REGENERATION, PotionEffectType.SPEED, PotionEffectType.WATER_BREATHING
		};
	}
	
	public static PotionEffectType[] getNegativeEffects() {
		return new PotionEffectType[] {
				PotionEffectType.BAD_OMEN, PotionEffectType.BLINDNESS, PotionEffectType.CONFUSION, PotionEffectType.HUNGER,
				PotionEffectType.POISON, PotionEffectType.SLOW, PotionEffectType.SLOW_DIGGING, PotionEffectType.UNLUCK, PotionEffectType.WEAKNESS,
				PotionEffectType.WITHER
		};
	}
}

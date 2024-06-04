package me.pride.spirits.abilities.light.passives;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.attribute.AttributeModifier;
import com.projectkorra.projectkorra.region.RegionProtection;
import me.pride.spirits.Spirits;
import me.pride.spirits.api.LightSpirit;
import me.pride.spirits.api.ability.LightSpiritAbility;
import me.pride.spirits.api.ability.SpiritElement;
import me.pride.spirits.util.Filter;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Lightborn extends LightSpiritAbility implements AddonAbility, PassiveAbility {
	private final String path = Tools.path(this, Path.PASSIVES);

	public static final Map<UUID, Double> LIGHTS = new HashMap<>();

	@Attribute("Chance")
	private double shedChance;
	private int shedRate;
	@Attribute("Amplifier")
	private double amplifier;
	private boolean shed;
	private boolean amplify;

	private int rate;
	private int hits;

	public Lightborn(Player player) {
		super(player);

		if (CoreAbility.hasAbility(player, Lightborn.class)) {
			return;
		}
		this.shedChance = Spirits.instance.getConfig().getDouble(path + "ShedChance");
		this.shedRate = Spirits.instance.getConfig().getInt(path + "ShedRate");
		this.amplifier = Spirits.instance.getConfig().getDouble(path + "Amplifier");
		this.shed = Spirits.instance.getConfig().getBoolean(path + "Shed");
		this.amplify = Spirits.instance.getConfig().getBoolean(path + "Amplify");

		LIGHTS.put(player.getUniqueId(), 0.0);

		start();
	}
	
	@Override
	public void progress() {
		if (!RegionProtection.isRegionProtected(player, player.getLocation(), this)) {
			if (!bPlayer.isToggled()) {
				if (LIGHTS.containsKey(player.getUniqueId())) {
					LIGHTS.remove(player.getUniqueId());
				}
				// LIGHTS.computeIfPresent(player.getUniqueId(), (k, v) -> LIGHTS.remove(player.getUniqueId()));
			}
			if (LIGHTS.containsKey(player.getUniqueId()) && LIGHTS.get(player.getUniqueId()) < 100.0) {
				LIGHTS.put(player.getUniqueId(), LIGHTS.get(player.getUniqueId()) + 0.1);
			}
			/*
			 * Every rate, a light spirit will have a chance to shed their negative potion effects.
			 */
			if (shed) {
				rate = rate > shedRate ? 0 : rate + 1;
				if (rate == 0) {
					if (ThreadLocalRandom.current().nextInt(100) < shedChance) {
						for (PotionEffect active : player.getActivePotionEffects()) {
							if (Tools.getNegativeEffectsSet().contains(active.getType())) {
								player.removePotionEffect(active.getType());
								break;
							}
						}
					}
				}
			}

			/*
			 * Amplify the damage of light spirit abilities during the day.
			 */
			if (amplify) {
				if (isDay(player.getWorld())) {
					try {
						for (CoreAbility ability : CoreAbility.getAbilitiesByElement(SpiritElement.LIGHT_SPIRIT)) {
							for (Field field : ability.getClass().getDeclaredFields()) {
								if (field.isAnnotationPresent(Attribute.class)) {
									String attribute = field.getAnnotation(Attribute.class).value();

									if (attribute.equalsIgnoreCase("damage")) {
										try {
											ability.addAttributeModifier(attribute, amplifier, AttributeModifier.MULTIPLICATION);
										} catch (Exception e) {
										}
									}
								}
							}
						}
					} catch (Exception e) {
					}
				}
			}

			/*
			 * Give regeneration to nearby light or passive entities.
			 */
			Tools.trackEntitySpirit(player.getLocation(), 1.25, e -> Filter.filterGeneralEntity(e, player, this), (entity, light, dark, neutral) -> {
				if (light) {
					PotionEffectType.REGENERATION.createEffect(30, 1).apply(entity);
				}
			});
		}
	}

	public void addHit(int hit) {
		this.hits += hit;
	}

	public static void addHit(Player player, int hit) {
		if (CoreAbility.hasAbility(player, Lightborn.class)) {
			CoreAbility.getAbility(player, Lightborn.class).addHit(hit);
		}
	}

	public int getHit() {
		return hits;
	}

	public static int getHit(Player player) {
		if (CoreAbility.hasAbility(player, Lightborn.class)) {
			return CoreAbility.getAbility(player, Lightborn.class).getHit();
		}
		return 0;
	}

	public void setHit(int hit) {
		this.hits = hit;
	}

	public static void setHit(Player player, int hit) {
		if (CoreAbility.hasAbility(player, Lightborn.class)) {
			CoreAbility.getAbility(player, Lightborn.class).setHit(hit);
		}
	}
	
	@Override
	public boolean isSneakAbility() {
		return false;
	}
	
	@Override
	public boolean isHarmlessAbility() {
		return false;
	}
	
	@Override
	public long getCooldown() {
		return 0;
	}
	
	@Override
	public String getName() {
		return "Lightborn";
	}
	
	@Override
	public Location getLocation() {
		return null;
	}
	
	@Override
	public void load() { }
	
	@Override
	public void stop() { }
	
	@Override
	public String getAuthor() {
		return Spirits.getAuthor(this.getElement());
	}
	
	@Override
	public String getVersion() {
		return Spirits.getVersion();
	}

	@Override
	public String getDescription() {
		return "As a being of light, you have a regenerative aura, you are able to heal quicker, you shed negative potion effects over time, your attacks are greater in the day, and you are able to bleed pure light that heals any non-hostile entity after having been damaged. However, you are more vulnerable to all attacks.";
	}
	
	@Override
	public boolean isInstantiable() {
		return true;
	}
	
	@Override
	public boolean isProgressable() {
		return true;
	}
}

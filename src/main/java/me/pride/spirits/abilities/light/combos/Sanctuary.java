package me.pride.spirits.abilities.light.combos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import me.pride.spirits.Spirits;
import me.pride.spirits.api.ability.LightSpiritAbility;
import me.pride.spirits.util.Filter;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempPotionEffect;

public class Sanctuary extends LightSpiritAbility implements AddonAbility, ComboAbility {
	private final String path = Tools.path(this, Path.COMBOS);
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.RADIUS)
	private double max_size;
	private double increment;
	private int max_pulses;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.KNOCKBACK)
	private double repel;
	@Attribute("EffectDuration")
	private int res_duration;
	@Attribute("EffectAmplifier")
	private int res_amplifier;
	
	private double size;
	private int pulses;
	
	private Location[] origins = new Location[4];
	private List<Location> locations = new ArrayList<>();
	
	public Sanctuary(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		} else if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, player.getLocation())) {
			return;
		}
		cooldown = Spirits.instance.getConfig().getLong(path + "Cooldown");
		max_size = Spirits.instance.getConfig().getDouble(path + "MaxSize");
		increment = Spirits.instance.getConfig().getDouble(path + "SizeIncrement");
		max_pulses = Spirits.instance.getConfig().getInt(path + "MaxPulses");
		damage = Spirits.instance.getConfig().getDouble(path + "Damage");
		repel = Spirits.instance.getConfig().getDouble(path + "Repel");
		res_duration = Spirits.instance.getConfig().getInt(path + "Resistance.Duration");
		res_amplifier = Spirits.instance.getConfig().getInt(path + "Resistance.Amplifier");
		
		for (int i = 0; i <= 3; i++) {
			origins[i] = player.getLocation().clone().add(0, i, 0);
		}
		bPlayer.addCooldown(this);
		start();
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}
		size += increment;
		
		if (size > max_size) {
			pulses++;
			if (pulses >= max_pulses) {
				remove();
				return;
			}
			size = 0;
		}
		for (Location location : origins) {
			Tools.createCircle(location, size, 360, l -> {
				if (ThreadLocalRandom.current().nextInt(60) == 0) {
					player.getWorld().spawnParticle(Particle.GLOW, l, 2, 0.2, 0.2, 0.2, 0.02);
				}
				player.getWorld().spawnParticle(Particle.CRIT_MAGIC, l, 2, 0.2, 0.2, 0.2, 0);
				locations.add(l);
				
				Tools.trackEntitySpirit(l, 1, e -> Filter.filterEntityFromAbility(e, player, this), (entity, light, dark, neutral) -> {
					if (GeneralMethods.locationEqualsIgnoreDirection(l, entity.getLocation())) return;
					
					if (light) {
						new TempPotionEffect(entity, new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Math.toIntExact((res_duration * 1000) / 50), res_amplifier));
					} else if (dark) {
						DamageHandler.damageEntity(entity, damage, this);
						GeneralMethods.setVelocity(this, entity, entity.getVelocity().add(GeneralMethods.getDirection(l, entity.getLocation()).multiply(repel)).setY(0.5));
					} else if (neutral) {
						GeneralMethods.setVelocity(this, entity, entity.getVelocity().add(GeneralMethods.getDirection(l, entity.getLocation()).multiply(repel)).setY(0.5));
					}
				});
			});
		}
		locations.clear();
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
	public boolean isIgniteAbility() {
		return false;
	}
	
	@Override
	public boolean isExplosiveAbility() {
		return false;
	}
	
	@Override
	public long getCooldown() {
		return cooldown;
	}
	
	@Override
	public String getName() {
		return "Sanctuary";
	}
	
	@Override
	public Location getLocation() {
		return null;
	}
	
	@Override
	public List<Location> getLocations() {
		return locations;
	}
	
	@Override
	public void load() { }
	
	@Override
	public void stop() { }
	
	@Override
	public boolean isEnabled() {
		return Spirits.instance.getConfig().getBoolean("Light.Combos.Sanctuary.Enabled", true);
	}
	
	@Override
	public String getAuthor() {
		return Spirits.getAuthor(this.getElement());
	}
	
	@Override
	public String getVersion() {
		return Spirits.getVersion(this.getElement());
	}
	
	@Override
	public String getDescription() {
		return Tools.getOppositeColor(this.getElement()) + Spirits.instance.getConfig().getString(path + "Description");
	}
	
	@Override
	public String getInstructions() {
		return ChatColor.GOLD + Spirits.instance.getConfig().getString(path + "Instructions");
	}
	
	@Override
	public Object createNewComboInstance(Player player) {
		return new Sanctuary(player);
	}
	
	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> info = new ArrayList<>();
		info.add(new AbilityInformation("Alleviate", ClickType.SHIFT_DOWN));
		info.add(new AbilityInformation("Blessing", ClickType.SHIFT_UP));
		info.add(new AbilityInformation("Blessing", ClickType.LEFT_CLICK));
		info.add(new AbilityInformation("Alleviate", ClickType.SHIFT_DOWN));
		return info;
	}
}
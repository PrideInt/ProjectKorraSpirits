package me.pride.spirits.abilities.light.combos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.projectkorra.projectkorra.region.RegionProtection;
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
	private double maxSize;
	private double increment;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.KNOCKBACK)
	private double repel;
	@Attribute("EffectDuration")
	private int resDuration;
	@Attribute("EffectAmplifier")
	private int resAmplifier;
	private int maxPulses;
	@Attribute(Attribute.HEIGHT)
	private int height;
	
	private double size;
	private int pulses;
	
	private Location[] origins;
	private List<Location> locations;
	
	public Sanctuary(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		} else if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (RegionProtection.isRegionProtected(player, player.getLocation(), this)) {
			return;
		}
		this.cooldown = Spirits.instance.getConfig().getLong(path + "Cooldown");
		this.maxSize = Spirits.instance.getConfig().getDouble(path + "MaxSize");
		this.increment = Spirits.instance.getConfig().getDouble(path + "SizeIncrement");
		this.damage = Spirits.instance.getConfig().getDouble(path + "Damage");
		this.repel = Spirits.instance.getConfig().getDouble(path + "Repel");
		this.resDuration = Spirits.instance.getConfig().getInt(path + "Resistance.Duration");
		this.resAmplifier = Spirits.instance.getConfig().getInt(path + "Resistance.Amplifier");
		this.maxPulses = Spirits.instance.getConfig().getInt(path + "MaxPulses");
		this.height = Spirits.instance.getConfig().getInt(path + "Height");

		this.origins = new Location[this.height];
		this.locations = new ArrayList<>();

		for (int i = 0; i <= 3; i++) {
			this.origins[i] = player.getLocation().clone().add(0, i, 0);
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
		size = size > maxSize ? 0 : size + increment;
		
		if (size == 0) {
			pulses++;
			if (pulses >= maxPulses) {
				remove();
				return;
			}
		}
		for (Location location : origins) {
			Tools.createCircle(location, size, 360, l -> {
				if (ThreadLocalRandom.current().nextInt(60) == 0) {
					player.getWorld().spawnParticle(Particle.GLOW, l, 2, 0.2, 0.2, 0.2, 0.02);
				}
				player.getWorld().spawnParticle(Particle.CRIT_MAGIC, l, 2, 0.2, 0.2, 0.2, 0);
				locations.add(l);
				
				Tools.trackEntitySpirit(l, 1, e -> Filter.filterEntityFromAbility(e, player, this), (entity, light, dark, neutral) -> {
					if (GeneralMethods.locationEqualsIgnoreDirection(l, entity.getLocation())) {
						return;
					}
					if (light) {
						new TempPotionEffect(entity, new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, resDuration, resAmplifier));
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
	public boolean isEnabled() {
		return Spirits.instance.getConfig().getBoolean("Light.Combos.Sanctuary.Enabled");
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
	public String getAuthor() {
		return Spirits.getAuthor(this.getElement());
	}
	
	@Override
	public String getVersion() {
		return Spirits.getVersion();
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
	public void load() { }

	@Override
	public void stop() { }

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
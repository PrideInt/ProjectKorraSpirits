package me.pride.spirits.abilities.light.passives.other;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.DamageHandler;
import me.pride.spirits.Spirits;
import me.pride.spirits.abilities.light.passives.Lightborn;
import me.pride.spirits.api.ability.LightSpiritAbility;
import me.pride.spirits.util.Filter;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class LightBlood extends LightSpiritAbility implements AddonAbility {
	private final String path = Tools.path(CoreAbility.getAbility(Lightborn.class), Path.PASSIVES) + "Bleed.";

	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute("Heal")
	private double heal;

	private Location location;

	public LightBlood(Player player) {
		super(player);

		if (RegionProtection.isRegionProtected(player, player.getLocation(), this)) {
			return;
		}
		this.duration = Spirits.instance.getConfig().getLong(path + "Duration");
		this.damage = Spirits.instance.getConfig().getDouble(path + "Damage");
		this.heal = Spirits.instance.getConfig().getDouble(path + "Heal");

		this.location = player.getLocation().clone();

		start();
	}

	@Override
	public void progress() {
		if (System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		location.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, location, 1, 1, 1, 1, 0);
		// location.getWorld().spawnParticle(Particle.SPELL_INSTANT, location, 1, 0, 0, 0, 0);
		location.getWorld().spawnParticle(Particle.GLOW, location, 1, 0, 0, 0, 0);

		Tools.trackEntitySpirit(location, 1.25, e -> Filter.filterGeneralEntity(e, player, this), (entity, light, dark, neutral) -> {
			if (light || neutral) {
				double heal = entity.getHealth() + this.heal;

				if (heal <= entity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()) {
					entity.setHealth(heal);
				}
			} else if (dark) {
				DamageHandler.damageEntity(entity, damage, this);
			}
			remove();
			return;
		});
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
		return "LightBlood";
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public boolean isHiddenAbility() {
		return true;
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
	public void load() { }

	@Override
	public void stop() { }
}

package me.pride.spirits.abilities.dark.passives;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import me.pride.spirits.Spirits;
import me.pride.spirits.api.Spirit;
import me.pride.spirits.api.ability.DarkSpiritAbility;
import me.pride.spirits.api.builder.SpiritBuilder;
import me.pride.spirits.util.Filter;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Rancor extends DarkSpiritAbility implements AddonAbility, PassiveAbility {
	private final String path = Tools.path(this, Path.PASSIVES);

	private double radius;
	private double lossRange;

	private Set<Spirit> spirits;
	private HashMap<Entity, BukkitTask> timers;

	public Rancor(Player player) {
		super(player);

		if (!bPlayer.canBendPassive(this)) {
			return;
		}
		this.radius = Spirits.instance.getConfig().getDouble(path + "Radius");
		this.lossRange = Spirits.instance.getConfig().getDouble(path + "LossRange");

		this.spirits = new HashSet<>();
		this.timers = new HashMap<>();

		start();
	}

	@Override
	public void progress() {
		Tools.trackEntitySpirit(player.getLocation(), radius, e -> Filter.filterGeneralEntity(e, player, this), (entity, light, dark, neutral) -> {
			if (entity.getType() != EntityType.PLAYER) {
				if (!dark) {
					if (!timers.containsKey(entity)) {
						BukkitTask task = new BukkitRunnable() {
							@Override
							public void run() {
								spirits.add(
										SpiritBuilder.dark()
												.spawn(player.getWorld(), entity.getLocation())
												.revertTime(10000)
												.replace(entity)
												.build());

								timers.remove(entity);
								cancel();
							}
						}.runTaskLater(ProjectKorra.plugin, 60);

						timers.put(entity, task);
					}
					entity.getWorld().spawnParticle(Particle.SMOKE_NORMAL, entity.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);
				}
			}
		});
		timers.keySet().removeIf(e -> {
			if (e.isDead() || e.getLocation().distanceSquared(player.getLocation()) > lossRange * lossRange) {
				timers.get(e).cancel();
				return true;
			}
			return false;
		});
	}

	@Override
	public boolean isEnabled() {
		return Spirits.instance.getConfig().getBoolean("Dark.Passives.Rancor.Enabled");
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
		return "Rancor";
	}

	@Override
	public Location getLocation() {
		return null;
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
	public boolean isInstantiable() {
		return true;
	}

	@Override
	public boolean isProgressable() {
		return true;
	}

	@Override
	public void load() { }

	@Override
	public void stop() { }
}

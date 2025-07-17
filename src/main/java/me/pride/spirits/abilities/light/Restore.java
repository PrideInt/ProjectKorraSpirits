package me.pride.spirits.abilities.light;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.DamageHandler;
import me.pride.spirits.Spirits;
import me.pride.spirits.abilities.light.passives.Orbs;
import me.pride.spirits.abilities.light.passives.Orbs.Orb;
import me.pride.spirits.api.DarkSpirit;
import me.pride.spirits.api.Spirit;
import me.pride.spirits.api.ability.LightSpiritAbility;
import me.pride.spirits.util.Filter;
import me.pride.spirits.util.Keys;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Restore extends LightSpiritAbility implements AddonAbility {
	private final String path = Tools.path(this, Path.ABILITIES);

	private enum RestoreForm {
		NONE, SELF, TARGET, SOURCING, SOURCED, ITEMS
	}
	private RestoreForm restoreForm;

	@Attribute(Attribute.COOLDOWN)
	private long cooldown, maxCooldown;
	@Attribute(Attribute.SELECT_RANGE)
	private double selectRange;
	@Attribute("Heal")
	private double restore;
	@Attribute("Rate")
	private int restoreRate;
	private int restoreDurability;
	private boolean healthFlashAnimation;
	private boolean enhanceItems;

	private int rate;

	private int generateParticleRate;
	private int particleRate;

	private LivingEntity target;
	private Set<RestoreParticles> particles;
	private static final Map<Material, Material> RESTORABLES = Map.of(
			Material.APPLE, Material.GOLDEN_APPLE,
			Material.CARROT, Material.GOLDEN_CARROT
	);

	public Restore(Player player) {
		super(player);

		if (!bPlayer.canBend(this)) {
			return;
		} else if (hasAbility(player, Restore.class)) {
			return;
		} else if (RegionProtection.isRegionProtected(player, player.getLocation(), this)) {
			return;
		}
		this.maxCooldown = Spirits.instance.getConfig().getLong(path + "MaxCooldown");
		this.selectRange = Spirits.instance.getConfig().getDouble(path + "SelectRange");
		this.restore = Spirits.instance.getConfig().getDouble(path + "Restore");
		this.restoreRate = Spirits.instance.getConfig().getInt(path + "RestoreRate");
		this.restoreDurability = Spirits.instance.getConfig().getInt(path + "RestoreDurability");
		this.healthFlashAnimation = Spirits.instance.getConfig().getBoolean(path + "HealthFlashAnimation");
		this.enhanceItems = Spirits.instance.getConfig().getBoolean(path + "EnhanceItems");

		this.target = player;
		this.restoreForm = RestoreForm.NONE;

		this.particles = new HashSet<>();

		if (player.getInventory().getItemInMainHand() != null || player.getInventory().getItemInOffHand() != null) {
			ItemStack item = player.getInventory().getItemInMainHand();
			if (item == null) {
				item = player.getInventory().getItemInOffHand();
			}
			if (item.getItemMeta() instanceof Damageable && ((Damageable) item.getItemMeta()).hasDamage()) {
				this.restoreForm = RestoreForm.ITEMS;
			} else if (this.enhanceItems && RESTORABLES.containsKey(item.getType())) {
				this.restoreForm = RestoreForm.ITEMS;
			}
		}
		if (this.restoreForm == RestoreForm.NONE) {
			Entity entity = Tools.rayTraceEntity(player, this.selectRange);
			if (entity != null) {
				if (entity instanceof LivingEntity && Filter.filterGeneralEntity(entity, player, this)) {
					this.restoreForm = RestoreForm.TARGET;
					this.target = (LivingEntity) entity;

					Orbs.absorb(player, this.target);
				}
			}
		}
		if (this.restoreForm == RestoreForm.NONE) {
			Block block = Tools.rayTraceBlock(player, this.selectRange);
			if (block != null) {
				if (block.hasMetadata(Keys.BLESSED_SOURCE) || Filter.filterFlowers(block)) {
					this.restoreForm = RestoreForm.SOURCING;
					for (int i = 0; i < 5; i++) {
						double x, z;
						x = ThreadLocalRandom.current().nextDouble(-0.5, 0.5);
						z = ThreadLocalRandom.current().nextDouble(-0.5, 0.5);

						this.particles.add(new RestoreParticles(block.getLocation().clone().add(x, 0.5, z), player.getLocation().clone().add(0, 1, 0)));
					}
				}
			}
		}
		if (this.restoreForm == RestoreForm.NONE) {
			this.restoreForm = RestoreForm.SELF;

			Orbs.absorb(player, player);
		}
		player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RETURN, 1, 1);

		start();
	}

	@Override
	public void progress() {
		if (!player.isSneaking() || (target != null && (target.isDead() || target instanceof Player playerTarget && !playerTarget.isOnline()))) {
			Orbs.unabsorb(player);
			remove();
			return;
		}
		switch (restoreForm) {
			case SELF:
				restoreSelf();
				break;
			case TARGET:
				restoreTarget();
				break;
			case SOURCING, SOURCED:
				restoreSource();
				break;
			case ITEMS:
				restoreItems();
				break;
		}
	}

	private void playEffects() {
		if (ThreadLocalRandom.current().nextInt(10) == 0) {
			player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, 0.3F, 0.25F);
		}
		if (generateParticleRate == 0) {
			generateParticleRate = ThreadLocalRandom.current().nextInt(8, 20);
		}
		particleRate = particleRate > generateParticleRate ? 0 : particleRate + 1;

		if (particleRate == 0) {
			generateParticleRate = 0;
			for (int i = 0; i < 3; i++) {
				particles.add(new RestoreParticles(target.getLocation().clone().add(0, 1, 0)));
			}
		}
		particles.removeIf(particle -> !particle.handle());
	}

	private void rateIntervals() {
		rate = rate > restoreRate ? 0 : rate + 1;

		if (rate == 0) {
			if (cooldown + 500 > maxCooldown) {
				cooldown = maxCooldown;
			} else {
				cooldown += 500;
			}
		}
	}

	private void restore(double restore) {
		double health = target.getHealth();

		if (target.getHealth() + 1 <= target.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue()) {
			if (healthFlashAnimation && target.getType() == EntityType.PLAYER) {
				PotionEffectType.INSTANT_HEALTH.createEffect(1, 0).apply(target);

				new BukkitRunnable() {
					@Override
					public void run() {
						target.setHealth(health + restore);
						cancel();
					}
				}.runTaskLater(Spirits.instance, 1);
			} else {
				target.setHealth(health + restore);
			}
		}
	}

	private void restore() {
		restore(restore);
	}

	private void restoreSelf(double restore) {
		rateIntervals();
		if (rate == 0) {
			restore(restore);
		}
		playEffects();
	}

	private void restoreSelf() {
		restoreSelf(restore);
	}

	private void restoreTarget() {
		rateIntervals();
		if (rate == 0) {
			if (DarkSpirit.isDarkSpirit(target)) {
				DamageHandler.damageEntity(target, restore, this);
			} else {
				restore();
			}
		}
		playEffects();
	}

	private void restoreSource() {
		if (restoreForm == RestoreForm.SOURCING) {
			for (Iterator<RestoreParticles> itr = particles.iterator(); itr.hasNext();) {
				RestoreParticles particle = itr.next();

				particle.progress();
				particle.setVector(GeneralMethods.getDirection(particle.getLocation(), player.getLocation().clone().add(0, 1, 0)).normalize().multiply(0.25));

				if (particle.getLocation().distanceSquared(player.getLocation().clone().add(0, 1, 0)) < 0.5) {
					itr.remove();
					restoreForm = RestoreForm.SOURCED;
				}
			}
		} else if (restoreForm == RestoreForm.SOURCED) {
			restoreSelf(restore * 1.5);
		}
	}

	private void restoreItems() {
		playEffects();

		rateIntervals();
		if (rate == 0) {
			ItemStack item = player.getInventory().getItemInMainHand();
			if (item == null) {
				item = player.getInventory().getItemInOffHand();
			}
			if (item != null) {
				if (item.getItemMeta() instanceof Damageable) {
					Damageable damageable = (Damageable) item.getItemMeta();

					if (damageable.hasDamage()) {
						if (damageable.getDamage() - restoreDurability < 0) {
							damageable.setDamage(0);
						} else {
							damageable.setDamage(damageable.getDamage() - restoreDurability);
						}
						item.setItemMeta(damageable);
					}
				} else if (RESTORABLES.containsKey(item.getType())) {
					item.setType(RESTORABLES.get(item.getType()));
				} else {
					remove();
					return;
				}
			} else {
				remove();
				return;
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return Spirits.instance.getConfig().getBoolean("Light.Abilities.Restore.Enabled");
	}

	@Override
	public boolean isSneakAbility() {
		return true;
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
		return "Restore";
	}

	@Override
	public void remove() {
		bPlayer.addCooldown(this);
		super.remove();
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
	public void load() { }

	@Override
	public void stop() { }

	class RestoreParticles {
		private Location origin, destination;
		private Particle particle;

		private double delta;

		private Location location;
		private Vector vector;

		public RestoreParticles(Location origin, Location destination, Particle particle) {
			this.origin = origin;
			this.destination = destination;
			this.particle = particle;

			this.vector = GeneralMethods.getDirection(origin, destination).normalize().multiply(0.1);

			this.delta = this.origin.distanceSquared(this.destination);

			this.location = this.origin.clone();

			this.location.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, location, 1, 0, 0, 0, 0);
		}
		public RestoreParticles(Location destination, Particle particle) {
			this(destination.clone().add(ThreadLocalRandom.current().nextDouble(-1.0, 1.0), ThreadLocalRandom.current().nextDouble(-1.0, 2.0), ThreadLocalRandom.current().nextDouble(-1.0, 1.0)), destination, particle);
		}
		public RestoreParticles(Location origin, Location destination) {
			this(origin, destination, Particle.GLOW);
		}
		/*
		public RestoreParticles(Location origin, Particle particle) {
			this.origin = origin;
			this.particle = particle;

			double x, y, z;
			x = ThreadLocalRandom.current().nextDouble(-1.0, 1.0);
			y = ThreadLocalRandom.current().nextDouble(-1.0, 2.0);
			z = ThreadLocalRandom.current().nextDouble(-1.0, 1.0);

			this.destination = origin.clone().add(x, y, z);
			this.vector = GeneralMethods.getDirection(origin, destination).normalize().multiply(0.1);

			this.delta = this.origin.distanceSquared(this.destination);

			this.location = this.origin.clone();
		}
		 */
		public RestoreParticles(Location origin) {
			this(origin, Particle.GLOW);
		}
		public boolean handle() {
			if (location.distanceSquared(origin) > delta) {
				return false;
			}
			location.add(vector);
			location.getWorld().spawnParticle(particle, location, 1, 0, 0, 0, 0);
			return true;
		}
		public void progress() {
			location.add(vector);
			location.getWorld().spawnParticle(particle, location, 1, 0, 0, 0, 0);
		}
		public Location getLocation() {
			return location;
		}
		public Location getOrigin() {
			return origin;
		}
		public Location getDestination() {
			return destination;
		}
		public Vector getVector() {
			return vector;
		}
		public void setLocation(Location location) {
			this.location = location;
		}
		public void setOrigin(Location origin) {
			this.origin = origin;
		}
		public void setDestination(Location destination) {
			this.destination = destination;
		}
		public void setVector(Vector vector) {
			this.vector = vector;
		}
	}
}

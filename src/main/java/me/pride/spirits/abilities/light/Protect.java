package me.pride.spirits.abilities.light;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.airbending.AirBlast;
import com.projectkorra.projectkorra.airbending.AirSwipe;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.earthbending.EarthBlast;
import com.projectkorra.projectkorra.earthbending.metal.MetalClips;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.firebending.combo.FireKick;
import com.projectkorra.projectkorra.firebending.combo.FireSpin;
import com.projectkorra.projectkorra.firebending.combo.FireWheel;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;
import com.projectkorra.projectkorra.waterbending.ice.IceBlast;
import com.projectkorra.projectkorra.waterbending.ice.IceSpikeBlast;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArmsFreeze;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArmsWhip;
import me.pride.spirits.Spirits;
import me.pride.spirits.abilities.light.passives.Orbs;
import me.pride.spirits.api.ability.LightSpiritAbility;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import me.pride.spirits.util.objects.TetraConsumer;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Protect extends LightSpiritAbility implements AddonAbility {
	private final String path = Tools.path(this, Path.ABILITIES);

	private static final Map<Player, Double> STOCKPILE = new HashMap<>();

	public enum ProtectType {
		PROTECT, DEFLECT
	}
	private ProtectType type;

	// Both
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;

	// Deflect
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.KNOCKBACK)
	private double knockback;
	@Attribute(Attribute.RANGE)
	private double minRange, range, maxRange;
	@Attribute(Attribute.RADIUS)
	private double size, maxSize;

	// Protect
	private int slowAmplifier;

	private boolean validated;

	private double sizeIncrease;
	private int time;
	
	private Location origin, location;
	private Vector direction;

	public Protect(Player player) {
		super(player);

		if (!bPlayer.canBend(this)) {
			return;
		} else if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (CoreAbility.hasAbility(player, Protect.class)) {
			return;
		} else if (RegionProtection.isRegionProtected(player, player.getLocation(), this)) {
			return;
		}
		if (!STOCKPILE.containsKey(player)) {
			STOCKPILE.put(player, 1.0);
		}
		this.origin = player.getLocation().clone().add(0, 1, 0);
		this.location = this.origin.clone();
		this.validated = true;
	}
	
	public Protect(Player player, ProtectType type) {
		this(player);

		if (!this.validated) {
			return;
		}
		if (type == ProtectType.DEFLECT) {
			this.cooldown = Spirits.instance.getConfig().getLong(path + "Deflect.Cooldown");
			this.speed = Spirits.instance.getConfig().getDouble(path + "Deflect.Speed");
			this.damage = Spirits.instance.getConfig().getDouble(path + "Deflect.Damage");
			this.knockback = Spirits.instance.getConfig().getDouble(path + "Deflect.Knockback");
			this.minRange = Spirits.instance.getConfig().getDouble(path + "Deflect.MinRange");
			this.maxRange = Spirits.instance.getConfig().getDouble(path + "Deflect.MaxRange");
			this.maxSize = Spirits.instance.getConfig().getDouble(path + "Deflect.MaxSize");

			this.range = ThreadLocalRandom.current().nextDouble(this.minRange, this.maxRange);
			this.sizeIncrease = ThreadLocalRandom.current().nextDouble(0.25, 0.4);

			this.direction = this.location.getDirection();

			Orbs.shootAll(player, this.range);
		} else if (type == ProtectType.PROTECT) {
			this.cooldown = Spirits.instance.getConfig().getLong(path + "Protect.Cooldown");
			this.slowAmplifier = Spirits.instance.getConfig().getInt(path + "Protect.SlowAmplifier");
		}
		this.type = type;

		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 0.5F, 1.75F);

		start();
	}

	/**
	 * Stockpiled deflect blast
	 *
	 * @param player
	 * @param origin
	 * @param range
	 * @param damage
	 * @param knockback
	 * @param maxSize
	 */
	public Protect(Player player, Location origin, double range, double damage, double knockback, double maxSize) {
		this(player);

		if (!bPlayer.canBend(this)) {
			return;
		} else if (RegionProtection.isRegionProtected(player, player.getLocation(), this)) {
			return;
		}
		this.cooldown = Spirits.instance.getConfig().getLong(path + "Deflect.Stockpile.Cooldown");
		this.speed = Spirits.instance.getConfig().getDouble(path + "Deflect.Speed");

		this.damage = damage;
		this.knockback = knockback;
		this.range = range;
		this.maxSize = maxSize;

		this.sizeIncrease = ThreadLocalRandom.current().nextDouble(0.5, 1.0);

		this.origin = origin.clone();
		this.location = this.origin.clone();
		this.direction = location.getDirection();

		this.type = ProtectType.DEFLECT;

		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 0.5F, 1.75F);

		applyCollisions();

		start();
	}
	
	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		} else if (RegionProtection.isRegionProtected(player, location, this)) {
			remove();
			return;
		}
		if (type == ProtectType.DEFLECT) {
			deflect();
		} else if (type == ProtectType.PROTECT) {
			protect();
		}
	}

	private void deflect() {
		if (origin.distanceSquared(location) > range * range) {
			remove();
			return;
		}
		location.add(direction.multiply(speed));
		size = size >= maxSize ? maxSize : size + sizeIncrease;

		Tools.generateDirectionalCircle(location, location.getDirection(), size, 8, l -> {
			if (ThreadLocalRandom.current().nextInt(5) == 0) {
				l.getWorld().spawnParticle(Particle.GLOW, l, 1, 0.05, 0.05, 0.05, 0);
			}
		});
		location.getWorld().spawnParticle(Particle.FLASH, location, 1, 0.25, 0.25, 0.25, 0);

		Tools.trackEntitySpirit(location, size / 1.5, e -> e.getUniqueId() != player.getUniqueId(), (entity, light, dark, neutral) -> {
			if (dark || neutral) {
				if (dark) {
					DamageHandler.damageEntity(entity, damage, this);
				}
				entity.setVelocity(player.getEyeLocation().getDirection().multiply(knockback));
				new HorizontalVelocityTracker(entity, player, 0, this);
			}
		});
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, size)) {
			if (entity instanceof Projectile) {
				Projectile projectile = (Projectile) entity;

				projectile.setVelocity(player.getEyeLocation().getDirection().multiply(knockback));
			}
		}
	}

	private void protect() {
		if (!player.isSneaking()) {
			remove();
			return;
		} else if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		}
		time = time >= 15 ? 0 : time + 1;

		if (time == 15) {
			double x = ThreadLocalRandom.current().nextDouble(-2, 2);
			double y = ThreadLocalRandom.current().nextDouble(0.5, 2);
			double z = ThreadLocalRandom.current().nextDouble(-2, 2);

			player.getWorld().spawnParticle(Particle.SONIC_BOOM, player.getLocation().clone().add(x, y, z), 1, 0.25, 0.25, 0.25, 0);
		}
		PotionEffectType.SLOWNESS.createEffect(10, slowAmplifier).apply(player);
	}

	public static void removeWithoutCooldown(Player player) {
		if (CoreAbility.hasAbility(player, Protect.class)) {
			CoreAbility.getAbility(player, Protect.class).removeWithoutCooldown();
		}
	}

	public static boolean isProtecting(Player player) {
		return CoreAbility.hasAbility(player, Protect.class) && CoreAbility.getAbility(player, Protect.class).getType() == ProtectType.PROTECT;
	}

	public static boolean isDeflecting(Player player) {
		return CoreAbility.hasAbility(player, Protect.class) && CoreAbility.getAbility(player, Protect.class).getType() == ProtectType.DEFLECT;
	}

	public static void stockpile(Player player, double stockpile) {
		if (CoreAbility.hasAbility(player, Protect.class)) {
			CoreAbility.getAbility(player, Protect.class).stockpile(stockpile);
		}
	}

	public static void setStockpile(Player player, double stockpile) {
		if (CoreAbility.hasAbility(player, Protect.class)) {
			CoreAbility.getAbility(player, Protect.class).setStockpile(stockpile);
		}
	}

	public static double getStockpile(Player player) {
		return STOCKPILE.get(player);
	}

	public void stockpile(double stockpile) {
		if (STOCKPILE.get(player) > 2.5) {
			return;
		} else if (stockpile > 2.5) {
			STOCKPILE.put(player, 2.5);
			return;
		}
		STOCKPILE.put(player, stockpile);
	}

	public void setStockpile(double stockpile) {
		STOCKPILE.put(player, stockpile);
	}

	public static void getStockpiles(Player player, TetraConsumer<Double, Double, Double, Double> consumer) {
		double stockpile = Protect.getStockpile(player);

		double minRange = Spirits.instance.getConfig().getDouble("Light.Abilities.Protect.Deflect.MinRange");
		double maxRange = Spirits.instance.getConfig().getDouble("Light.Abilities.Protect.Deflect.MaxRange");
		double range = ThreadLocalRandom.current().nextDouble(minRange + stockpile, maxRange + stockpile) + stockpile;

		double damage = Spirits.instance.getConfig().getDouble("Light.Abilities.Protect.Deflect.Damage") * stockpile;
		double knockback = Spirits.instance.getConfig().getDouble("Light.Abilities.Protect.Deflect.Knockback") * stockpile;
		double maxSize = Spirits.instance.getConfig().getDouble("Light.Abilities.Protect.Deflect.MaxSize") * stockpile;

		consumer.accept(range, damage, knockback, maxSize);
	}

	public ProtectType getType() {
		return type;
	}

	private void applyCollisions() {
		CoreAbility main = CoreAbility.getAbility(Protect.class);

		CoreAbility[] abilities = {
				CoreAbility.getAbility(FireBlast.class),
				CoreAbility.getAbility(EarthBlast.class),
				CoreAbility.getAbility(WaterManipulation.class),
				CoreAbility.getAbility(AirBlast.class),
				CoreAbility.getAbility(AirSwipe.class),
				CoreAbility.getAbility(IceBlast.class),
				CoreAbility.getAbility(IceSpikeBlast.class),
				CoreAbility.getAbility(MetalClips.class),
				CoreAbility.getAbility(FireKick.class),
				CoreAbility.getAbility(FireSpin.class),
				CoreAbility.getAbility(FireWheel.class),
				CoreAbility.getAbility(WaterArmsFreeze.class),
				CoreAbility.getAbility(WaterArmsWhip.class)
		};

		for (CoreAbility ability : abilities) {
			ProjectKorra.getCollisionManager().addCollision(new Collision(main, ability, false, true));
		}
	}

	@Override
	public boolean isEnabled() {
		return Spirits.instance.getConfig().getBoolean("Light.Abilities.Protect.Enabled");
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
		return "Protect";
	}
	
	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public void remove() {
		super.remove();
		if (!bPlayer.isOnCooldown(this)) {
			bPlayer.addCooldown(this);
		}
	}

	public void removeWithoutCooldown() {
		super.remove();
	}

	@Override
	public void handleCollision(Collision collision) {
		super.handleCollision(collision);
	}

	@Override
	public double getCollisionRadius() {
		return size;
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
	public void load() {
		applyCollisions();
	}
	
	@Override
	public void stop() { }
}

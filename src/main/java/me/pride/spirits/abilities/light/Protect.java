package me.pride.spirits.abilities.light;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import me.pride.spirits.Spirits;
import me.pride.spirits.api.ability.LightSpiritAbility;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class Protect extends LightSpiritAbility implements AddonAbility {
	private final String path = Tools.path(this, Path.ABILITIES);

	public enum ProtectType {
		PROTECT, DEFLECT
	}
	private ProtectType type;
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.RANGE)
	private double minRange, range, maxRange;
	@Attribute(Attribute.RADIUS)
	private double size, maxSize;

	private boolean validated;

	private double sizeIncrease;
	
	private Location origin, location;
	private Vector direction;

	public Protect(Player player) {
		super(player);

		if (!bPlayer.canBend(this)) {
			return;
		} else if (RegionProtection.isRegionProtected(player, player.getLocation())) {
			return;
		} else if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (CoreAbility.hasAbility(player, Protect.class)) {
			return;
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
			this.minRange = Spirits.instance.getConfig().getDouble(path + "Deflect.MinRange");
			this.maxRange = Spirits.instance.getConfig().getDouble(path + "Deflect.MaxRange");
			this.maxSize = Spirits.instance.getConfig().getDouble(path + "Deflect.MaxSize");

			this.range = ThreadLocalRandom.current().nextDouble(this.minRange, this.maxRange);
			this.sizeIncrease = ThreadLocalRandom.current().nextDouble(0.25, 0.4);

			this.direction = this.location.getDirection();

		} else if (type == ProtectType.PROTECT) {
			this.cooldown = Spirits.instance.getConfig().getLong(path + "Protect.Cooldown");
		}
		this.type = type;

		start();
	}
	
	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		} else if (RegionProtection.isRegionProtected(player, location, this)) {
			bPlayer.addCooldown(this);
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
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		location.add(direction.multiply(speed));
		size = size >= maxSize ? maxSize : size + sizeIncrease;

		Tools.generateDirectionalCircle(location, location.getDirection(), size, 8, l -> l.getWorld().spawnParticle(Particle.GLOW, l, 1, 0.05, 0.05, 0.05, 0));

		Tools.trackEntitySpirit(location, size / 1.5, e -> e.getUniqueId() != player.getUniqueId(), (entity, light, dark, neutral) -> {
			if (dark) {
				DamageHandler.damageEntity(entity, damage, this);
				entity.setVelocity(player.getEyeLocation().getDirection().multiply(0.5));
				new HorizontalVelocityTracker(entity, player, 0, this);
			}
		});
	}

	private void protect() {
		if (!player.isSneaking()) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
	}

	public static boolean isProtecting(Player player) {
		return CoreAbility.hasAbility(player, Protect.class) && CoreAbility.getAbility(player, Protect.class).getType() == ProtectType.PROTECT;
	}

	public static boolean isDeflecting(Player player) {
		return CoreAbility.hasAbility(player, Protect.class) && CoreAbility.getAbility(player, Protect.class).getType() == ProtectType.DEFLECT;
	}

	public ProtectType getType() {
		return type;
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
}

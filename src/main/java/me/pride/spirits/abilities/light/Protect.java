package me.pride.spirits.abilities.light;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
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

public class Protect extends LightSpiritAbility implements AddonAbility {
	private final String path = Tools.path(this, Path.ABILITIES);
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.RANGE)
	private double range;
	
	private Location origin, location;
	
	public Protect(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			return;
		} else if (RegionProtection.isRegionProtected(player, player.getLocation())) {
			return;
		} else if (bPlayer.isOnCooldown(this)) {
			return;
		}
		this.cooldown = Spirits.instance.getConfig().getLong(path + "Cooldown");
		this.speed = Spirits.instance.getConfig().getDouble(path + "Speed");
		this.damage = Spirits.instance.getConfig().getDouble(path + "Damage");
		this.range = Spirits.instance.getConfig().getDouble(path + "Range");
		
		this.origin = player.getLocation().clone().add(0, 1, 0);
		this.location = origin.clone();
		
		start();
	}
	
	@Override
	public void progress() {
		if (player.isOnline() || player.isDead()) {
			remove();
			return;
		} else if (RegionProtection.isRegionProtected(player, location, this)) {
			remove();
			return;
		}
		if (origin.distanceSquared(location) > range * range) {
			remove();
			return;
		}
		location.add(player.getEyeLocation().getDirection().multiply(speed));
		
		for (int i = 0; i < 360; i += 8) {
			Vector circle = GeneralMethods.getOrthogonalVector(player.getEyeLocation().getDirection(), i, 2.0);
			player.getWorld().spawnParticle(Particle.GLOW, location.clone().add(GeneralMethods.getOrthogonalVector(player.getEyeLocation().getDirection(), i, 0.2)), 0, circle.getX(), circle.getY(), circle.getZ(), 0.10);
		}
		Tools.trackEntitySpirit(location, 1.75, e -> e.getUniqueId() != player.getUniqueId(), (entity, light, dark, neutral) -> {
			if (dark) {
				DamageHandler.damageEntity(entity, damage, this);
				entity.setVelocity(player.getEyeLocation().getDirection().multiply(0.5));
			}
		});
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
		return 0;
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

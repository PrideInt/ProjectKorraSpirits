package me.pride.spirits.abilities.light.passives;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.DamageHandler;
import me.pride.spirits.Spirits;
import me.pride.spirits.api.Spirit;
import me.pride.spirits.api.ability.LightSpiritAbility;
import me.pride.spirits.util.Filter;
import me.pride.spirits.util.SpecialThanks;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Orbs extends LightSpiritAbility implements AddonAbility, PassiveAbility {
	private final String path = Tools.path(this, Path.PASSIVES);

	private enum OrbState {
		ACTIVE, INACTIVE, ABSORBING, ABSORBED, SHOT_ALL, REVERTING_ABSORB, REVERTING_SHOT, REVERTING_SHOT_ALL
	}
	private OrbState state;

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute("Orbs")
	private int orbNumber;
	@Attribute(Attribute.RANGE)
	private double shootRange;
	@Attribute(Attribute.RADIUS)
	private double hitRadius;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	private boolean altForm;

	private double distance;

	private Orb[] orbs;
	private int[] angles;
	private List<Orb> shotOrbs;

	public Orbs(Player player) {
		super(player);

		if (!bPlayer.canBendPassive(this)) {
			return;
		}
		this.cooldown = Spirits.instance.getConfig().getLong(path + "Cooldown");
		this.orbNumber = Spirits.instance.getConfig().getInt(path + "Orbs");
		this.hitRadius = Spirits.instance.getConfig().getDouble(path + "HitRadius");
		this.shootRange = Spirits.instance.getConfig().getDouble(path + "ShootRange");
		this.damage = Spirits.instance.getConfig().getDouble(path + "Damage");
		this.altForm = Spirits.instance.getConfig().getBoolean(path + "AltForm");

		this.state = OrbState.ACTIVE;
		this.orbs = new Orb[this.orbNumber];
		this.angles = new int[this.orbNumber];

		this.shotOrbs = new CopyOnWriteArrayList<>();

		for (int i = 0; i < this.orbNumber; i++) {
			this.orbs[i] = new Orb(orb(), i);
			this.angles[i] = i * (360 / this.orbNumber);
		}
		start();
	}

	@Override
	public void progress() {
		if (bPlayer.isToggled()) {
			addOrbs();

			switch (state) {
				case ACTIVE -> {
					for (int i = 0; i < angles.length; i++) {
						angles[i] += 2;

						if (angles[i] >= 360) {
							angles[i] = 0;
						}
					}
					for (Orb orb : orbs) {
						if (!shotOrbs.contains(orb)) {
							orb.rotate();
						}
					}
					for (Orb shot : shotOrbs) {
						if (shot.getLocation().distanceSquared(player.getLocation()) >= shootRange * shootRange || shot.isReverting()) {
							shot.revert();
						} else {
							if (shot.getDirection() != null) {
								shot.shoot(shot.getDirection(), 1.25);
							}
						}
						if (shot.isInOriginalState()) {
							shotOrbs.remove(shot);
						}
					}
				}
				case ABSORBING -> {
					boolean allAbsorbed = true;

					for (Orb orb : orbs) {
						orb.shoot(GeneralMethods.getDirection(orb.getLocation(), player.getLocation()).normalize(), 1.5);

						if (orb.getLocation().distanceSquared(player.getLocation()) > 0.5 * 0.5) {
							allAbsorbed = false;
						}
					}
					if (allAbsorbed) {
						state = OrbState.ABSORBED;
					}
				}
				case ABSORBED -> {
					for (Orb orb : orbs) {
						orb.getOrb().teleport(player.getLocation());
					}
				}
				case SHOT_ALL -> {
					boolean allShot = true;

					for (Orb orb : orbs) {
						orb.shoot();

						if (orb.getLocation().distanceSquared(player.getLocation()) < distance * distance) {
							allShot = false;
						}
					}
					if (allShot) {
						state = OrbState.REVERTING_SHOT_ALL;
					}
				}
				case REVERTING_ABSORB -> {
					boolean allReverted = true;

					for (Orb orb : orbs) {
						orb.revert();

						if (!orb.isInOriginalState()) {
							allReverted = false;
						}
					}
					if (allReverted) {
						state = OrbState.ACTIVE;
					}
				}
				case REVERTING_SHOT_ALL -> {
					for (Orb orb : orbs) {
						if (orb.getLocation().distanceSquared(player.getLocation()) <= 0.5 * 0.5) {
							state = OrbState.ACTIVE;
						}
						orb.shoot(GeneralMethods.getDirection(orb.getLocation(), player.getLocation()).normalize(), 1.5);
					}
				}
			}
		} else {
			for (Orb orb : orbs) {
				if (orb != null) {
					orb.getOrb().remove();
				}
			}
		}
	}

	private void addOrbs() {
		for (int i = 0; i < orbNumber; i++) {
			if (orbs[i] == null || orbs[i].getOrb().isDead()) {
				orbs[i] = new Orb(orb(), i);
			}
		}
	}

	private ArmorStand orb() {
		return player.getWorld().spawn(player.getLocation(), ArmorStand.class, e -> {
			e.setVisible(false);
			e.setGravity(false);
			e.setSmall(true);
			e.getEquipment().setHelmet(new ItemStack(SpecialThanks.getOrbType(player)));

			e.setMetadata(Spirit.ORB_KEY, new FixedMetadataValue(Spirits.instance, 0));
		});
	}

	public void absorb() {
		this.state = OrbState.ABSORBING;
	}

	public static void absorb(Player player) {
		if (hasAbility(player, Orbs.class)) {
			getAbility(player, Orbs.class).absorb();
		}
	}

	public void unabsorb() {
		this.state = OrbState.REVERTING_ABSORB;
	}

	public static void unabsorb(Player player) {
		if (hasAbility(player, Orbs.class)) {
			getAbility(player, Orbs.class).unabsorb();
		}
	}

	public void shoot() {
		if (shotOrbs.size() != orbNumber) {
			Orb orb = orbs[ThreadLocalRandom.current().nextInt(orbNumber)];

			if (shotOrbs.contains(orb)) {
				int i = ThreadLocalRandom.current().nextInt(orbNumber);
				orb = orbs[i];

				while (shotOrbs.contains(orb)) {
					i = ThreadLocalRandom.current().nextInt(orbNumber);
					orb = orbs[i];
				}
			}
			RayTraceResult result = Tools.rayTrace(player, shootRange, e -> e.getUniqueId() != player.getUniqueId() && !e.hasMetadata(Spirit.ORB_KEY));

			shotOrbs.add(orb);

			if (result == null) {
				orb.shoot(GeneralMethods.getDirection(orb.getLocation(), GeneralMethods.getTargetedLocation(player, shootRange)).normalize(), 1.25);
			} else {
				Entity entity = result.getHitEntity();

				if (entity == null) {
					orb.shoot(GeneralMethods.getDirection(orb.getLocation(), GeneralMethods.getTargetedLocation(player, shootRange)).normalize(), 1.25);
				} else {
					orb.shoot(GeneralMethods.getDirection(orb.getLocation(), entity.getLocation()).normalize(), 1.25);
				}
			}
			player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RETURN, 1, 1);
		}
	}

	public static void shoot(Player player) {
		if (hasAbility(player, Orbs.class)) {
			getAbility(player, Orbs.class).shoot();
		}
	}

	public void shootAll(double distance) {
		this.state = OrbState.SHOT_ALL;
		this.distance = distance;
	}

	public static void shootAll(Player player, double distance) {
		if (hasAbility(player, Orbs.class)) {
			getAbility(player, Orbs.class).shootAll(distance);
		}
	}

	public void setState(OrbState state) {
		this.state = state;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	@Override
	public boolean isEnabled() {
		return Spirits.instance.getConfig().getBoolean("Light.Passives.Orbs.Enabled");
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
		return "Orbs";
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public List<Location> getLocations() {
		return Arrays.stream(orbs).map(Orb::getLocation).toList();
	}

	@Override
	public void remove() {
		super.remove();
		for (Orb orb : orbs) {
			if (orb != null) {
				orb.getOrb().remove();
			}
		}
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

	class Orb {
		private ArmorStand orb;
		private int pos;

		private boolean reverting;
		private boolean inOriginalState;

		private Location location;
		private Vector direction;

		public Orb(ArmorStand orb, int pos) {
			this.orb = orb;
			this.pos = pos;
			this.location = this.orb.getLocation();

			this.reverting = false;
			this.inOriginalState = true;
		}
		public ArmorStand getOrb() {
			return orb;
		}
		public Location getLocation() {
			return orb.getLocation();
		}
		public Vector getDirection() {
			return direction;
		}
		public boolean isReverting() {
			return reverting;
		}
		public boolean isInOriginalState() {
			return inOriginalState;
		}
		public int getPos() {
			return pos;
		}
		public void rotate() {
			Location loc = player.getLocation().clone();

			if (altForm) {
				loc = player.getLocation().clone().add(0, 0.8, 0).add(player.getLocation().getDirection().normalize().multiply(-1));
				Vector circle = GeneralMethods.getOrthogonalVector(player.getEyeLocation().getDirection(), angles[pos], 1);

				loc.add(circle);
			} else {
				double angle = angles[pos];
				double x = 1.5 * Math.cos(Math.toRadians(angle));
				double z = 1.5 * Math.sin(Math.toRadians(angle));

				loc.add(x, 0, z);
			}
			orb.teleport(loc);
		}
		public void revert() {
			reverting = true;
			Location loc = player.getLocation().clone();

			if (altForm) {
				loc = player.getLocation().clone().add(0, 0.8, 0).add(player.getLocation().getDirection().normalize().multiply(-1));
				Vector circle = GeneralMethods.getOrthogonalVector(player.getEyeLocation().getDirection(), angles[pos], 1);

				loc.add(circle);
			} else {
				double angle = angles[pos];
				double x = 1.5 * Math.cos(Math.toRadians(angle));
				double z = 1.5 * Math.sin(Math.toRadians(angle));

				loc.add(x, 0, z);
			}
			orb.teleport(orb.getLocation().add(GeneralMethods.getDirection(orb.getLocation(), loc).normalize().multiply(1)));

			if (orb.getLocation().distanceSquared(loc) <= 0.5 * 0.5) {
				inOriginalState = true;
				reverting = false;
				direction = null;
			}
		}
		public void shoot(Vector direction, double speed) {
			inOriginalState = false;
			if (this.direction == null) {
				this.direction = direction;
			}
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(orb.getLocation(), Orbs.this.hitRadius)) {
				if (entity.getUniqueId() != player.getUniqueId() && entity instanceof LivingEntity) {
					DamageHandler.damageEntity(entity, Orbs.this.damage, Orbs.this);
				}
			}
			orb.teleport(orb.getLocation().add(direction.multiply(speed)));
		}
		public void shoot(Vector direction) {
			shoot(direction, 1.5);
		}
		public void shoot() {
			shoot(player.getEyeLocation().getDirection(), 1.5);
		}
	}
}

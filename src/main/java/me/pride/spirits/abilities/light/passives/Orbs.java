package me.pride.spirits.abilities.light.passives;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import me.pride.spirits.Spirits;
import me.pride.spirits.api.ability.LightSpiritAbility;
import me.pride.spirits.util.SpecialThanks;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class Orbs extends LightSpiritAbility implements AddonAbility, PassiveAbility {
	private final String path = Tools.path(this, Path.PASSIVES);

	private enum OrbState {
		ACTIVE, INACTIVE, ABSORBING, ABSORBED, SHOT, REVERTING_ABSORB, REVERTING_SHOT
	}
	private OrbState state;

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute("Orbs")
	private int orbNumber;

	private double distance;

	private ArmorStand[] orbs;
	private int[] angles;

	public Orbs(Player player) {
		super(player);

		if (!bPlayer.canBendPassive(this)) {
			return;
		}
		this.cooldown = Spirits.instance.getConfig().getLong(path + "Cooldown");
		this.orbNumber = Spirits.instance.getConfig().getInt(path + "Orbs");

		this.state = OrbState.ACTIVE;
		this.orbs = new ArmorStand[this.orbNumber];
		this.angles = new int[this.orbNumber];

		for (int i = 0; i < this.orbNumber; i++) {
			this.orbs[i] = orb();
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
					for (int i = 0; i < orbNumber; i++) {
						Location loc = player.getLocation().clone();

						double angle = angles[i];
						double x = 1.5 * Math.cos(Math.toRadians(angle));
						double z = 1.5 * Math.sin(Math.toRadians(angle));

						loc.add(x, 0, z);

						orbs[i].teleport(loc);
						angles[i] += 2;
					}
				}
				case ABSORBING -> {
					for (ArmorStand orb : orbs) {
						orb.teleport(orb.getLocation().add(GeneralMethods.getDirection(orb.getLocation(), player.getLocation()).normalize().multiply(1.5)));

						if (orb.getLocation().distanceSquared(player.getLocation()) <= 0.5 * 0.5) {
							state = OrbState.ABSORBED;
						}
					}
				}
				case ABSORBED -> {
					for (ArmorStand orb : orbs) {
						orb.teleport(player.getLocation());
					}
				}
				case SHOT -> {
					for (ArmorStand orb : orbs) {
						orb.teleport(orb.getLocation().add(player.getEyeLocation().getDirection().multiply(1.5)));

						if (orb.getLocation().distanceSquared(player.getLocation()) >= distance * distance) {
							state = OrbState.REVERTING_SHOT;
						}
					}
				}
				case REVERTING_ABSORB -> {
					for (int i = 0; i < orbNumber; i++) {
						Location loc = player.getLocation().clone();

						double angle = angles[i];
						double x = 1.5 * Math.cos(Math.toRadians(angle));
						double z = 1.5 * Math.sin(Math.toRadians(angle));

						loc.add(x, 0, z);

						orbs[i].teleport(orbs[i].getLocation().add(GeneralMethods.getDirection(orbs[i].getLocation(), loc).normalize().multiply(1)));

						if (orbs[i].getLocation().distanceSquared(loc) <= 0.5 * 0.5) {
							state = OrbState.ACTIVE;
						}
						angles[i] += 2;
					}
				}
				case REVERTING_SHOT -> {
					for (ArmorStand orb : orbs) {
						if (orb.getLocation().distanceSquared(player.getLocation()) <= 0.5 * 0.5) {
							state = OrbState.ACTIVE;
						}
						orb.teleport(orb.getLocation().add(GeneralMethods.getDirection(orb.getLocation(), player.getLocation()).normalize().multiply(1.5)));
					}
				}
			}
		} else {
			for (ArmorStand orb : orbs) {
				if (orb != null) {
					orb.remove();
				}
			}
		}
	}

	private void addOrbs() {
		for (int i = 0; i < orbNumber; i++) {
			if (orbs[i] == null || orbs[i].isDead()) {
				orbs[i] = orb();
			}
		}
	}

	private ArmorStand orb() {
		return player.getWorld().spawn(player.getLocation(), ArmorStand.class, e -> {
			e.setVisible(false);
			e.setGravity(false);
			e.setSmall(true);
			e.getEquipment().setHelmet(new ItemStack(SpecialThanks.getOrbType(player)));
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

	public void shoot(double distance) {
		this.state = OrbState.SHOT;
		this.distance = distance;
	}

	public static void shoot(Player player, double distance) {
		if (hasAbility(player, Orbs.class)) {
			getAbility(player, Orbs.class).shoot(distance);
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
		if (orbs == null) {
			return null;
		}
		return Arrays.stream(orbs).map(ArmorStand::getLocation).toList();
	}

	@Override
	public void remove() {
		super.remove();
		for (ArmorStand orb : orbs) {
			if (orb != null) {
				orb.remove();
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
}

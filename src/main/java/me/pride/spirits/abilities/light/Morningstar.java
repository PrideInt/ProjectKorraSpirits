package me.pride.spirits.abilities.light;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import me.pride.spirits.Spirits;
import me.pride.spirits.api.ability.LightSpiritAbility;
import me.pride.spirits.util.SpecialThanks;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

public class Morningstar extends LightSpiritAbility implements AddonAbility {
	private final String path = Tools.path(this, Path.ABILITIES);

	private enum StarState {
		CHARGING, CHARGED, LAUNCHED;
	}
	private StarState state;

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.SELECT_RANGE)
	private double selectRange;
	@Attribute("MaxSize")
	private double maxSize;
	private double chargeSize;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.RANGE)
	private double range;

	private double size;

	private BlockDisplay display;
	private Location origin, location;
	private Vector direction;

	public Morningstar(Player player) {
		super(player);

		if (!bPlayer.canBend(this)) {
			return;
		}
		this.cooldown = Spirits.instance.getConfig().getLong(path + "Cooldown");
		this.selectRange = Spirits.instance.getConfig().getDouble(path + "SelectRange");
		this.maxSize = Spirits.instance.getConfig().getDouble(path + "MaxSize");
		this.chargeSize = Spirits.instance.getConfig().getDouble(path + "ChargeSize");
		this.speed = Spirits.instance.getConfig().getDouble(path + "Speed");
		this.range = Spirits.instance.getConfig().getDouble(path + "Range");

		this.state = StarState.CHARGING;

		Location target = GeneralMethods.getTargetedLocation(player, this.selectRange);

		this.origin = target.clone();
		this.location = this.origin.clone();
		this.direction = player.getEyeLocation().getDirection();

		this.display = (BlockDisplay) target.getWorld().spawnEntity(target, EntityType.BLOCK_DISPLAY);
		this.display.setBlock(SpecialThanks.getOrbType(player).createBlockData());

		Transformation transformation = this.display.getTransformation();
		transformation.getScale().set(0);

		this.display.setTransformation(transformation);

		start();
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		switch (state) {
			case CHARGING:
				charge();
				break;
			case CHARGED:
				charged();
				break;
			case LAUNCHED:
				launch();
				break;
		}
	}

	private void charge() {
		origin = GeneralMethods.getTargetedLocation(player, selectRange).clone();
		location = origin.clone();
		direction = player.getEyeLocation().getDirection();

		display.teleport(location);

		if (!player.isSneaking()) {
			state = StarState.LAUNCHED;
		}
	}

	private void chargeUp() {
		charge();

		size = size >= maxSize ? maxSize : size + chargeSize;

		Transformation transformation = display.getTransformation();
		transformation.getScale().set(size);
	}

	private void charged() {
		charge();

		display.getWorld().spawnParticle(SpecialThanks.getParticle(player), location, 1, 0.1, 0.1, 0.1, 0.05);
	}

	private void launch() {
		if (origin.distanceSquared(location) > range * range) {
			remove();
			return;
		}
		if (location.getBlock().getType().isSolid()) {
			remove();
			return;
		}
		location.add(direction.multiply(speed));

		display.teleport(location);
	}

	@Override
	public boolean isEnabled() {
		return Spirits.instance.getConfig().getBoolean("Light.Abilities.Morningstar.Enabled");
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
		return "Morningstar";
	}

	@Override
	public void remove() {
		display.remove();
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
}

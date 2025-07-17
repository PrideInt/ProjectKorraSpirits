package me.pride.spirits.abilities.spirit.combos;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.ClickType;
import me.pride.spirits.Spirits;
import me.pride.spirits.abilities.spirit.Disappear;
import me.pride.spirits.abilities.spirit.Rematerialize;
import me.pride.spirits.api.ability.SpiritAbility;
import me.pride.spirits.api.ability.SpiritElement;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class Possess extends SpiritAbility implements AddonAbility, ComboAbility {

	public record PossessionData(double health, boolean invulnerable, boolean invisible, boolean gliding, GameMode gameMode) {
		public double health() {
			return health;
		}
		public boolean invulnerable() {
			return invulnerable;
		}
		public boolean invisible() {
			return invisible;
		}
		public boolean gliding() {
			return gliding;
		}
		public GameMode gameMode() {
			return gameMode;
		}
	}
	
	private final String path = Tools.path(this, Path.COMBOS);

	public static final Map<UUID, Set<UUID>> POSSESSORS = new HashMap<>();
	public static final Set<Pair<UUID, UUID>> POSSESSED = new HashSet<>();

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.SELECT_RANGE)
	private double selectRange;

	private boolean possessingImmovable;

	private PossessionData possessorData, possessedData;
	private Entity target;
	private Location origin;

	public Possess(Player player) {
		super(player);

		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		} else if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (RegionProtection.isRegionProtected(player, player.getLocation(), this)) {
			return;
		}
		if (hasAbility(player, Disappear.class)) {
			getAbility(player, Disappear.class).remove();
		}
		if (hasAbility(player, Rematerialize.class)) {
			getAbility(player, Rematerialize.class).remove();
		}
		this.cooldown = Spirits.instance.getConfig().getLong(path + "Cooldown");
		this.duration = Spirits.instance.getConfig().getLong(path + "Duration");
		this.selectRange = Spirits.instance.getConfig().getDouble(path + "SelectRange");

		this.target = Tools.rayTraceEntity(player, selectRange, 1.3);

		if (this.target == null) {
			return;
		}
		if (this.target.getType() == EntityType.PLAYER) {
			Player targetPlayer = (Player) this.target;

			if (hasAbility(targetPlayer, Possess.class)) {
				return;
			}
			BendingPlayer targetBPlayer = BendingPlayer.getBendingPlayer(targetPlayer);

			if (targetBPlayer != null) {
				if (targetBPlayer.hasElement(SpiritElement.SPIRIT) || targetBPlayer.hasElement(SpiritElement.LIGHT_SPIRIT) || targetBPlayer.hasElement(SpiritElement.DARK_SPIRIT)) {
					player.sendMessage("You cannot possess another Spirit.");
					return;
				}
			}
		}
		if (RegionProtection.isRegionProtected(player, this.target.getLocation(), this)) {
			return;
		}
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1, 1);

		this.possessorData = new PossessionData(player.getHealth(), player.isInvulnerable(), player.isInvisible(), player.isGliding(), player.getGameMode());

		if (target instanceof LivingEntity) {
			if (target.getType() == EntityType.PLAYER) {
				this.possessedData = new PossessionData(((Player) target).getHealth(), target.isInvulnerable(), ((Player) target).isInvisible(), ((Player) target).isGliding(), ((Player) target).getGameMode());
			} else {
				this.possessedData = new PossessionData(((LivingEntity) target).getHealth(), target.isInvulnerable(), ((LivingEntity) target).isInvisible(), ((LivingEntity) target).isGliding(), null);
			}
		}
		player.setInvisible(true);

		possess(this.target);

		start();
		bPlayer.addCooldown(this);
	}
	
	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		} else if (!player.isSneaking()) {
			remove();
			return;
		} else if (target.isDead()) {
			remove();
			return;
		} else if (target.getType() == EntityType.PLAYER && !((Player) target).isOnline()) {
			remove();
			return;
		} else if (System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		if (!possessingImmovable) {
			target.teleport(player);
		}
	}

	private void possess(Entity entity) {
		if (!(entity instanceof LivingEntity)) {
			this.duration = Long.MAX_VALUE;
		}
		if (entity.getType() == EntityType.ITEM) {
			Item item = (Item) entity;

			item.setInvulnerable(true);
			item.setPickupDelay(Integer.MAX_VALUE);

			player.setGliding(true);
		} else if (entity.getType() == EntityType.PLAYER) {
			Set<UUID> hiddenFrom = new HashSet<>();

			for (Player online : Bukkit.getOnlinePlayers()) {
				online.hidePlayer(Spirits.instance, player);
				hiddenFrom.add(online.getUniqueId());
			}
			POSSESSORS.put(player.getUniqueId(), hiddenFrom);
			POSSESSED.add(Pair.of(player.getUniqueId(), entity.getUniqueId()));
		} else if (entity.getType() == EntityType.ITEM_DISPLAY || entity.getType() == EntityType.ARMOR_STAND) {
			player.setGameMode(GameMode.SPECTATOR);

			this.origin = player.getLocation().clone();
			this.possessingImmovable = true;
		}
		player.teleport(entity.getLocation());
	}

	public boolean isPossessingImmovable() {
		return possessingImmovable;
	}

	public static boolean isPossessingImmovable(Player player) {
		if (hasAbility(player, Possess.class)) {
			return getAbility(player, Possess.class).isPossessingImmovable();
		}
		return false;
	}

	public static boolean isPossessed(Player player) {
		return POSSESSED.stream().anyMatch(pair -> pair.getRight().equals(player.getUniqueId()));
	}

	public static Optional<Player> getPossessedFrom(Player player) {
		return POSSESSED.stream().filter(pair -> pair.getLeft().equals(player.getUniqueId())).map(pair -> Bukkit.getPlayer(pair.getRight())).findFirst();
	}

	@Override
	public boolean isEnabled() {
		return Spirits.instance.getConfig().getBoolean("Spirit.Combos.Possess.Enabled", true);
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
		return "Possess";
	}

	@Override
	public void remove() {
		super.remove();

		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1, 1);

		if (target.getType() == EntityType.ITEM) {
			Item item = (Item) target;

			item.setInvulnerable(false);
			item.setPickupDelay(0);
		} else if (target.getType() == EntityType.PLAYER) {
			Set<UUID> hiddenFrom = POSSESSORS.get(player.getUniqueId());

			for (UUID uuid : hiddenFrom) {
				Player online = Bukkit.getPlayer(uuid);

				if (online != null) {
					online.showPlayer(Spirits.instance, player);
				}
			}
			POSSESSORS.remove(player.getUniqueId());
		}
		if (origin != null) {
			player.teleport(origin);
		}
		player.setGameMode(possessorData.gameMode());
		player.setInvulnerable(possessorData.invulnerable());
		player.setInvisible(possessorData.invisible());
		player.setGliding(possessorData.gliding());
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
	
	@Override
	public Object createNewComboInstance(Player player) {
		return new Possess(player);
	}
	
	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> info = new ArrayList<>();
		info.add(new AbilityInformation("Disappear", ClickType.LEFT_CLICK));
		info.add(new AbilityInformation("Rematerialize", ClickType.SHIFT_DOWN));
		return info;
	}
}

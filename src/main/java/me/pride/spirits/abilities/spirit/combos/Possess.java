package me.pride.spirits.abilities.spirit.combos;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.ClickType;
import me.pride.spirits.Spirits;
import me.pride.spirits.abilities.spirit.Disappear;
import me.pride.spirits.api.ability.SpiritAbility;
import me.pride.spirits.api.ability.SpiritElement;
import me.pride.spirits.util.Tools;
import me.pride.spirits.util.Tools.Path;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Possess extends SpiritAbility implements AddonAbility, ComboAbility {
	private final String path = Tools.path(this, Path.COMBOS);

	public static final Map<UUID, Set<UUID>> POSSESSORS = new HashMap<>();

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.SELECT_RANGE)
	private double selectRange;
	private boolean useNMS;

	private boolean possessingImmovable;
	private boolean alreadyInvisible;

	private Entity target;
	private Location origin;
	private GameMode gameMode;

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
		this.cooldown = Spirits.instance.getConfig().getLong(path + "Cooldown");
		this.duration = Spirits.instance.getConfig().getLong(path + "Duration");
		this.selectRange = Spirits.instance.getConfig().getDouble(path + "SelectRange");
		this.useNMS = Spirits.instance.getConfig().getBoolean(path + "ChangeSkins");

		this.target = Tools.rayTraceEntity(player, selectRange);

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
			return;
		}
		if (RegionProtection.isRegionProtected(player, this.target.getLocation(), this)) {
			return;
		}
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1, 1);

		this.gameMode = player.getGameMode();
		this.alreadyInvisible = player.isInvisible();

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
		if (entity.getType() == EntityType.DROPPED_ITEM) {
			Item item = (Item) entity;

			item.setInvulnerable(true);
			item.setPickupDelay(Integer.MAX_VALUE);

			player.setGliding(true);
		} else if (entity.getType() == EntityType.PLAYER) {
			Player possessedPlayer = (Player) entity;

			if (useNMS) {
				CraftPlayer craftPlayer = ((CraftPlayer) possessedPlayer).getHandle().getBukkitEntity();

				if (setSkin(craftPlayer.getProfile(), UUID.randomUUID())) {
					possessedPlayer.sendMessage("You have been possessed by " + player.getName());
				}
				possessedPlayer.setGameMode(GameMode.SPECTATOR);
			} else {
				Set<UUID> hiddenFrom = new HashSet<>();

				for (Player online : Bukkit.getOnlinePlayers()) {
					online.hidePlayer(Spirits.instance, player);
					hiddenFrom.add(online.getUniqueId());
				}
				POSSESSORS.put(player.getUniqueId(), hiddenFrom);
			}
		} else if (entity.getType() == EntityType.ITEM_DISPLAY || entity.getType() == EntityType.ARMOR_STAND) {
			player.setGameMode(GameMode.SPECTATOR);

			this.origin = player.getLocation().clone();
			this.possessingImmovable = true;
		}
		player.teleport(entity.getLocation());
	}

	public static boolean setSkin(GameProfile profile, UUID uuid) {
		try {
			HttpsURLConnection connection = (HttpsURLConnection) new URL(String.format("https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false", UUIDTypeAdapter.fromUUID(uuid))).openConnection();
			if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
				String reply = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
				String skin = reply.split("\"value\":\"")[1].split("\"")[0];
				String signature = reply.split("\"signature\":\"")[1].split("\"")[0];

				profile.getProperties().removeAll("textures");
				profile.getProperties().put("textures", new Property("textures", skin, signature));
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			return false;
		}
	}

	private void reload(Player player) {
		for (Player online : Bukkit.getOnlinePlayers()) {
			online.hidePlayer(Spirits.instance, player);
		}
		for (Player online : Bukkit.getOnlinePlayers()) {
			online.showPlayer(Spirits.instance, player);
		}
		player.spigot().respawn();
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

		if (target.getType() == EntityType.DROPPED_ITEM) {
			Item item = (Item) target;

			item.setInvulnerable(false);
			item.setPickupDelay(0);
		} else if (target.getType() == EntityType.PLAYER) {
			Player possessedPlayer = (Player) target;

			if (useNMS) {
				possessedPlayer.setGameMode(GameMode.SURVIVAL);
			} else {
				Set<UUID> hiddenFrom = POSSESSORS.get(player.getUniqueId());

				for (UUID uuid : hiddenFrom) {
					Player online = Bukkit.getPlayer(uuid);

					if (online != null) {
						online.showPlayer(Spirits.instance, player);
					}
				}
				POSSESSORS.remove(player.getUniqueId());
			}
		}
		if (player.getGameMode() == GameMode.SPECTATOR) {
			player.setGameMode(gameMode);
		}
		if (player.isGliding()) {
			player.setGliding(false);
		}
		if (origin != null) {
			player.teleport(origin);
		}
		player.setInvisible(alreadyInvisible);
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
		info.add(new AbilityInformation("Disappear", ClickType.SHIFT_DOWN));
		return info;
	}
}

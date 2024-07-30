package me.pride.spirits.util;

import com.projectkorra.projectkorra.board.BendingBoardManager;
import me.pride.spirits.Spirits;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Set;

public class GhostFactory {
	public static final String GHOSTS_KEY = "ghost";

	private Scoreboard scoreboard;
	private Team team;

	private static final Set<Player> VIEWERS = new HashSet<>();

	public GhostFactory() {
		this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		this.team = this.scoreboard.registerNewTeam(GHOSTS_KEY);
		this.team.setCanSeeFriendlyInvisibles(true);
		this.team.setAllowFriendlyFire(false);
	}
	public void ghost(Player player) {
		addToTeam(player);
		PotionEffectType.INVISIBILITY.createEffect(Integer.MAX_VALUE, 1).apply(player);
	}
	public void unghost(Player player) {
		removeFromTeam(player);
		player.removePotionEffect(PotionEffectType.INVISIBILITY);
	}
	public void addToTeam(Player player) {
		Scoreboard board = player.getScoreboard();

		if (board.getTeam(GHOSTS_KEY) == null) {
			board.registerNewTeam(GHOSTS_KEY).setCanSeeFriendlyInvisibles(true);
		}
		Team team = this.team;

		if (!player.getScoreboard().equals(scoreboard)) {
			team = board.getTeam(GHOSTS_KEY);
		}
		if (team.hasEntry(player.getName())) {
			return;
		}
		team.addEntry(player.getName());

		VIEWERS.add(player);
	}
	public void removeFromTeam(Player player) {
		Scoreboard board = player.getScoreboard();

		if (board.getTeam(GHOSTS_KEY) == null) {
			board.registerNewTeam(GHOSTS_KEY).setCanSeeFriendlyInvisibles(true);
		}
		Team team = this.team;

		if (!player.getScoreboard().equals(scoreboard)) {
			team = board.getTeam(GHOSTS_KEY);
		}
		if (VIEWERS.contains(player)) {
			VIEWERS.remove(player);
		}
		if (!team.hasEntry(player.getName())) {
			return;
		}
		team.removeEntry(player.getName());

		VIEWERS.remove(player);
	}
	@Deprecated
	public void setGhostTime(Player player, long duration) {
		ghost(player);
		new BukkitRunnable() {
			@Override
			public void run() {
				unghost(player);
			}
		}.runTaskLater(Spirits.instance, (duration / 1000) * 20);
	}
	public void setGhostTime(Player player, Player viewer, long duration) {
		ghost(player);
		addToTeam(viewer);
		new BukkitRunnable() {
			@Override
			public void run() {
				unghost(player);
				removeFromTeam(viewer);
			}
		}.runTaskLater(Spirits.instance, (duration / 1000) * 20);
	}
	public void setGhostTime(Player player, Set<Player> viewers, long duration) {
		ghost(player);
		viewers.iterator().forEachRemaining(viewer -> addToTeam(viewer));
		new BukkitRunnable() {
			@Override
			public void run() {
				unghost(player);
				viewers.iterator().forEachRemaining(viewer -> removeFromTeam(viewer));
			}
		}.runTaskLater(Spirits.instance, (duration / 1000) * 20);
	}
	public boolean isGhost(Player player) {
		// return GHOSTS.contains(player.getUniqueId()) || team.hasEntry(player.getUniqueId().toString());
		return team.hasEntry(player.getName());
	}

	/*
	 * For servers with multiple scoreboards, like ones that use ProjectKorra,
	 * they are relative for each player, so no use really in storing this sort of data.
	 */
	@Deprecated
	public Scoreboard getScoreboard() {
		return scoreboard;
	}
	@Deprecated
	public Team getTeam() {
		return team;
	}
	public static boolean isGhostEnabled() {
		return Spirits.instance.getConfig().getBoolean("Spirit.Ghosts");
	}
	/*
	public static Set<UUID> getGhosts() {
		return GHOSTS;
	}
	 */
}

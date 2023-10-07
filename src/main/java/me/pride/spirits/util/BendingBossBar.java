package me.pride.spirits.util;

import me.pride.spirits.storage.StorageCache;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// TODO: database to store players that are added to the bossbar to handle persistently
public class BendingBossBar {
	private static final Set<BendingBossBar> BARS = new HashSet<>();
	private BossBar bossBar;
	private NamespacedKey key;
	private double length;
	
	private double progress;
	
	public BendingBossBar(NamespacedKey key, String title, BarColor barColor, double length, boolean startup, long startupTime, Player... players) {
		if (key != null && Bukkit.getBossBar(key) != null) {
			this.length = length;
			this.key = key;
			this.progress = Bukkit.getBossBar(key).getProgress();
			this.bossBar = Bukkit.getBossBar(key);
			BARS.add(this);
		} else {
			this.length = length;
			if (key != null) {
				this.key = key;
				this.bossBar = Bukkit.createBossBar(key, title, barColor, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC, BarFlag.CREATE_FOG);
			} else {
				this.bossBar = Bukkit.createBossBar(title, barColor, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC, BarFlag.CREATE_FOG);
			}
			if (startup) {
				this.bossBar.setProgress(0);
				new BarTimer(0, (0.05 / (startupTime / 1000.0)), this);
			} else {
				this.progress = 1;
				this.bossBar.setProgress(this.progress);
			}
			for (Player player : players) {
				StorageCache.addUUIDToCache(player.getUniqueId());
				this.bossBar.setVisible(true);
				this.bossBar.addPlayer(player);
				BARS.add(this);
			}
		}
	}
	public BendingBossBar(NamespacedKey key, String title, BarColor barColor, double length, Player... players) {
		this(key, title, barColor, length, false, 0, players);
	}
	public BendingBossBar(String title, BarColor barColor, double length, Player... players) {
		this(null, title, barColor, length, false, 0, players);
	}
	public void remove() {
		for (Player player : bossBar.getPlayers()) {
			StorageCache.removeUUIDFromCache(player.getUniqueId());
		}
		bossBar.removeAll();
		if (key != null) {
			Bukkit.removeBossBar(key);
		}
		BARS.removeIf(bar -> bar.equals(this));
		BendingBossBar bendingBossBar = this;
		bendingBossBar = null;
	}
	public List<Player> players() {
		return bossBar.getPlayers();
	}
	public double progress() {
		return bossBar.getProgress();
	}
	public BossBar bossBar() {
		return this.bossBar;
	}
	public NamespacedKey key() {
		return this.key;
	}
	public BendingBossBar setProgress(double progress) {
		this.progress = progress;
		bossBar.setProgress(progress);
		return this;
	}
	public BendingBossBar update(double segment, boolean increase) {
		if (progress <= 0) {
			return this;
		}
		segment = segment / length;
		if (increase) {
			progress = progress + segment >= 1 ? 1 : progress + segment;
		} else {
			progress = progress - segment <= 0 ? 0 : progress - segment;
		}
		bossBar.setProgress(progress);
		return this;
	}
	public static void reset(NamespacedKey key, String title, BarColor barColor, double length, BarFlag... flags) {
		Bukkit.getServer().removeBossBar(key);
		
		from(key).ifPresent(bar -> {
			double progress = bar.progress();
			List<Player> players = new ArrayList<>();
			players.addAll(bar.bossBar.getPlayers());
			
			bar.bossBar.removeAll();
			bar.bossBar = Bukkit.createBossBar(key, title, barColor, BarStyle.SOLID, flags);
			
			bar.bossBar.setProgress(progress);
			for (Player player : players) {
				bar.bossBar.addPlayer(player);
			}
		});
	}
	public static void reset(BendingBossBar bar, String title, BarColor barColor, double length, BarFlag... flags) {
		double progress = bar.progress();
		List<Player> players = new ArrayList<>();
		players.addAll(bar.bossBar.getPlayers());
		
		bar.bossBar.removeAll();
		bar.bossBar = Bukkit.createBossBar(title, barColor, BarStyle.SOLID, flags);
		
		bar.bossBar.setProgress(progress);
		for (Player player : players) {
			bar.bossBar.addPlayer(player);
		}
	}
	public static Optional<BendingBossBar> from(NamespacedKey key) {
		for (BendingBossBar bar : BARS) {
			if (bar.key().getNamespace().equals(key.getNamespace())) {
				return Optional.of(bar);
			}
		}
		return Optional.empty();
	}
	public static Optional<BendingBossBar> fromPlayer(Player player) {
		for (BendingBossBar bar : BARS) {
			if (bar.bossBar().getPlayers().contains(player)) {
				return Optional.of(bar);
			}
		}
		return Optional.empty();
	}
	public static Optional<BossBar> of(NamespacedKey key) {
		return Bukkit.getBossBar(key) == null ? Optional.empty() : Optional.of(Bukkit.getBossBar(key));
	}
	public static boolean exists(NamespacedKey key) {
		return of(key).isPresent();
	}
	public static void updateTimer() {
		BarTimer.update();
	}
	
	class BarTimer {
		private static final Set<BarTimer> TIMER = new HashSet<>();
		private int start;
		private double progress, timer;
		private BendingBossBar bossBar;
		
		protected BarTimer(int start, double progress, BendingBossBar bossBar) {
			this.start = start; this.progress = progress; this.bossBar = bossBar;
			TIMER.add(this);
		}
		private int start() { return this.start; }
		private double progress() { return this.progress; }
		public BendingBossBar bendingBossBar() { return this.bossBar; }
		public double timer() { return this.timer; }
		
		public static void update() {
			TIMER.removeIf(timer -> {
				timer.bendingBossBar().setProgress(timer.timer);
				
				timer.timer += timer.progress;
				if (timer.timer >= 1) {
					timer.bendingBossBar().setProgress(1);
					timer.timer = 1;
					return true;
				}
				return false;
			});
		}
	}
}
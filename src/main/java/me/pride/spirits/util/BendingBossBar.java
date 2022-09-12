package me.pride.spirits.util;

import me.pride.spirits.Spirits;
import me.pride.spirits.game.AncientSoulweaver;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BendingBossBar {
	private static final Set<BendingBossBar> BARS = new HashSet<>();
	private BossBar bossBar;
	private NamespacedKey key;
	private double length;
	
	private double progress;
	
	public BendingBossBar(String title, NamespacedKey key, BarColor barColor, double length, boolean startup, long startupTime, Player... players) {
		if (exists(AncientSoulweaver.ANCIENT_SOULWEAVER_BAR_KEY)) {
			remove();
			return;
		}
		this.length = length;
		this.key = key;
		this.bossBar = Bukkit.createBossBar(key, title, barColor, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC, BarFlag.CREATE_FOG);
		BARS.add(this);
		
		if (startup) {
			this.bossBar.setProgress(0);
			new BarTimer(0, (0.05 / (startupTime / 1000.0)), this);
		} else {
			this.progress = 1;
			this.bossBar.setProgress(this.progress);
		}
		for (Player player : players) {
			this.bossBar.setVisible(true);
			this.bossBar.addPlayer(player);
		}
	}
	public BendingBossBar(String title, NamespacedKey key, BarColor barColor, double length, Player... players) {
		this(title, key, barColor, length, false, 0, players);
	}
	public void remove() {
		BendingBossBar bendingBossBar = this;
		bendingBossBar = null;
	}
	public BossBar bossBar() {
		return this.bossBar;
	}
	public NamespacedKey key() {
		return this.key;
	}
	public void setProgress(double progress) {
		this.progress = progress;
		bossBar.setProgress(progress);
	}
	public void update(double segment) {
		if (progress <= 0) {
			return;
		}
		segment = segment / length;
		progress = progress - segment <= 0 ? 0 : progress - segment;
		
		bossBar.setProgress(progress);
		System.out.println(bossBar.getProgress());
	}
	public static Optional<BendingBossBar> from(NamespacedKey key) {
		for (BendingBossBar bar : BARS) {
			if (bar.key().getNamespace().equals(key.getNamespace())) {
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
}

class BarTimer {
	private static final Set<BarTimer> TIMER = new HashSet<>();
	private int start;
	private double progress, timer;
	private BendingBossBar bossBar;
	
	public BarTimer(int start, double progress, BendingBossBar bossBar) {
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
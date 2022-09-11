package me.pride.spirits.util;

import me.pride.spirits.Spirits;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
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
	private static final Map<UUID, BendingBossBar> BOSS_BARS = new HashMap<>();
	
	private BossBar bossBar;
	private double length;
	
	private double progress;
	
	public BendingBossBar(String title, BarColor barColor, double length, int begin, boolean startup, long startupTime, Player... players) {
		this.length = length;
		this.bossBar = Bukkit.createBossBar(title, barColor, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC, BarFlag.CREATE_FOG);
		
		if (startup) {
			new BarTimer(begin, ((startupTime / 1000.0) / 0.05), this.bossBar);
		} else {
			this.progress = begin;
			this.bossBar.setProgress(this.progress);
		}
		for (Player player : players) {
			this.bossBar.setVisible(true);
			this.bossBar.addPlayer(player);
			BOSS_BARS.put(player.getUniqueId(), this);
		}
	}
	public BendingBossBar(String title, BarColor barColor, double length, int begin, Player... players) {
		this(title, barColor, length, begin, false, 0, players);
	}
	public void update(double segment) {
		if (progress <= 0) {
			return;
		}
		segment = segment / length;
		progress = progress <= 0 ? 0 : progress - segment;
		
		bossBar.setProgress(progress);
	}
	public BossBar bossBar() {
		return this.bossBar;
	}
	public static BendingBossBar of(UUID uuid) {
		return BOSS_BARS.get(uuid);
	}
	public static boolean hasBossBar(UUID uuid) {
		return BOSS_BARS.containsKey(uuid);
	}
	public void remove() {
		BOSS_BARS.keySet().removeIf(BOSS_BARS::containsKey);
	}
	public static void updateTimer() {
		BarTimer.update();
	}
}

class BarTimer {
	private static final Set<BarTimer> TIMER = new HashSet<>();
	private int start;
	private double progress, timer;
	private BossBar bossBar;
	
	public BarTimer(int start, double progress, BossBar bossBar) {
		this.start = start; this.progress = progress; this.bossBar = bossBar;
		TIMER.add(this);
	}
	private int start() { return this.start; }
	private double progress() { return this.progress; }
	private double timer() { return this.timer; }
	public BossBar bossBar() { return this.bossBar; }
	
	public static void update() {
		TIMER.removeIf(timer -> {
			timer.bossBar().setProgress(timer.timer);
			switch (timer.start) {
				case 0 -> { timer.timer -= timer.progress;
					if (timer.timer <= 0) {
						timer.timer = 0;
						return true;
					}
				}
				case 1 -> { timer.timer += timer.progress;
					if (timer.timer >= 1) {
						timer.timer = 1;
						return true;
					}
				}
			}
			return false;
		});
	}
}
package me.pride.spirits.game.behavior;

import me.pride.spirits.Spirits;
import me.pride.spirits.game.AncientSoulweaver;
import me.pride.spirits.game.AncientSoulweaver.Phase;
import me.pride.spirits.util.BendingBossBar;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class NightmareNature extends Behavior {
	@Override
	public String toString() {
		return "Nightmare";
	}
	@Override
	public Optional<BehaviorRecord> behavioralRecord() {
		return Optional.of(new BehaviorRecord(new SummonWraith(), new SummonObelisk(), new CauseInsanity()));
	}
	
	static class NightmareCycle {
		private long cycle;
		private Phase phase;
		private Pair[] attributes;
		
		protected NightmareCycle() {
			this.cycle = System.currentTimeMillis() + (ThreadLocalRandom.current().nextInt(8, 14) * 1000);
		}
		protected void nightmareCycle(AncientSoulweaver soulweaver) {
			if (System.currentTimeMillis() > cycle) {
				if (soulweaver.phase() != Phase.NIGHTMARE) {
					switch (soulweaver.phase()) {
						case PROTECTOR:
							phase = Phase.PROTECTOR;
							break;
						case TERROR:
							phase = Phase.TERROR;
							break;
					}
					this.attributes = new Pair[]{
							Pair.of(Attribute.GENERIC_ATTACK_DAMAGE, soulweaver.entity().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue()),
							Pair.of(Attribute.GENERIC_FOLLOW_RANGE, soulweaver.entity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getValue()),
							Pair.of(Attribute.GENERIC_KNOCKBACK_RESISTANCE, soulweaver.entity().getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).getValue()),
							Pair.of(Attribute.GENERIC_MOVEMENT_SPEED, soulweaver.entity().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue())
					};
					soulweaver.entity().getWorld().playSound(soulweaver.entity().getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 0.5F, 0.75F);
					
					BendingBossBar.reset(AncientSoulweaver.ANCIENT_SOULWEAVER_BAR_KEY,
							ChatColor.of("#E06969") + "" + ChatColor.BOLD + "Ancient Soulweaver", BarColor.RED, 1000, BarFlag.DARKEN_SKY, BarFlag.CREATE_FOG);

					updateNightmareAttributes(soulweaver);
					
					new BukkitRunnable() {
						@Override
						public void run() {
							if (!soulweaver.healthAtNightmare()) {
								BendingBossBar.reset(AncientSoulweaver.ANCIENT_SOULWEAVER_BAR_KEY, AncientSoulweaver.NAME, BarColor.BLUE, 1000, BarFlag.CREATE_FOG, BarFlag.PLAY_BOSS_MUSIC);
								resetCycle(soulweaver);
							}
							cancel();
						}
					}.runTaskLater(Spirits.instance, 100);
				}
				soulweaver.setPhase(Phase.NIGHTMARE);
			}
		}
		private void updateNightmareAttributes(AncientSoulweaver soulweaver) {
			for (Pair<Attribute, Double> couple : attributes) {
				Attribute attribute = couple.getLeft();
				AttributeInstance instance = soulweaver.entity().getAttribute(attribute);
				double value = instance.getValue();
				
				switch (couple.getLeft()) {
					case GENERIC_ATTACK_DAMAGE -> { value = value * 1.5 > 2048.0 ? 2048.0 : value * 1.5;
						break;
					}
					case GENERIC_FOLLOW_RANGE -> { value = value * 3 > 2048.0 ? 2048.0 : value * 3;
						break;
					}
					case GENERIC_KNOCKBACK_RESISTANCE -> { value = 0.85;
						break;
					}
					case GENERIC_MOVEMENT_SPEED -> { value = value * 1.5 > 1024.0 ? 1024.0 : value * 1.5;
						break;
					}
				}
				instance.setBaseValue(value);
			}
		}
		private void resetAttributes(AncientSoulweaver soulweaver) {
			for (Pair<Attribute, Double> couple : attributes) {
				soulweaver.entity().getAttribute(couple.getLeft()).setBaseValue(couple.getRight());
			}
		}
		private void resetCycle(AncientSoulweaver soulweaver) {
			soulweaver.setPhase(phase);
			resetAttributes(soulweaver);
			this.cycle = System.currentTimeMillis() + (ThreadLocalRandom.current().nextInt(8, 14) * 1000);
		}
	}
	class SummonWraith extends AttackerAct {
		@Override
		public String name() {
			return "SummonWraith";
		}
		protected SummonWraith() {
		}
	}
	class SummonObelisk extends AttackerAct {
		@Override
		public String name() {
			return "SummonObelisk";
		}
	}
	class CauseInsanity extends AttackerAct {
		@Override
		public String name() {
			return "CauseInsanity";
		}
	
	}
}

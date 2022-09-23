package me.pride.spirits.game;

import com.projectkorra.projectkorra.GeneralMethods;
import me.pride.spirits.Spirits;
import me.pride.spirits.game.behavior.Behaviors;
import me.pride.spirits.util.BendingBossBar;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AncientSoulweaver {
	public static final NamespacedKey ANCIENT_SOULWEAVER_BAR_KEY = new NamespacedKey(Spirits.instance, "soulweaverbar");
	public static final NamespacedKey ANCIENT_SOULWEAVER_KEY = new NamespacedKey(Spirits.instance, "ancientsoulweaver");
	public static final String NAME = ChatColor.of("#416485") + "" + ChatColor.BOLD + "Ancient Soulweaver";
	
	public static Pair<Warden, Optional<AncientSoulweaver>> ANCIENT_SOULWEAVER = Pair.of(null, Optional.empty());
	
	public enum Phase { PROTECTOR, TERROR, NIGHTMARE; }
	private Phase phase;
	
	private Warden entity;
	private Behaviors behaviors;
	
	public AncientSoulweaver(Warden entity, Consumer<Warden> consumer) {
		if (BendingBossBar.exists(ANCIENT_SOULWEAVER_BAR_KEY) && ANCIENT_SOULWEAVER.getRight().isPresent()) {
			return;
		}
		this.phase = Phase.PROTECTOR;
		this.entity = entity;
		this.entity.getPersistentDataContainer().set(ANCIENT_SOULWEAVER_KEY, PersistentDataType.STRING, this.entity.getUniqueId().toString());
		consumer.accept(this.entity);
		
		this.behaviors = new Behaviors();
		this.behaviors.setupTree();
		ANCIENT_SOULWEAVER = Pair.of(this.entity, Optional.of(this));
	}
	public AncientSoulweaver(Warden entity) {
		this(entity, warden -> {
			double max = warden.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
			warden.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(max * 2);
			warden.setHealth(warden.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		});
	}
	public Phase phase() {
		return this.phase;
	}
	public Phase protectorPhase() { return Phase.PROTECTOR; }
	public Phase terrorPhase() { return Phase.TERROR; }
	public Phase nightmarePhase() { return Phase.NIGHTMARE; }
	
	public boolean inProtectorPhase() {
		return this.phase == Phase.PROTECTOR;
	}
	public boolean inTerrorPhase() {
		return this.phase == Phase.TERROR;
	}
	public boolean inNightmarePhase() {
		return this.phase == Phase.NIGHTMARE;
	}
	public boolean healthAtProtector() {
		return entity.getHealth() >= healthPartition(3) * 2 && entity.getHealth() <= maxHealth();
	}
	public boolean healthAtTerror() {
		return entity.getHealth() >= healthPartition(3) && entity.getHealth() < healthPartition(3) * 2;
	}
	public boolean healthAtNightmare() {
		return entity.getHealth() >= 0 && entity.getHealth() < healthPartition(3);
	}
	public double maxHealth() {
		return entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
	}
	public Warden entity() {
		return this.entity;
	}
	public Behaviors actions() {
		return this.behaviors;
	}
	public boolean has(byte value) {
		return entity.getPersistentDataContainer().get(ANCIENT_SOULWEAVER_KEY, PersistentDataType.BYTE).byteValue() == value;
	}
	public void setPhase(Phase phase) {
		this.phase = phase;
	}
	public void setNightmarePhase(boolean natural, Consumer<Boolean> ifNatural) {
		this.phase = Phase.NIGHTMARE;
		ifNatural.accept(natural);
	}
	private double healthPartition(int partition) {
		return Math.ceil((maxHealth() / partition));
	}
	public static Optional<AncientSoulweaver> of(Warden warden) {
		if (ANCIENT_SOULWEAVER.getLeft() == null) return Optional.empty();
		
		if (ANCIENT_SOULWEAVER.getLeft().getUniqueId() == warden.getUniqueId()) {
			return ANCIENT_SOULWEAVER.getRight();
		}
		return Optional.empty();
	}
	public static void addExistingSoulweaver(Warden warden) {
		new AncientSoulweaver(warden);
	}
	public static void remove() {
		Optional<AncientSoulweaver> right = ANCIENT_SOULWEAVER.getRight();
		right.ifPresent(soulweaver -> soulweaver = null);
		right = Optional.empty();
	}
	public static void manageAI() {
		ANCIENT_SOULWEAVER.getRight().ifPresent(soulweaver -> {
			Behaviors actions = soulweaver.actions();
			Warden entity = soulweaver.entity();
			
			switch (soulweaver.phase()) {
				case PROTECTOR -> {
					if (soulweaver.healthAtTerror()) {
						soulweaver.setPhase(Phase.TERROR);
					}
					break;
				}
				case TERROR -> {
					if (soulweaver.healthAtNightmare()) {
						soulweaver.setPhase(Phase.NIGHTMARE);
					}
					break;
				}
			}
			actions.manageBehavior(soulweaver);
			entity.getWorld().spawnParticle(Particle.FLASH, entity.getLocation().clone().add(0, 1, 0), 1, 0, 0, 0);
		});
	}
}
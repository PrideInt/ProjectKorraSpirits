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

// TODO: warden entity representing ancient soulweaver stops being associated with it when player logs
public class AncientSoulweaver {
	public static final NamespacedKey ANCIENT_SOULWEAVER_BAR_KEY = new NamespacedKey(Spirits.instance, "soulweaverbar");
	public static final NamespacedKey ANCIENT_SOULWEAVER_KEY = new NamespacedKey(Spirits.instance, "ancientsoulweaver");
	public static final String NAME = ChatColor.of("#416485") + "" + ChatColor.BOLD + "Ancient Soulweaver";
	
	private static Optional<AncientSoulweaver> ANCIENT_SOULWEAVER = Optional.empty();
	
	public enum Phase { GRACE, PROTECTOR, TERROR, NIGHTMARE; }
	private Phase phase;
	
	private Warden entity;
	private Behaviors behaviors;
	private long grace;
	
	public AncientSoulweaver(Warden entity, Consumer<Warden> consumer) {
		if (BendingBossBar.exists(ANCIENT_SOULWEAVER_BAR_KEY) && ANCIENT_SOULWEAVER.isPresent()) {
			return;
		}
		this.phase = Phase.GRACE;
		this.entity = entity;
		this.entity.getPersistentDataContainer().set(ANCIENT_SOULWEAVER_KEY, PersistentDataType.STRING, this.entity.getUniqueId().toString());
		consumer.accept(this.entity);
		
		this.behaviors = new Behaviors();
		this.behaviors.setupTree();
		
		this.grace = System.currentTimeMillis() + 4000;
		
		ANCIENT_SOULWEAVER = Optional.of(this);
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
	
	public boolean inGracePeriod() { return this.phase == Phase.GRACE; }
	public boolean inProtectorPhase() {
		return this.phase == Phase.PROTECTOR;
	}
	public boolean inTerrorPhase() {
		return this.phase == Phase.TERROR;
	}
	public boolean inNightmarePhase() {
		return this.phase == Phase.NIGHTMARE;
	}
	
	public long gracePeriod() { return this.grace; }
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
	public void setPhase(Phase phase) {
		this.phase = phase;
	}
	private double healthPartition(int partition) {
		return Math.ceil((maxHealth() / partition));
	}
	public static Optional<AncientSoulweaver> of(Warden warden) {
		if (!ANCIENT_SOULWEAVER.isPresent()) {
			return Optional.empty();
		}
		if (ANCIENT_SOULWEAVER.get().entity().getUniqueId() == warden.getUniqueId()) {
			return ANCIENT_SOULWEAVER;
		}
		return Optional.empty();
	}
	public static void addExistingSoulweaver(Warden warden) {
		new AncientSoulweaver(warden);
	}
	public void remove() {
		AncientSoulweaver soulweaver = this;
		soulweaver.entity().getPersistentDataContainer().remove(ANCIENT_SOULWEAVER_KEY);
		soulweaver.entity().remove();
		soulweaver = null;
	}
	public static void manageAI() {
		ANCIENT_SOULWEAVER.ifPresent(soulweaver -> {
			Behaviors actions = soulweaver.actions();
			Warden entity = soulweaver.entity();
			
			switch (soulweaver.phase()) {
				case GRACE -> {
					entity.getWorld().spawnParticle(Particle.FLASH, entity.getLocation().clone().add(0, 1, 0), 1, 0, 0, 0);
					if (System.currentTimeMillis() > soulweaver.gracePeriod()) {
						soulweaver.setPhase(Phase.PROTECTOR);
					}
					break;
				}
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
		});
	}
}
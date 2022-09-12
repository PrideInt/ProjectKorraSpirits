package me.pride.spirits.game;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.util.ParticleEffect;
import me.pride.spirits.Spirits;
import me.pride.spirits.util.BendingBossBar;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
	private SoulweaverAI ai;
	
	public AncientSoulweaver(Warden entity, Consumer<Warden> consumer) {
		if (BendingBossBar.exists(ANCIENT_SOULWEAVER_BAR_KEY) && ANCIENT_SOULWEAVER.getRight().isPresent()) {
			return;
		}
		this.phase = Phase.PROTECTOR;
		this.entity = entity;
		this.entity.getPersistentDataContainer().set(ANCIENT_SOULWEAVER_KEY, PersistentDataType.STRING, this.entity.getUniqueId().toString());
		consumer.accept(this.entity);
		
		this.ai = new SoulweaverAI(this);
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
	public SoulweaverAI actions() {
		return this.ai;
	}
	public boolean has(byte value) {
		return entity.getPersistentDataContainer().get(ANCIENT_SOULWEAVER_KEY, PersistentDataType.BYTE).byteValue() == value;
	}
	public void setPhase(Phase phase) {
		this.phase = phase;
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
			SoulweaverAI actions = soulweaver.actions();
			Warden entity = soulweaver.entity();
			
			actions.nightmareCycle();
			entity.getWorld().spawnParticle(Particle.FLASH, entity.getLocation().clone().add(0, 1, 0), 1, 0, 0, 0);
			
			switch (soulweaver.phase()) {
				case PROTECTOR -> { actions.doProtectorPhase();
					if (soulweaver.healthAtTerror()) {
						soulweaver.setPhase(Phase.TERROR);
					}
					break;
				}
				case TERROR -> { actions.doTerrorPhase();
					if (soulweaver.healthAtNightmare()) {
						actions.doNightmarePhase(false);
						soulweaver.setPhase(Phase.NIGHTMARE);
					}
					break;
				}
				case NIGHTMARE -> {
					if (!actions.naturalNightmare()) {
						actions.doNightmarePhase(false);
					}
					break;
				}
			}
		});
	}
}

// TODO: seperate these damn AIs
class SoulweaverAI {
	private AncientSoulweaver soulweaver;
	private Warden entity;
	private AncientSoulweaver.Phase phase;
	
	private long nightmareCycle;
	
	private Optional<Forcefield> forcefield;
	private boolean forcefieldRemoval;
	private long forcefieldTimer;
	
	private long timer, regenerationTime;
	
	private boolean naturalNightmare;
	
	private Pair<Attribute, Double>[] attributes;
	
	public SoulweaverAI(AncientSoulweaver soulweaver) {
		this.soulweaver = soulweaver;
		this.entity = this.soulweaver.entity();
		this.phase = this.soulweaver.phase();
		
		this.forcefield = Optional.empty();
		this.nightmareCycle = System.currentTimeMillis() + 20000;
		this.naturalNightmare = true;
		
		this.attributes = new Pair[] {
				Pair.of(Attribute.GENERIC_ATTACK_DAMAGE, this.soulweaver.entity().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue()),
				Pair.of(Attribute.GENERIC_FOLLOW_RANGE, this.soulweaver.entity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getValue()),
				Pair.of(Attribute.GENERIC_KNOCKBACK_RESISTANCE, this.soulweaver.entity().getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).getValue()),
				Pair.of(Attribute.GENERIC_MOVEMENT_SPEED, this.soulweaver.entity().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue())
		};
	}
	
	public void nightmareCycle() {
		if (naturalNightmare) {
			if (System.currentTimeMillis() > nightmareCycle) {
				System.out.println("NIGHTMARE PHASE");
				if (phase == soulweaver.phase()) {
					switch (soulweaver.phase()) {
						case PROTECTOR: phase = AncientSoulweaver.Phase.PROTECTOR; break;
						case TERROR: phase = AncientSoulweaver.Phase.TERROR; break;
					}
					new BukkitRunnable() {
						@Override
						public void run() {
							resetCycle();
							cancel();
						}
					}.runTaskLater(Spirits.instance, 100);
				}
				soulweaver.setPhase(AncientSoulweaver.Phase.NIGHTMARE);
				doNightmarePhase(true);
			}
		}
	}
	public void resetCycle() {
		nightmareCycle = System.currentTimeMillis() + 20000;
		System.out.println("PHASE RESET, " + phase);
		soulweaver.setPhase(phase);
		resetAttributes();
	}
	
	/*
		- PROTECTOR PHASE -
	 */
	public void doProtectorPhase() {
		List<LivingEntity> entities = GeneralMethods.getEntitiesAroundPoint(entity.getLocation(), 3)
				.stream().filter(e -> e.getUniqueId() != entity.getUniqueId() && e instanceof LivingEntity)
				.map(e -> (LivingEntity) e).collect(Collectors.toList());
		
		forcefield.ifPresent(ff -> ff.createSphere(entities));
		
		if (!entities.isEmpty()) {
			forcefield(entities);
		}
		if (forcefieldRemoval) {
			if (System.currentTimeMillis() > forcefieldTimer) {
				forcefieldRemoval = false;
			}
		}
	}
	private void forcefield(List<LivingEntity> entities) {
		if (forcefieldRemoval) return;
		
		if (!forcefield.isPresent()) {
			forcefield = Optional.of(new Forcefield(this));
		}
	}
	
	class Forcefield {
		private SoulweaverAI ai; private double size; private Location location;
		public Forcefield(SoulweaverAI ai) {
			this.ai = ai; this.size = 0; this.location = ai.entity.getLocation().clone().add(0, 1, 0);
		}
		public void createSphere(List<LivingEntity> entities) {
			size += 0.5;
			for (double i = 0; i <= Math.PI; i += Math.PI / 15) {
				double y = size * Math.cos(i);
				
				for (double j = 0; j <= 2 * Math.PI; j += Math.PI / 30) {
					double x = size * Math.cos(j) * Math.sin(i);
					double z = size * Math.sin(j) * Math.sin(i);
					
					location.add(x, y, z);
					entities.forEach(e -> {
						if (GeneralMethods.locationEqualsIgnoreDirection(e.getLocation(), location)) return;
						
						e.setVelocity(location.getDirection().setY(1.5).multiply(1));
						e.damage(2);
					});
					if (ThreadLocalRandom.current().nextInt(20) == 0) {
						location.getWorld().spawnParticle(Particle.GLOW, location, 1, 0, 0, 0, 0);
					}
					location.subtract(x, y, z);
				}
			}
			if (size > 5) {
				System.out.println("FORCEFIELD OVER");
				ai.forcefieldRemoval = true;
				ai.forcefieldTimer = System.currentTimeMillis() + 4000;
				ai.forcefield = Optional.empty();
				Forcefield forcefield = this;
				forcefield = null;
			}
		}
	}
	
	/*
		- TERROR PHASE -
	 */
	public void doTerrorPhase() {
	
	}
	public boolean beginRegenerating() {
		return System.currentTimeMillis() > regenerationTime;
	}
	public void resetRegenerationTimer() {
		regenerationTime = System.currentTimeMillis() + 10000;
	}
	
	/*
		- NIGHTMARE PHASE -
	 */
	public void doNightmarePhase(boolean natural) {
		if (!natural) {
			if (naturalNightmare) naturalNightmare = false;
		}
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(entity.getLocation(), 8)) {
			if (e.getUniqueId() != entity.getUniqueId() && e instanceof Player) {
				Player player = (Player) e;
				
				if (!player.hasMetadata("soulweaver:restricted")) {
					player.setMetadata("soulweaver:restricted", new FixedMetadataValue(Spirits.instance, 0));
				}
			}
		}
	}
	public void updateNightmareAttributes() {
		for (Pair<Attribute, Double> couple : attributes) {
			Attribute attribute = couple.getLeft();
			AttributeInstance instance = entity.getAttribute(attribute);
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
	public void resetAttributes() {
		for (Pair<Attribute, Double> couple : attributes) {
			entity.getAttribute(couple.getLeft()).setBaseValue(couple.getRight());
		}
	}
	public boolean naturalNightmare() {
		return naturalNightmare;
	}
}
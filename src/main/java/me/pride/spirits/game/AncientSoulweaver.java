package me.pride.spirits.game;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.util.ParticleEffect;
import me.pride.spirits.Spirits;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AncientSoulweaver {
	public static final NamespacedKey ANCIENT_SOULWEAVER_BAR_KEY = new NamespacedKey(Spirits.instance, "soulweaverbar");
	public static final NamespacedKey ANCIENT_SOULWEAVER_KEY = new NamespacedKey(Spirits.instance, "ancientsoulweaver");
	public static final String NAME = ChatColor.of("#6E34EB") + "" + ChatColor.BOLD + "Ancient Soulweaver";
	private static final Set<AncientSoulweaver> SOULWEAVERS = new HashSet<>();
	
	public enum Phase { PROTECTOR, TERROR, NIGHTMARE; }
	private Phase phase;
	
	private static byte VALUE = 0x0;
	
	private Warden entity;
	private SoulweaverAI ai;
	
	public AncientSoulweaver(Warden entity, Consumer<Warden> consumer) {
		if (VALUE == 0x7F) {
			return;
		}
		SOULWEAVERS.add(this);
		
		this.phase = Phase.PROTECTOR;
		this.entity = entity;
		this.entity.getPersistentDataContainer().set(ANCIENT_SOULWEAVER_KEY, PersistentDataType.BYTE, VALUE);
		consumer.accept(this.entity);
		
		VALUE++;
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
	public static void remove(byte value) {
		SOULWEAVERS.removeIf(soulwvr -> soulwvr.has(value));
	}
	public static void updateBitValue(byte value) {
		if (VALUE + value < 0x0) {
			VALUE = 0x0;
		} else if (VALUE + value > 0x7F) {
			VALUE = 0x7F;
		} else {
			VALUE += value;
		}
	}
	public static void manageAI() {
		SOULWEAVERS.iterator().forEachRemaining(soulweaver -> {
			SoulweaverAI actions = soulweaver.actions();
			Warden entity = soulweaver.entity();
			
			actions.nightmareCycle();
			
			switch (soulweaver.phase()) {
				case PROTECTOR -> { actions.doProtectorPhase();
					if (soulweaver.healthAtTerror()) {
						soulweaver.setPhase(Phase.TERROR);
					}
					break;
				}
				case TERROR -> { actions.doTerrorPhase();
					if (soulweaver.healthAtNightmare()) {
						soulweaver.setPhase(Phase.NIGHTMARE);
					}
					break;
				}
				case NIGHTMARE -> { actions.doNightmarePhase(false);
					break;
				}
			}
		});
	}
}

class SoulweaverAI {
	private AncientSoulweaver soulweaver;
	private Warden entity;
	private AncientSoulweaver.Phase phase;
	
	private long nightmareCycle;
	
	private boolean forcefield;
	private long forcefieldTimer;
	
	private long timer, regenerationTime;
	
	private boolean naturalNightmare;
	
	private Pair<Attribute, Double>[] attributes;
	
	public SoulweaverAI(AncientSoulweaver soulweaver) {
		this.soulweaver = soulweaver;
		this.entity = this.soulweaver.entity();
		this.phase = this.soulweaver.phase();
		
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
				if (phase != soulweaver.nightmarePhase()) {
					phase = soulweaver.phase();
				}
				soulweaver.setPhase(AncientSoulweaver.Phase.NIGHTMARE);
				doNightmarePhase(true);
				
				new BukkitRunnable() {
					@Override
					public void run() {
						resetCycle();
						cancel();
					}
				}.runTaskLater(Spirits.instance, 40);
			}
		}
	}
	public void resetCycle() {
		nightmareCycle = System.currentTimeMillis() + 30000;
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
		
		if (!entities.isEmpty()) {
			forcefield(entities);
		}
		if (forcefield) {
			forcefieldTimer = System.currentTimeMillis() + 4500;
			
			if (System.currentTimeMillis() > forcefieldTimer) {
				forcefield = false;
			}
		}
	}
	private void forcefield(List<LivingEntity> entities) {
		if (forcefield) return;
		
		new Forcefield(soulweaver.entity().getLocation().clone().add(0, 1, 0)).createSphere(entities, remove -> {
			if (remove) forcefield = true;
		});
	}
	
	class Forcefield {
		private double size; private Location location;
		public Forcefield(Location location) {
			this.size = 0; this.location = location;
		}
		public void createSphere(List<LivingEntity> entities, Consumer<Boolean> remove) {
			size += 0.75;
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
					if (ThreadLocalRandom.current().nextInt(80) == 0) {
						location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, location, 1, 0, 0, 0);
					}
					location.subtract(x, y, z);
				}
			}
			if (size > 5) {
				remove.accept(true);
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
}
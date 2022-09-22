package me.pride.spirits.game.behavior;

import com.projectkorra.projectkorra.GeneralMethods;
import me.pride.spirits.game.AncientSoulweaver;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Deprecated
public class ProtectorNature extends Behavior {
	@Override
	public Optional<BehaviorRecord> behavioralRecord() {
		return Optional.of(new BehaviorRecord(new Forcefield(), new HealAct()));
	}
	
	class Forcefield extends DefenderAct {
		private double size;
		private Location location;
		
		public Forcefield() {
			this.size = 0;
		}
		@Override
		public String name() {
			return "Forcefield";
		}
		@Override
		public void doAction(AncientSoulweaver soulweaver) {
			super.doAction(soulweaver);
			
			List<LivingEntity> entities = GeneralMethods.getEntitiesAroundPoint(soulweaver.entity().getLocation(), 3)
					.stream().filter(e -> e.getUniqueId() != soulweaver.entity().getUniqueId() && e instanceof LivingEntity)
					.map(e -> (LivingEntity) e).collect(Collectors.toList());
			
			createSphere(entities);
		}
		public void setLocation(Location location) {
			this.location = location.clone().add(0, 1, 0);
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
				addCooldown(4000);
				remove();
			}
		}
	}
	class RandomHeal extends HealAct {
		@Override
		public void doAction(AncientSoulweaver soulweaver) {
			super.doAction(soulweaver);
			
			double health = soulweaver.entity().getHealth() + 6.0;
			health = health > soulweaver.maxHealth() ? soulweaver.maxHealth() : health;
			
			soulweaver.entity().setHealth(health);
			
			remove();
			addCooldown(6000);
		}
	}
}

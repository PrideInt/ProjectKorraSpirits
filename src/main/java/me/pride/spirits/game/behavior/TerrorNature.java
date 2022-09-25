package me.pride.spirits.game.behavior;

import com.projectkorra.projectkorra.GeneralMethods;
import me.pride.spirits.Spirits;
import me.pride.spirits.game.AncientSoulweaver;
import me.pride.spirits.util.Tools;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class TerrorNature extends Behavior {
	@Override
	public String toString() {
		return "Terror";
	}
	@Override
	public Optional<BehaviorRecord> behavioralRecord() {
		return Optional.of(new BehaviorRecord(new FangsCircle(), new FangsLine(), new HealingStasis()));
	}
	
	class FangsCircle extends AttackerAct {
		protected FangsCircle() { }
		@Override
		public String name() {
			return "FangsCircle";
		}
		@Override
		public void doAction(AncientSoulweaver soulweaver) {
			circle(soulweaver);
		}
		protected void circle(AncientSoulweaver soulweaver) {
			Tools.createCircle(soulweaver.entity().getLocation(), 6, 60, l -> {
				Block top = GeneralMethods.getTopBlock(l, 6);
				
				EvokerFangs fangs = (EvokerFangs) soulweaver.entity().getWorld().spawnEntity(top.getLocation().clone().add(0.5, 1.5, 0.5), EntityType.EVOKER_FANGS);
				fangs.setOwner(soulweaver.entity());
			});
			addCooldown(6000);
			remove();
		}
	}
	class FangsLine extends AttackerAct {
		private Location origin, location;
		private Vector direction;
		
		protected FangsLine() { }
		@Override
		public String name() {
			return "FangsLine";
		}
		@Override
		public void doAction(AncientSoulweaver soulweaver) {
			super.doAction(soulweaver);
			
			location.add(direction.multiply(0.3));
			
			if (location.distanceSquared(origin) > 20 * 20) {
				addCooldown(6000);
				remove();
			}
			for (Block block : GeneralMethods.getBlocksAroundPoint(location, 2.5)) {
				if (ThreadLocalRandom.current().nextInt(18) == 0) {
					Block top = GeneralMethods.getTopBlock(block.getLocation(), 6);
					
					EvokerFangs fangs = (EvokerFangs) soulweaver.entity().getWorld().spawnEntity(top.getLocation().clone().add(0.5, 1.5, 0.5), EntityType.EVOKER_FANGS);
					fangs.setOwner(soulweaver.entity());
				}
			}
		}
		protected void attune(AncientSoulweaver soulweaver) {
			LivingEntity target = soulweaver.entity().getEntityAngryAt();
			
			origin = soulweaver.entity().getLocation().clone();
			location = origin.clone();
			direction = GeneralMethods.getDirection(soulweaver.entity().getLocation(), target.getLocation());
		}
	}
	class HealingStasis extends HealAct {
		private long end;
		
		protected HealingStasis() { }
		@Override
		public String name() {
			return "HealingStasis";
		}
		@Override
		public void doAction(AncientSoulweaver soulweaver) {
			super.doAction(soulweaver);
			
			if (!soulweaver.entity().hasMetadata("healingstasis")) {
				soulweaver.entity().setMetadata("healingstatis", new FixedMetadataValue(Spirits.instance, 0));
			}
			soulweaver.entity().getWorld().spawnParticle(Particle.GLOW, soulweaver.entity().getLocation().clone().add(0, 1, 0), 5, 0.5 ,0.5, 0.5, 0.1);
			
			if (System.currentTimeMillis() > end) {
				soulweaver.entity().removeMetadata("healingstasis", Spirits.instance);
				addCooldown(12000);
				remove();
			}
		}
	}
}

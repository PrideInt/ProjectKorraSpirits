package me.pride.spirits.game.behavior;

import me.pride.spirits.game.AncientSoulweaver;

import java.util.Optional;

@Deprecated
public class TerrorNature extends Behavior {
	@Override
	public Optional<BehaviorRecord> behavioralRecord() {
		return Optional.of(new BehaviorRecord(new FangsCircle(), new FangsLine(), new HealingStasis()));
	}
	
	class FangsCircle extends AttackerAct {
	
	}
	class FangsLine extends AttackerAct {
	
	}
	class HealingStasis extends HealAct {
	
	}
}

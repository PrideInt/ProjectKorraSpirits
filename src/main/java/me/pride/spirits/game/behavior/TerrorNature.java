package me.pride.spirits.game.behavior;

import java.util.Optional;

@Deprecated
public class TerrorNature extends BehaviorAction {
	@Override
	public String type() {
		return "Terroring";
	}
	@Override
	public Optional<BehaviorRecord> behaviors() {
		return Optional.of(new BehaviorRecord(new FangsCircle(), new FangsLine(), new HealingStasis()));
	}
	
	class FangsCircle extends AttackerAct {
	
	}
	class FangsLine extends AttackerAct {
	
	}
	class HealingStasis extends HealAct {
	
	}
}

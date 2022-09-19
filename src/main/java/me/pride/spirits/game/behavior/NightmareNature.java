package me.pride.spirits.game.behavior;

import java.util.Optional;

@Deprecated
public class NightmareNature extends BehaviorAction {
	@Override
	public String type() {
		return "Nightmaring";
	}
	@Override
	public Optional<BehaviorRecord> behaviors() {
		return Optional.of(new BehaviorRecord(new SummonWraith(), new SummonObelisk(), new CauseInsanity()));
	}
	
	class NightmareCycle {
	
	}
	class SummonWraith extends AttackerAct {
		protected SummonWraith() {
		}
	}
	class SummonObelisk extends AttackerAct {
	
	}
	class CauseInsanity extends AttackerAct {
	
	}
}

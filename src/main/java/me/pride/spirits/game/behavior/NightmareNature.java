package me.pride.spirits.game.behavior;

import me.pride.spirits.game.AncientSoulweaver;

import java.util.Optional;

@Deprecated
public class NightmareNature extends Behavior {
	@Override
	public Optional<BehaviorRecord> behavioralRecord() {
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

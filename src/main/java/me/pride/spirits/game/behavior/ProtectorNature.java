package me.pride.spirits.game.behavior;

import java.util.Optional;

@Deprecated
public class ProtectorNature extends BehaviorAction {
	@Override
	public String type() {
		return "Protecting";
	}
	@Override
	public Optional<BehaviorRecord> behaviors() {
		return Optional.of(new BehaviorRecord(new Forcefield(), new HealAct()));
	}
	
	class Forcefield extends DefenderAct {
	
	}
	class RandomHeal extends HealAct {
	
	}
}

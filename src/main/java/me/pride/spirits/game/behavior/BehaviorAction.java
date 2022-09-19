package me.pride.spirits.game.behavior;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Deprecated
public abstract class BehaviorAction {
	public static final Map<Action, Long> ACTION_COOLDOWNS = new HashMap<>();
	
	public abstract String type();
	public abstract Optional<BehaviorRecord> behaviors();
}

@Deprecated
abstract class Action extends BehaviorAction {
	public boolean inCooldown() {
		return ACTION_COOLDOWNS.containsKey(this);
	}
	public void addCooldown(long cooldown) {
		ACTION_COOLDOWNS.put(this, cooldown);
	}
	@Override
	public String type() {
		return "";
	}
	@Override
	public Optional<BehaviorRecord> behaviors() {
		return Optional.empty();
	}
}

@Deprecated
class DefenderAct extends Action {
	@Override
	public String type() {
		return "Defending";
	}
}
@Deprecated
class AttackerAct extends Action {
	@Override
	public String type() {
		return "Attacking";
	}
}
@Deprecated
class HealAct extends Action {
	@Override
	public String type() {
		return "Healing";
	}
}

@Deprecated
record BehaviorRecord(BehaviorAction... actions) {
	public BehaviorAction[] actions() { return this.actions; }
}

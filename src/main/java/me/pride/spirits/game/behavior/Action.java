package me.pride.spirits.game.behavior;

import me.pride.spirits.game.AncientSoulweaver;

import java.util.*;

public abstract class Action extends BehaviorAction {
	private static final Map<Action, Long> ACTION_COOLDOWNS = new HashMap<>();
	private static final Set<Action> ACTIVE_ACTIONS = new HashSet<>();
	
	public abstract String name();
	
	public boolean inCooldown() {
		return ACTION_COOLDOWNS.containsKey(this);
	}
	public void addCooldown(long cooldown) {
		ACTION_COOLDOWNS.put(this, System.currentTimeMillis() + cooldown);
	}

	public void doAction(AncientSoulweaver soulweaver) {
		if (!ACTIVE_ACTIONS.contains(this)) {
			ACTIVE_ACTIONS.add(this);
		}
	}
	public boolean progressing() {
		return ACTIVE_ACTIONS.contains(this);
	}
	public void remove() {
		ACTIVE_ACTIONS.remove(this);
	}
	
	@Override
	public Optional<BehaviorRecord> behavioralRecord() {
		return Optional.empty();
	}
	
	public static void manageActionsAndCooldowns() {
		for (Iterator<Map.Entry<Action, Long>> itr = ACTION_COOLDOWNS.entrySet().iterator(); itr.hasNext();) {
			Map.Entry<Action, Long> entry = itr.next();
			
			if (System.currentTimeMillis() > entry.getValue()) {
				itr.remove();
			}
		}
	}
}

class DefenderAct extends Action {
	@Override
	public String name() {
		return "Defending";
	}
}
class AttackerAct extends Action {
	@Override
	public String name() {
		return "Attacking";
	}
}
class HealAct extends Action {
	@Override
	public String name() {
		return "Healing";
	}
}
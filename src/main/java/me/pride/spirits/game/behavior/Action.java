package me.pride.spirits.game.behavior;

import me.pride.spirits.game.AncientSoulweaver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class Action extends BehaviorAction {
	private static final Map<Action, Long> ACTION_COOLDOWNS = new HashMap<>();
	private static final Set<Action> ACTIVE_ACTIONS = new HashSet<>();
	
	public enum Act {
		DEFEND, ATTACK, HEAL, UTIL
	}
	
	public abstract String name();
	public abstract Act act();
	
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
	@Override
	public Act act() {
		return Act.DEFEND;
	}
}
class AttackerAct extends Action {
	@Override
	public String name() {
		return "Attacking";
	}
	@Override
	public Act act() {
		return Act.ATTACK;
	}
}
class HealAct extends Action {
	@Override
	public String name() {
		return "Healing";
	}
	@Override
	public Act act() {
		return Act.HEAL;
	}
}
class UtilAct extends Action {
	@Override
	public String name() {
		return "Utility";
	}
	@Override
	public Act act() {
		return Act.UTIL;
	}
}
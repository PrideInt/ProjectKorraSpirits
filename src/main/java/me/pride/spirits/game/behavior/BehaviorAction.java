package me.pride.spirits.game.behavior;

import java.util.Optional;

public abstract class BehaviorAction {
	public abstract Optional<BehaviorRecord> behavioralRecord();
}

record BehaviorRecord(BehaviorAction... actions) {
	public BehaviorAction[] actions() { return this.actions; }
}

package me.pride.spirits.game.behavior;

import me.pride.spirits.game.AncientSoulweaver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

@Deprecated
public abstract class BehaviorAction {
	public abstract Optional<BehaviorRecord> behavioralRecord();
}

@Deprecated
record BehaviorRecord(BehaviorAction... actions) {
	public BehaviorAction[] actions() { return this.actions; }
}

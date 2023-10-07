package me.pride.spirits.game.behavior;

import java.util.Optional;

public class Behavior extends BehaviorAction {
	@Override
	public Optional<BehaviorRecord> behavioralRecord() {
		return Optional.empty();
	}
}

package me.pride.spirits;

import me.pride.spirits.api.Spirit;
import me.pride.spirits.game.AncientSoulweaver;
import me.pride.spirits.util.BendingBossBar;
import me.pride.spirits.world.SpiritWorld;
import me.pride.spirits.game.behavior.Action;

public class SpiritsManager implements Runnable {
	@Override
	public void run() {
		Spirit.handle();
		SpiritWorld.handle();
		BendingBossBar.updateTimer();
		AncientSoulweaver.manageAI();
		Action.manageActionsAndCooldowns();
	}
}

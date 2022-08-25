package me.pride.spirits;

import me.pride.spirits.api.Spirit;
import me.pride.spirits.world.SpiritWorld;

public class SpiritsManager implements Runnable {
	@Override
	public void run() {
		Spirit.handle();
		SpiritWorld.handle();
	}
}

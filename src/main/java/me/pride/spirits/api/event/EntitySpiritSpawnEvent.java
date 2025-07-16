package me.pride.spirits.api.event;

import me.pride.spirits.api.Spirit;
import me.pride.spirits.api.SpiritType;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EntitySpiritSpawnEvent extends Event {
	public static final HandlerList HANDLER_LIST = new HandlerList();
	private Spirit spirit;
	
	public EntitySpiritSpawnEvent(final Spirit spirit) {
		this.spirit = spirit;
	}
	public Spirit getSpirit() {
		return this.spirit;
	}
	public Entity getEntity() {
		return this.spirit.entity();
	}
	public SpiritType getSpiritType() {
		return this.spirit.type();
	}
	public long getRevertTime() {
		return this.spirit.revertTime();
	}
	@Override
	public HandlerList getHandlers() {
		return HANDLER_LIST;
	}
	public static HandlerList getHandlerList() {
		return HANDLER_LIST;
	}
}

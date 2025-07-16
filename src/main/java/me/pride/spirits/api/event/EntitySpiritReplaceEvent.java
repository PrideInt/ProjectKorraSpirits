package me.pride.spirits.api.event;

import me.pride.spirits.api.Spirit;
import me.pride.spirits.api.SpiritType;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EntitySpiritReplaceEvent extends Event {
	public static final HandlerList HANDLER_LIST = new HandlerList();
	private Spirit oldSpirit;
	private Spirit newSpirit;
	
	public EntitySpiritReplaceEvent(final Spirit oldSpirit, final Spirit newSpirit) {
		this.oldSpirit = oldSpirit;
		this.newSpirit = newSpirit;
	}
	public Spirit getOldSpirit() {
		return this.oldSpirit;
	}
	public Spirit getNewSpirit() {
		return this.newSpirit;
	}
	public Entity getOriginalEntity() {
		return this.oldSpirit.entity();
	}
	public SpiritType getOriginalSpiritType() {
		return this.oldSpirit.type();
	}
	public long getOriginalRevertTime() {
		return this.oldSpirit.revertTime();
	}
	public Entity getNewEntity() {
		return this.newSpirit.entity();
	}
	public SpiritType getNewSpiritType() {
		return this.newSpirit.type();
	}
	public long getNewRevertTime() {
		return this.newSpirit.revertTime();
	}
	@Override
	public HandlerList getHandlers() {
		return HANDLER_LIST;
	}
	public static HandlerList getHandlerList() {
		return HANDLER_LIST;
	}
}

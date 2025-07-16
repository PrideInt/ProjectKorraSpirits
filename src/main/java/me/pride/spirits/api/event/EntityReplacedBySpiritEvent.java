package me.pride.spirits.api.event;

import me.pride.spirits.api.Spirit;
import me.pride.spirits.api.SpiritType;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EntityReplacedBySpiritEvent extends Event {
	public static final HandlerList HANDLER_LIST = new HandlerList();
	private Entity oldEntity;
	private Spirit newSpirit;
	
	public EntityReplacedBySpiritEvent(final Entity oldEntity, final Spirit newSpirit) {
		this.oldEntity = oldEntity;
		this.newSpirit = newSpirit;
	}
	public Entity getOldEntity() {
		return this.oldEntity;
	}
	public Spirit getNewSpirit() {
		return this.newSpirit;
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

package me.pride.spirits.api.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EntityTouchWaterEvent extends Event {
	public static final HandlerList HANDLER_LIST = new HandlerList();

	private Entity entity;
	private Block water;

	public EntityTouchWaterEvent(Entity entity, Block water) {
		this.entity = entity;
		this.water = water;
	}
	public Entity getEntity() {
		return this.entity;
	}
	public Block getWater() {
		return this.water;
	}
	@Override
	public HandlerList getHandlers() {
		return HANDLER_LIST;
	}
	public static HandlerList getHandlerList() {
		return HANDLER_LIST;
	}
}

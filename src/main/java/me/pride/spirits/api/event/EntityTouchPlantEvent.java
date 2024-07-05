package me.pride.spirits.api.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class EntityTouchPlantEvent extends Event {
	public static final HandlerList HANDLER_LIST = new HandlerList();

	private Entity entity;
	private Block plant;

	public EntityTouchPlantEvent(Entity entity, Block plant) {
		this.entity = entity;
		this.plant = plant;
	}
	public Entity getEntity() {
		return this.entity;
	}
	public Block getPlant() {
		return this.plant;
	}
	@NotNull
	@Override
	public HandlerList getHandlers() {
		return HANDLER_LIST;
	}
	public static HandlerList getHandlerList() {
		return HANDLER_LIST;
	}
}

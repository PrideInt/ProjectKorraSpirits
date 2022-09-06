package me.pride.spirits.api.event;

import me.pride.spirits.api.Spirit;
import me.pride.spirits.api.SpiritType;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class EntitySpiritRevertEvent extends Event {
	public static final HandlerList HANDLER_LIST = new HandlerList();
	private Spirit spirit;
	private long time;
	
	public EntitySpiritRevertEvent(final Spirit spirit, final long time) {
		this.spirit = spirit;
		this.time = time;
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
	public long getTimeAtRevert() {
		return this.time;
	}
	@NotNull
	@Override
	public HandlerList getHandlers() {
		return HANDLER_LIST;
	}
}

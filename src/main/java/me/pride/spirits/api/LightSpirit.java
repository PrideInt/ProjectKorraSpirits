package me.pride.spirits.api;

import com.projectkorra.projectkorra.BendingPlayer;
import me.pride.spirits.api.ability.SpiritElement;
import me.pride.spirits.util.Filter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class LightSpirit extends ReplaceableSpirit {
	public LightSpirit(World world, Location location, String name, EntityType entityType, long revertTime) {
		super(world, location, name, entityType, SpiritType.LIGHT, revertTime);
	}
	public LightSpirit(World world, Entity entity, String name, EntityType entityType, long revertTime) {
		super(world, entity, name, entityType, SpiritType.LIGHT, revertTime);
	}
	public LightSpirit(World world, Location location) {
		this(world, location, SpiritType.LIGHT.name(), SpiritType.LIGHT.entityType(), -1);
	}
	public LightSpirit(World world, Location location, EntityType entityType) {
		this(world, location, SpiritType.LIGHT.name(), entityType, -1);
	}
	public LightSpirit(World world, Location location, long revertTime) {
		this(world, location, SpiritType.LIGHT.name(), SpiritType.LIGHT.entityType(), revertTime);
	}
	public LightSpirit(World world, Location location, String name, EntityType entityType) {
		this(world, location, name, entityType, -1);
	}
	public LightSpirit(World world, Location location, String name, long revertTime) {
		this(world, location, name, SpiritType.LIGHT.entityType(), revertTime);
	}

	public static boolean isLightSpirit(Entity entity) {
		boolean filter = Filter.filterEntityLight(entity);

		if (entity.getType() == EntityType.PLAYER) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer((Player) entity);

			if (bPlayer != null) {
				if (bPlayer.hasElement(SpiritElement.LIGHT_SPIRIT)) {
					return true;
				}
			}
		}
		return filter;
	}
}

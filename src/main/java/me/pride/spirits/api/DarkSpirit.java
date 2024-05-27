package me.pride.spirits.api;

import com.projectkorra.projectkorra.BendingPlayer;
import me.pride.spirits.api.ability.SpiritElement;
import me.pride.spirits.util.Filter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class DarkSpirit extends ReplaceableSpirit {
	public DarkSpirit(World world, Location location, String name, EntityType entityType, long revertTime) {
		super(world, location, name, entityType, SpiritType.DARK, revertTime);
	}
	public DarkSpirit(World world, Entity entity, String name, EntityType entityType, long revertTime) {
		super(world, entity, name, entityType, SpiritType.DARK, revertTime);
	}
	public DarkSpirit(World world, Location location) {
		this(world, location, SpiritType.DARK.name(), SpiritType.DARK.entityType(), -1);
	}
	public DarkSpirit(World world, Location location, EntityType entityType) {
		this(world, location, SpiritType.DARK.name(), entityType, -1);
	}
	public DarkSpirit(World world, Location location, long revertTime) {
		this(world, location, SpiritType.DARK.name(), SpiritType.DARK.entityType(), revertTime);
	}
	public DarkSpirit(World world, Location location, String name, EntityType entityType) {
		this(world, location, name, entityType, -1);
	}
	public DarkSpirit(World world, Location location, String name, long revertTime) {
		this(world, location, name, SpiritType.DARK.entityType(), revertTime);
	}

	public static boolean isDarkSpirit(Entity entity) {
		boolean filter = Filter.filterEntityDark(entity);

		if (entity.getType() == EntityType.PLAYER) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer((Player) entity);

			if (bPlayer != null) {
				if (bPlayer.hasElement(SpiritElement.DARK_SPIRIT)) {
					return true;
				}
			}
		}
		return filter;
	}
}

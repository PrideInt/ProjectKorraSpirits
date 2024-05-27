package me.pride.spirits.api;

import com.projectkorra.projectkorra.BendingPlayer;
import me.pride.spirits.api.ability.SpiritElement;
import me.pride.spirits.util.Filter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class NeutralSpirit extends ReplaceableSpirit {
	public NeutralSpirit(World world, Location location, String name, EntityType entityType, long revertTime) {
		super(world, location, name, entityType, SpiritType.SPIRIT, revertTime);
	}
	public NeutralSpirit(World world, Entity entity, String name, EntityType entityType, long revertTime) {
		super(world, entity, name, entityType, SpiritType.SPIRIT, revertTime);
	}
	public NeutralSpirit(World world, Location location) {
		this(world, location, SpiritType.SPIRIT.name(), SpiritType.SPIRIT.entityType(), -1);
	}
	public NeutralSpirit(World world, Location location, EntityType entityType) {
		this(world, location, SpiritType.SPIRIT.name(), entityType, -1);
	}
	public NeutralSpirit(World world, Location location, long revertTime) {
		this(world, location, SpiritType.SPIRIT.name(), SpiritType.SPIRIT.entityType(), revertTime);
	}
	public NeutralSpirit(World world, Location location, String name, EntityType entityType) {
		this(world, location, name, entityType, -1);
	}
	public NeutralSpirit(World world, Location location, String name, long revertTime) {
		this(world, location, name, SpiritType.SPIRIT.entityType(), revertTime);
	}

	public static boolean isNeutralSpirit(Entity entity) {
		boolean filter = Filter.filterEntityNeutral(entity);

		if (entity.getType() == EntityType.PLAYER) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer((Player) entity);

			if (bPlayer != null) {
				if (bPlayer.hasElement(SpiritElement.SPIRIT)) {
					return true;
				}
			}
		}
		return filter;
	}
}

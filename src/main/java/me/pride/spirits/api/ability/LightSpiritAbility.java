package me.pride.spirits.api.ability;

import com.projectkorra.projectkorra.Element;
import me.pride.spirits.api.SpiritType;
import org.bukkit.entity.Player;

public abstract class LightSpiritAbility extends SpiritAbility {
	public LightSpiritAbility(Player player) {
		super(player);
	}
	@Override
	public boolean isExplosiveAbility() {
		return false;
	}
	@Override
	public boolean isIgniteAbility() {
		return false;
	}
	@Override
	public Element getElement() {
		return SpiritElement.LIGHT_SPIRIT;
	}
	public SpiritType getSpiritType() {
		return SpiritType.LIGHT;
	}
}

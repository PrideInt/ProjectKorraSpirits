package me.pride.spirits.api.ability;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import org.bukkit.entity.Player;

public abstract class LightSpiritAbility extends ElementalAbility {
	public LightSpiritAbility(Player player) {
		super(player);
	}
	@Override
	public Element getElement() {
		return SpiritElement.LIGHT_SPIRIT;
	}
}

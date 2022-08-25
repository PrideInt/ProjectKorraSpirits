package me.pride.spirits.game;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import org.bukkit.entity.Player;

public abstract class DarkSpiritAbility extends ElementalAbility {
	public DarkSpiritAbility(Player player) {
		super(player);
	}
	@Override
	public Element getElement() {
		return SpiritElement.DARK_SPIRIT;
	}
}

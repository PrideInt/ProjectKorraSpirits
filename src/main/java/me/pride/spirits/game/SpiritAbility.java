package me.pride.spirits.game;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import org.bukkit.entity.Player;

public abstract class SpiritAbility extends ElementalAbility {
	public SpiritAbility(Player player) {
		super(player);
	}
	@Override
	public Element getElement() { return SpiritElement.SPIRIT; }
}


package me.pride.spirits.api.ability;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import me.pride.spirits.api.SpiritType;
import org.bukkit.entity.Player;

public abstract class SpiritAbility extends ElementalAbility {
	public SpiritAbility(Player player) {
		super(player);
	}
	@Override
	public Element getElement() { return SpiritElement.SPIRIT; }
	public SpiritType getSpiritType() {
		return SpiritType.SPIRIT;
	}
}


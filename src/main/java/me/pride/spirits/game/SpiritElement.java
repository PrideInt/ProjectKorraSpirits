package me.pride.spirits.game;

import com.projectkorra.projectkorra.Element;
import me.pride.spirits.Spirits;
import org.bukkit.ChatColor;

public class SpiritElement {
	
	public static final Element SPIRIT = new Element("Spirit", Element.ElementType.NO_SUFFIX, Spirits.instance) {
		@Override
		public ChatColor getColor() { return ChatColor.DARK_AQUA; }
		@Override
		public ChatColor getSubColor() { return ChatColor.BLUE; }
	};
	public static final Element LIGHT_SPIRIT = new Element("LightSpirit", Element.ElementType.NO_SUFFIX, Spirits.instance) {
		@Override
		public ChatColor getColor() { return ChatColor.YELLOW; }
		@Override
		public ChatColor getSubColor() { return ChatColor.GOLD; }
	};
	public static final Element DARK_SPIRIT = new Element("DarkSpirit", Element.ElementType.NO_SUFFIX, Spirits.instance) {
		@Override
		public ChatColor getColor() { return ChatColor.DARK_GRAY; }
		@Override
		public ChatColor getSubColor() { return ChatColor.GRAY; }
	};
}

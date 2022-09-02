package me.pride.spirits.game;

import com.projectkorra.projectkorra.Element;
import me.pride.spirits.Spirits;
import net.md_5.bungee.api.ChatColor;

public class SpiritElement {
	
	public static final Element SPIRIT = new Element("Spirit", Element.ElementType.NO_SUFFIX, Spirits.instance) {
		@Override
		public ChatColor getColor() { return ChatColor.of("#92e8b6"); }
		@Override
		public ChatColor getSubColor() { return ChatColor.of("#319fb5"); }
	};
	public static final Element LIGHT_SPIRIT = new Element("LightSpirit", Element.ElementType.NO_SUFFIX, Spirits.instance) {
		@Override
		public ChatColor getColor() { return ChatColor.of("#f7f5a6"); }
		@Override
		public ChatColor getSubColor() { return ChatColor.of("#e2d3f5"); }
	};
	public static final Element DARK_SPIRIT = new Element("DarkSpirit", Element.ElementType.NO_SUFFIX, Spirits.instance) {
		@Override
		public ChatColor getColor() { return ChatColor.of("#846399"); }
		@Override
		public ChatColor getSubColor() { return ChatColor.of("#555555"); }
	};
}

package me.pride.spirits.api.ability;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.ElementType;
import me.pride.spirits.Spirits;
import net.md_5.bungee.api.ChatColor;

public class SpiritElement {
	public static final Element SPIRIT = new Element("Spirit", ElementType.NO_SUFFIX, Spirits.instance) {
		@Override
		public ChatColor getColor() { return ChatColor.of("#92e8b6"); }
		@Override
		public ChatColor getSubColor() { return ChatColor.of("#319fb5"); }
	};
	public static final Element LIGHT_SPIRIT = new Element("LightSpirit", ElementType.NO_SUFFIX, Spirits.instance) {
		@Override
		public ChatColor getColor() { return ChatColor.of("#f7f5a6"); }
		@Override
		public ChatColor getSubColor() { return ChatColor.of("#e2d3f5"); }
	};
	public static final Element DARK_SPIRIT = new Element("DarkSpirit", ElementType.NO_SUFFIX, Spirits.instance) {
		@Override
		public ChatColor getColor() { return ChatColor.of("#846399"); }
		@Override
		public ChatColor getSubColor() { return ChatColor.of("#555555"); }
	};
	public static boolean isSpiritElement(Element element) {
		return element.equals(SPIRIT) || element.equals(LIGHT_SPIRIT) || element.equals(DARK_SPIRIT);
	}
}

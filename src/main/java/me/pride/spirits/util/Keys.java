package me.pride.spirits.util;

import me.pride.spirits.Spirits;
import me.pride.spirits.api.ability.SpiritElement;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.NamespacedKey;

public class Keys {
	public static final NamespacedKey LIGHT_SPIRIT_KEY = new NamespacedKey(Spirits.instance, "lightspirit");
	public static final NamespacedKey DARK_SPIRIT_KEY = new NamespacedKey(Spirits.instance, "darkspirit");
	public static final NamespacedKey SPIRIT_KEY = new NamespacedKey(Spirits.instance, "spirit");
	public static final NamespacedKey REPLACED_KEY = new NamespacedKey(Spirits.instance, "replacedentity");
	public static final String LIGHT_SPIRIT_NAME = SpiritElement.LIGHT_SPIRIT.getColor() + "" + ChatColor.BOLD + "Light spirit";
	public static final String DARK_SPIRIT_NAME = SpiritElement.DARK_SPIRIT.getColor() + "" + ChatColor.BOLD + "Dark spirit";
	public static final String SPIRIT_NAME = SpiritElement.SPIRIT.getColor() + "" + ChatColor.BOLD + "Spirit";
	public static final String BLESSED_SOURCE = "spirits:blessed_source";
	public static final String BLESSED_ENTITY = "spirits:blessed_entity";
	public static final String CORRUPTED_SOURCE = "spirits:corrupted_source";
	public static final String ORB_KEY = "spirits:orb";
}

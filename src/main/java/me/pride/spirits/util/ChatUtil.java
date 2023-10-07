package me.pride.spirits.util;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.CoreAbility;
import me.pride.spirits.util.objects.Gradient;
import me.pride.spirits.util.objects.QuadraticInterpolator;

import java.awt.Color;

public class ChatUtil {
	public static String getAuthor() { return "Prride/prideyy"; }
	public static String getAuthor(Element element) {
		return Gradient.elementFromTo(getAuthor(), element);
	}
	public static String getAuthor(CoreAbility ability) { return getAuthor(ability.getElement()); };
	
	public static String getAuthor(String append) { return "Prride/prideyy" + append; }
	public static String getAuthor(Element element, String append) {
		return Gradient.elementFromTo(getAuthor(append), element);
	}
	public static String getAuthor(CoreAbility ability, String append) { return getAuthor(ability.getElement(), append); };
	
	public static String getVersion() {
		return Gradient.hsvGradient("VERSION 1 BETA", Color.decode("#a469d1"), Color.decode("#c2b478"), new QuadraticInterpolator());
	}
}
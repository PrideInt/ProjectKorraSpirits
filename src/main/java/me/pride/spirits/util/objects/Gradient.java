package me.pride.spirits.util.objects;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import net.md_5.bungee.api.ChatColor;

import java.awt.*;

/* ChatColor gradient in chat utilizing mathematical interpolation; resource by Schottky https://www.spigotmc.org/threads/gradient-chat-particles.470496/
 */
public class Gradient {
	
	public static String hsvGradient(String str, Color from, Color to, Interpolator interpolator) {
		// returns a float-array where hsv[0] = hue, hsv[1] = saturation, hsv[2] = value/brightness
		final float[] hsvFrom = Color.RGBtoHSB(from.getRed(), from.getGreen(), from.getBlue(), null);
		final float[] hsvTo = Color.RGBtoHSB(to.getRed(), to.getGreen(), to.getBlue(), null);
		
		final double[] h = interpolator.interpolate(hsvFrom[0], hsvTo[0], str.length());
		final double[] s = interpolator.interpolate(hsvFrom[1], hsvTo[1], str.length());
		final double[] v = interpolator.interpolate(hsvFrom[2], hsvTo[2], str.length());
		
		final StringBuilder builder = new StringBuilder();
		
		for (int i = 0 ; i < str.length(); i++) {
			builder.append(ChatColor.of(Color.getHSBColor((float) h[i], (float) s[i], (float) v[i]))).append(str.charAt(i));
		}
		return builder.toString();
	}
	
	public static String elementFromTo(String string, Element element) {
		if (element instanceof Element.SubElement) {
			Element.SubElement sub = (Element.SubElement) element;
			return hsvGradient(string, Color.decode(ConfigManager.languageConfig.get().getString("Chat.Colors." + sub.getParentElement().getName() + "Sub")), Color.decode(ConfigManager.languageConfig.get().getString("Chat.Colors." + sub.getParentElement().getName())), new QuadraticInterpolator().mode(false));
		} else {
			return hsvGradient(string, Color.decode(ConfigManager.languageConfig.get().getString("Chat.Colors." + element.getName())), Color.decode(ConfigManager.languageConfig.get().getString("Chat.Colors." + element.getName() + "Sub")), new QuadraticInterpolator().mode(false));
		}
	}
}

@FunctionalInterface
interface Interpolator {
	double[] interpolate(double from, double to, int max);
}
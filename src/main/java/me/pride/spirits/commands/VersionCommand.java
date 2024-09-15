package me.pride.spirits.commands;

import com.projectkorra.projectkorra.command.PKCommand;
import me.pride.spirits.Spirits;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class VersionCommand extends PKCommand {

	public VersionCommand() {
		super("spirits", "/bending spirits", "Shows information regarding Pride's ProjectKorra Spirits.", new String[] { "spirits", "sp" });
	}

	@Override
	public void execute(CommandSender commandSender, List<String> list) {
		if (!correctLength(commandSender, list.size(), 0, 1) || (!hasPermission(commandSender))) {
			return;
		}
		if (list.size() == 0) {
			info(commandSender);
		} else {
			help(commandSender, false);
		}
	}

	public static void info(CommandSender commandSender) {
		commandSender.sendMessage(ChatColor.of("#A3A3A3") + "Running ProjectKorraSpirits Build: " + ChatColor.BLUE + Spirits.instance.getVersion());
		commandSender.sendMessage(ChatColor.of("#A3A3A3") + "Developed by: " + ChatColor.BLUE + "Prride");
	}
}

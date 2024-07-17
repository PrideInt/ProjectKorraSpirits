package me.pride.spirits.commands;

import me.pride.spirits.storage.StorageCache;
import me.pride.spirits.world.SpiritWorld;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class SpiritWorldCommand implements CommandExecutor, TabCompleter {

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String string, String[] strings) {
		if (strings.length == 2) {
			if (strings[0].equalsIgnoreCase("create")) {
				if (StorageCache.spiritWorlds().contains(strings[1])) {
					commandSender.sendMessage("§cSpirit world already exists in world " + strings[0] + ".");
				} else {
					StorageCache.spiritWorlds().add(strings[1]);
					commandSender.sendMessage("§aSuccessfully created spirit world in world " + strings[0] + ".");
				}
				return true;
			} else if (strings[0].equalsIgnoreCase("remove")) {
				if (StorageCache.spiritWorlds().contains(strings[1])) {
					commandSender.sendMessage("§aSuccessfully removed spirit world in world " + strings[0] + ".");

					StorageCache.spiritWorlds().remove(strings[1]);
					SpiritWorld.remove(Bukkit.getWorld(strings[1]));
				} else {
					commandSender.sendMessage("§aSpirit world " + strings[1] + " not found.");
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String string, String[] strings) {
		if (strings.length == 1) {
			return List.of("create", "remove");
		} else if (strings.length == 2) {
			if (strings[0].equalsIgnoreCase("create")) {
				return Bukkit.getWorlds().stream().map(world -> world.getName()).toList();
			} else if (strings[0].equalsIgnoreCase("remove")) {
				return StorageCache.spiritWorlds();
			}
		}
		return new ArrayList<>();
	}
}

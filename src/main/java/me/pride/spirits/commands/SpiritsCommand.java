package me.pride.spirits.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Subcommand;
import me.pride.spirits.Spirits;
import me.pride.spirits.abilities.light.passives.Orbs;
import me.pride.spirits.api.ability.SpiritElement;
import me.pride.spirits.storage.StorageCache;
import me.pride.spirits.util.objects.Gradient;
import me.pride.spirits.util.objects.LinearInterpolator;
import me.pride.spirits.util.objects.QuadraticInterpolator;
import me.pride.spirits.world.SpiritWorld;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.Color;

@CommandAlias("spirits")
public class SpiritsCommand extends BaseCommand {

	public static BukkitCommandManager commandManager = new BukkitCommandManager(Spirits.instance);

	@Subcommand("world create")
	@CommandCompletion("@worlds")
	public void onSpiritWorldCreate(Player player, String spiritWorld) {
		if (StorageCache.spiritWorlds().contains(spiritWorld)) {
			player.sendMessage("§cSpirit world already exists in world " + player.getWorld().getName() + ".");
		} else {
			StorageCache.spiritWorlds().add(spiritWorld);
			player.sendMessage("§aSuccessfully created spirit world in world " + player.getWorld().getName() + ".");
		}
	}

	@Subcommand("world delete")
	@CommandCompletion("@spiritWorlds")
	public void onSpiritWorldDelete(Player player, String spiritWorlds) {
		if (StorageCache.spiritWorlds().contains(spiritWorlds)) {
			player.sendMessage("§aSuccessfully removed spirit world in world " + player.getWorld().getName() + ".");

			StorageCache.spiritWorlds().remove(spiritWorlds);
			SpiritWorld.remove(Bukkit.getWorld(spiritWorlds));
		} else {
			player.sendMessage("§aSpirit world " + spiritWorlds + " not found.");
		}
	}

	@Subcommand("help")
	public void onHelp(Player player) {
		player.sendMessage(Gradient.hsvGradient("" +
						"ProjectKorraSpirits: Pride's Cut (Pride's ProjectKorraSpirits) is a ProjectKorra side plugin that adds a variety of new mechanics " +
						"around the concept of Spirits in the world of Avatar: The Last Airbender and The Legend of Korra. " +
						"Become these mystical beings in game! Choosing from: \n",
					Color.decode("#395487"),
					Color.decode("#58A395"),
					new LinearInterpolator()) +

				SpiritElement.SPIRIT.getColor() + "Spirit" + ChatColor.of("#58A395") + ", " +
				SpiritElement.LIGHT_SPIRIT.getColor() + "Light Spirit" + ChatColor.of("#58A395") + " and, " +
				SpiritElement.DARK_SPIRIT.getColor() + "Dark Spirit" + ChatColor.of("#58A395") + "!\n");

		VersionCommand.info(player);
	}

	@Subcommand("orbs change")
	public void onOrbsChange(Player player) {
		Orbs.changeForm(player);
	}
}

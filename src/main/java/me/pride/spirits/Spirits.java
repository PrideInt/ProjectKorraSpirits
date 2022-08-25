package me.pride.spirits;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.Config;
import me.pride.spirits.api.Spirit;
import me.pride.spirits.game.SpiritElement;
import me.pride.spirits.items.Spirecite;
import me.pride.spirits.items.Station;
import me.pride.spirits.util.Tools;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Spirits extends JavaPlugin {

    public static Spirits instance;

    private static final String AUTHOR = "Prride/prideyy", VERSION = "VERSION 1";

    private Config config;
    private Listener listener;

    @Override
    public void onEnable() {
        instance = this;
        listener = new SpiritsListener();
        config = new Config(new File("prides_spirits.yml"));

        config();
        if (getConfig().getBoolean("Spirecite.Enabled")) {
            Spirecite.setup();
            Station.setup();
        }
        CoreAbility.registerPluginAbilities(this, "me.pride.spirits.abilities");

        getLogger().info("Pride's Spirits: Definitive Version is now open for public use! Trial 1.0.0");
        getServer().getPluginManager().registerEvents(listener, this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new SpiritsManager(), 0, 1);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(listener);
        Spirit.cleanup();
    }
    public static String getAuthor() {
        return AUTHOR;
    }
    public static String getAuthor(Element element) {
        return Tools.getOppositeColor(element) + "" + ChatColor.UNDERLINE + AUTHOR;
    }
    public static String getVersion() {
        return VERSION;
    }
    public static String getVersion(Element element) {
        return Tools.getOppositeColor(element) + "" + ChatColor.UNDERLINE + VERSION;
    }
    public Config configuration() {
        return this.config;
    }
    public FileConfiguration getConfig() {
        return this.config.get();
    }
    
    public void config() {
    
    }
}

package me.pride.spirits;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.CoreAbility;
import me.pride.spirits.api.Spirit;
import me.pride.spirits.config.Config;
import me.pride.spirits.game.AncientSoulweaver;
import me.pride.spirits.game.Spirecite;
import me.pride.spirits.game.Station;
import me.pride.spirits.util.BendingBossBar;
import me.pride.spirits.util.ChatUtil;
import me.pride.spirits.util.Tools;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Spirits extends JavaPlugin {
    public static Spirits instance;

    private FileConfiguration config;
    private Listener listener;

    @Override
    public void onEnable() {
        instance = this;
        listener = new SpiritsListener();
        config = this.getConfig();

        Config.setup();
        if (getConfig().getBoolean("Spirecite.Enabled")) {
            Spirecite.setup();
            Station.setup();
        }
        CoreAbility.registerPluginAbilities(this, "me.pride.spirits.abilities");

        getLogger().info("Pride's Spirits: Definitive Version is now open for public use! Trial 1.7.1.9");
    
        BossBar bar = Bukkit.getBossBar(AncientSoulweaver.ANCIENT_SOULWEAVER_BAR_KEY);
        boolean exists = false;
        
        if (bar != null) {
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity.getPersistentDataContainer().has(AncientSoulweaver.ANCIENT_SOULWEAVER_KEY, PersistentDataType.BYTE)) {
                        Warden warden = (Warden) entity;
                        
                        new BendingBossBar(AncientSoulweaver.ANCIENT_SOULWEAVER_BAR_KEY, AncientSoulweaver.NAME, BarColor.BLUE, 1000.0).setProgress(warden.getHealth() / warden.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                        AncientSoulweaver.addExistingSoulweaver(warden);
                        exists = true;
                    }
                }
            }
        }
        if (!exists) {
            Bukkit.removeBossBar(AncientSoulweaver.ANCIENT_SOULWEAVER_BAR_KEY);
        }
        getServer().getPluginManager().registerEvents(listener, this);
        getServer().getPluginManager().registerEvents(((SpiritsListener) listener).mainListener(), this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new SpiritsManager(), 0, 1);
    }

    @Override
    public void onDisable() {
        // Bukkit.getServer().removeBossBar(AncientSoulweaver.ANCIENT_SOULWEAVER_BAR_KEY);
        HandlerList.unregisterAll(listener);
        Spirit.cleanup();
    }
    public static String getAuthor() {
        return ChatUtil.getAuthor();
    }
    public static String getAuthor(Element element) {
        return ChatUtil.getAuthor(element);
    }
    public static String getVersion() {
        return ChatUtil.getVersion();
    }
    
    public FileConfiguration configuration() {
        return this.config;
    }
}

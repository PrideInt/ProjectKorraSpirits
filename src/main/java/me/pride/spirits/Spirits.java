package me.pride.spirits;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.CoreAbility;
import me.pride.spirits.api.Spirit;
import me.pride.spirits.api.SpiritType;
import me.pride.spirits.api.record.SpiritRecord;
import me.pride.spirits.config.Config;
import me.pride.spirits.game.AncientSoulweaver;
import me.pride.spirits.game.Spirecite;
import me.pride.spirits.game.Station;
import me.pride.spirits.storage.SQLite;
import me.pride.spirits.storage.StorageCache;
import me.pride.spirits.util.BendingBossBar;
import me.pride.spirits.util.ChatUtil;
import me.pride.spirits.util.Tools;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Spirits extends JavaPlugin {
    public static Spirits instance;

    private FileConfiguration config;
    private Listener listener;
    private SQLite database;

    @Override
    public void onEnable() {
        instance = this;
        listener = new SpiritsListener();
        config = this.getConfig();
        database = new SQLite();
        database.init();

        Config.setup();
        if (getConfig().getBoolean("Spirecite.Enabled")) {
            Spirecite.setup();
            Station.setup();
        }
        CoreAbility.registerPluginAbilities(this, "me.pride.spirits.abilities");

        getLogger().info("Pride's Spirits: Definitive Version is now open for public use! Trial 1.8.0.0");
    
        StorageCache.queryUUIDs(database);
        StorageCache.queryLocations();
        
        for (Map.Entry<String, List<int[]>> entry : StorageCache.locations().entrySet()) {
            World world = Bukkit.getWorld(entry.getKey());
            
            for (int[] coords : entry.getValue()) {
                int x = coords[0], y = coords[1], z = coords[2];
                
                new Location(world, x, y, z).getBlock().setMetadata("station:ancient", new FixedMetadataValue(Spirits.instance, 0));
            }
        }
        
        BossBar bar = Bukkit.getBossBar(AncientSoulweaver.ANCIENT_SOULWEAVER_BAR_KEY);
        BendingBossBar bendingBossBar = null;
        boolean exists = false;
    
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (Spirit.exists(entity)) {
                    SpiritType spiritType = SpiritType.SPIRIT;
                    if (entity.getPersistentDataContainer().has(Spirit.LIGHT_SPIRIT_KEY, PersistentDataType.STRING)) {
                        spiritType = SpiritType.LIGHT;
                    } else if (entity.getPersistentDataContainer().has(Spirit.DARK_SPIRIT_KEY, PersistentDataType.STRING)) {
                        spiritType = SpiritType.DARK;
                    }
                    SpiritType finalSpiritType = spiritType;
                    new Spirit(world, entity) {
                        @Override
                        public SpiritRecord record() {
                            return new SpiritRecord(entity.getCustomName(), entity.getType(), finalSpiritType, -1);
                        }
                    };
                }
                if (bar != null) {
                    if (entity.getPersistentDataContainer().has(AncientSoulweaver.ANCIENT_SOULWEAVER_KEY, PersistentDataType.BYTE)) {
                        Warden warden = (Warden) entity;
                    
                        bendingBossBar = new BendingBossBar(AncientSoulweaver.ANCIENT_SOULWEAVER_BAR_KEY, AncientSoulweaver.NAME, BarColor.BLUE, 1000.0).setProgress(warden.getHealth() / warden.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                        AncientSoulweaver.addExistingSoulweaver(warden);
                        exists = true;
                    }
                }
            }
        }
        if (!exists) {
            for (UUID uuid : StorageCache.uuidCache()) {
                bendingBossBar.bossBar().addPlayer(Bukkit.getPlayer(uuid));
            }
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
        StorageCache.updateLocations();
        StorageCache.updateUUIDs(database);
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

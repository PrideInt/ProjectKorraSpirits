package me.pride.spirits;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.CoreAbility;
import me.pride.spirits.api.Spirit;
import me.pride.spirits.api.SpiritType;
import me.pride.spirits.api.builder.SpiritBuilder;
import me.pride.spirits.commands.SpiritsCommand;
import me.pride.spirits.config.Config;
import me.pride.spirits.game.AncientSoulweaver;
import me.pride.spirits.game.Atrium;
import me.pride.spirits.game.Spirecite;
import me.pride.spirits.game.Station;
import me.pride.spirits.storage.SQLite;
import me.pride.spirits.storage.StorageCache;
import me.pride.spirits.util.BendingBossBar;
import me.pride.spirits.util.ChatUtil;
import me.pride.spirits.util.GhostFactory;
import me.pride.spirits.util.Keys;
import me.pride.spirits.world.SpiritWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Warden;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Spirits extends JavaPlugin {
    public static Spirits instance;

    private FileConfiguration config;
    private Listener listener;
    private SQLite database;
    private GhostFactory ghostFactory;

    @Override
    public void onEnable() {
        instance = this;
        listener = new SpiritsListener();
        config = this.getConfig();
        database = new SQLite();
        database.init();

        Config.setup();

        if (getConfig().getBoolean("Spirit.Ghosts")) {
            ghostFactory = new GhostFactory();
        }
        if (getConfig().getBoolean("Spirecite.Enabled")) {
            Spirecite.setup();
            Station.setup();
            Atrium.setup();
        }
        CoreAbility.registerPluginAbilities(this, "me.pride.spirits.abilities");

        getLogger().info("Pride's Spirits: Definitive Version is now open for public use! Trial 1.8.0.0");
    
        StorageCache.queryUUIDs(database);
        StorageCache.querySpirits(database);
        StorageCache.queryLocations();
        StorageCache.queryWorlds();
        if (getConfig().getBoolean("Light.CanStackTotems")) {
            StorageCache.queryTotemStacks(database);
        }
        
        for (Map.Entry<String, List<int[]>> entry : StorageCache.locations().entrySet()) {
            World world = Bukkit.getWorld(entry.getKey());
            
            for (int[] coords : entry.getValue()) {
                int x = coords[0], y = coords[1], z = coords[2];
                
                new Location(world, x, y, z).getBlock().setMetadata("station:ancient", new FixedMetadataValue(Spirits.instance, 0));
            }
        }

        for (String world : StorageCache.spiritWorlds()) {
            SpiritWorld.create(Bukkit.getWorld(world));
        }
        
        BossBar bar = Bukkit.getBossBar(AncientSoulweaver.ANCIENT_SOULWEAVER_BAR_KEY);
        BendingBossBar bendingBossBar = null;
        boolean exists = false;
    
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (Spirit.exists(entity)) {
                    SpiritType spiritType = SpiritType.SPIRIT;
                    if (entity.getPersistentDataContainer().has(Keys.LIGHT_SPIRIT_KEY, PersistentDataType.STRING)) {
                        spiritType = SpiritType.LIGHT;
                    } else if (entity.getPersistentDataContainer().has(Keys.DARK_SPIRIT_KEY, PersistentDataType.STRING)) {
                        spiritType = SpiritType.DARK;
                    }
                    SpiritBuilder
                            .builder(spiritType)
                            .spiritName(entity.getCustomName())
                            .entityType(entity.getType())
                            .revertTime(-1)
                            .build();
                }
                if (bar != null) {
                    if (entity.getPersistentDataContainer().has(AncientSoulweaver.ANCIENT_SOULWEAVER_KEY, PersistentDataType.BYTE)) {
                        Warden warden = (Warden) entity;
                    
                        bendingBossBar = new BendingBossBar(AncientSoulweaver.ANCIENT_SOULWEAVER_BAR_KEY, AncientSoulweaver.NAME, BarColor.BLUE, 1000.0).setProgress(warden.getHealth() / warden.getAttribute(Attribute.MAX_HEALTH).getValue());
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

        SpiritsCommand.commandManager.registerCommand(new SpiritsCommand());
        SpiritsCommand.commandManager.getCommandCompletions().registerCompletion("worlds", (worlds) -> Bukkit.getWorlds().stream().map(World::getName).toList());
        SpiritsCommand.commandManager.getCommandCompletions().registerCompletion("spiritWorlds", (spiritWorld) -> StorageCache.spiritWorlds());
    }

    @Override
    public void onDisable() {
        // Bukkit.getServer().removeBossBar(AncientSoulweaver.ANCIENT_SOULWEAVER_BAR_KEY);
        HandlerList.unregisterAll(listener);
        Spirit.cleanup();
        StorageCache.updateLocations();
        StorageCache.updateWorlds();
        StorageCache.updateUUIDs(database);
        StorageCache.updateSpirits(database);
        if (getConfig().getBoolean("Light.CanStackTotems")) {
            StorageCache.updateTotemStacks(database);
        }
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
    public GhostFactory getGhostFactory() {
        return this.ghostFactory;
    }
}

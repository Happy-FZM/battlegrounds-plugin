package com.matsg.battlegrounds;

import com.matsg.battlegrounds.api.*;
import com.matsg.battlegrounds.api.config.BattlegroundsConfig;
import com.matsg.battlegrounds.api.config.CacheYaml;
import com.matsg.battlegrounds.api.config.WeaponConfig;
import com.matsg.battlegrounds.api.dao.PlayerDAOFactory;
import com.matsg.battlegrounds.api.game.*;
import com.matsg.battlegrounds.api.item.Explosive;
import com.matsg.battlegrounds.api.item.FireArm;
import com.matsg.battlegrounds.api.item.Knife;
import com.matsg.battlegrounds.config.BattleCacheYaml;
import com.matsg.battlegrounds.di.DIContainer;
import com.matsg.battlegrounds.di.DIFactory;
import com.matsg.battlegrounds.game.*;
import com.matsg.battlegrounds.listener.BattleEventManager;
import com.matsg.battlegrounds.listener.EventListener;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BattlegroundsPlugin extends JavaPlugin implements Battlegrounds {

    private static BattlegroundsPlugin plugin;
    private BattlegroundsConfig config;
    private CacheYaml cache;
    private EventManager eventManager;
    //private ExplosiveConfig explosiveConfig;
    //private FireArmConfig fireArmConfig;
    private GameManager gameManager;
    //private KnifeConfig knifeConfig;
    private List<BattlegroundsExtension> extensions;
    //private PlayerData playerData;
    //private SQLConfig sqlConfig;
    private Translator translator;

    public void onEnable() {
        plugin = this;

        try {
            startPlugin();
        } catch (StartupFailedException e) {
            getLogger().severe("An error occurred while enabling Battlegrounds v" + getDescription().getVersion());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Succesfully started Battlegrounds (" + extensions.size() + " extensions loaded)");
    }

    public void onDisable() {
        gameManager.shutdown();
    }

    public static BattlegroundsPlugin getPlugin() {
        return plugin;
    }

    public static WorldEditPlugin getWorldEditPlugin() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
            return null;
        }
        return (WorldEditPlugin) plugin;
    }

    public CacheYaml getBattlegroundsCache() {
        return cache;
    }

    public BattlegroundsConfig getBattlegroundsConfig() {
        return config;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public WeaponConfig<Explosive> getExplosiveConfig() {
        return null;
    }

    public WeaponConfig<FireArm> getFireArmConfig() {
        return null;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public WeaponConfig<Knife> getKnifeConfig() {
        return null;
    }

    public PlayerDAOFactory getPlayerStorage() {
        return null;
    }

    public Translator getTranslator() {
        return translator;
    }

    public boolean loadConfigs() {
        try {
            cache = new BattleCacheYaml(this, "cache.yml");
            config = new BattlegroundsConfig(this);
            //explosiveConfig = new ExplosiveConfig(this);
            //fireArmConfig = new FireArmConfig(this);
            //knifeConfig = new KnifeConfig(this);
            //playerData = new PlayerData(this);
            //sqlConfig = new SQLConfig(this);
            translator = new PluginTranslator(this);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private List<BattlegroundsExtension> loadExtensions() {
        List<BattlegroundsExtension> list = new ArrayList<>();
        for (Plugin plugin : getServer().getPluginManager().getPlugins()) {
            if (plugin instanceof BattlegroundsExtension) {
                list.add((BattlegroundsExtension) plugin);
            }
        }
        return list;
    }

    private void registerDIContainers() {
        DIFactory.registerContainer(Arena.class, new DIContainer<>(BattleArena.class));
        DIFactory.registerContainer(Game.class, new DIContainer<>(BattleGame.class));
        DIFactory.registerContainer(GameConfiguration.class, new DIContainer<>(BattleGameConfiguration.class));
        DIFactory.registerContainer(GamePlayer.class, new DIContainer<>(BattleGamePlayer.class));
        DIFactory.registerContainer(SavedInventory.class, new DIContainer<>(BattleSavedInventory.class));
    }

    private void startPlugin() throws StartupFailedException {
        registerDIContainers();

        extensions = loadExtensions();

        for (BattlegroundsExtension extension : extensions) {
            extension.onInit();
        }

        try {
            cache = new BattleCacheYaml(this, "cache.yml");
            config = new BattlegroundsConfig(this);
            //playerData = new PlayerData(this);
            //sqlConfig = new SQLConfig(this);
        } catch (Exception e) {
            throw new StartupFailedException("Failed to load configuration files!", e);
        }

        try {
            translator = new PluginTranslator(this);
        } catch (Exception e) {
            throw new StartupFailedException("Failed to initialize the translator!", e);
        }

        try {
            //explosiveConfig = new ExplosiveConfig(this);
            //fireArmConfig = new FireArmConfig(this);
            //knifeConfig = new KnifeConfig(this);

            //getLogger().info("Succesfully loaded " + fireArmConfig.getWeaponList().size() + " guns, " +
            //        explosiveConfig.getWeaponList().size() + " explosives and "
            //        + knifeConfig.getWeaponList().size() + " knives from the config");
        } catch (Exception e) {
            throw new StartupFailedException("Failed to load weapon configuration files!", e);
        }

        //this.channelMessenger = new PluginChannelMessenger(this);
        this.eventManager = new BattleEventManager();
        this.gameManager = new BattleGameManager();

        //new DataLoader(this);

        //new WeaponsCommand(this);
        //new ZombiesCommand(this);

        new EventListener(this);

        //DAOFactory.init(this, sqlConfig);
        //WeaponsView.getInstance(this);

        for (BattlegroundsExtension extension : extensions) {
            extension.onPostInit();
        }
    }
}
package com.matsg.battlegrounds.config;

import com.matsg.battlegrounds.api.Battlegrounds;
import com.matsg.battlegrounds.api.config.CacheYaml;
import com.matsg.battlegrounds.api.game.*;
import com.matsg.battlegrounds.api.gamemode.GameMode;
import com.matsg.battlegrounds.game.*;
import com.matsg.battlegrounds.gamemode.GameModeType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class DataLoader {

    private final Battlegrounds plugin;
    private final Logger logger;

    public DataLoader(Battlegrounds plugin) {
        this.logger = plugin.getLogger();
        this.plugin = plugin;

        plugin.getGameManager().getGames().clear();

        load();
    }

    private void load() {
        logger.info("Loading in games and arenas...");

        // Look for games files that have been created already
        try {
            File[] files = new File(plugin.getDataFolder().getPath() + "/data").listFiles();

            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.isDirectory() && file.getName().startsWith("game_")) {
                        int id = Integer.parseInt(file.getName().substring(5, file.getName().length()));

                        plugin.getGameManager().getGames().add(new BattleGame(plugin, id));
                    }
                }
            } else {
                logger.info("No games have been found!");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Setting configurations
        try {
            for (Game game : plugin.getGameManager().getGames()) {
                ConfigurationSection config = game.getDataFile().getConfigurationSection("_config");
                List<GameMode> gameModes = new ArrayList<>();

                for (String gameModeType : config.getStringList("gamemodes")) {
                    GameMode gameMode;
                    try {
                        gameMode = GameModeType.valueOf(gameModeType.toUpperCase()).getInstance(game);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().severe("Invalid gamemode type \"" + gameModeType + "\"");
                        continue;
                    }
                    gameModes.add(gameMode);
                }

                GameConfiguration configuration = new BattleGameConfiguration(
                        gameModes.toArray(new GameMode[gameModes.size()]),
                        config.getInt("maxplayers"),
                        config.getInt("minplayers"),
                        config.getInt("gamecountdown"),
                        config.getInt("lobbycountdown")
                );

                game.setConfiguration(configuration);
                game.setGameMode(game.getConfiguration().getGameModes()[new Random().nextInt(game.getConfiguration().getGameModes().length)]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Add existing arenas to the game
        try {
            for (Game game : plugin.getGameManager().getGames()) {
                int id = game.getId();
                CacheYaml data = game.getDataFile();
                ConfigurationSection arenaSection = data.getConfigurationSection("arena");

                if (arenaSection == null) {
                    logger.warning("No arenas were found for game " + id);
                    continue;
                }

                for (String name : arenaSection.getKeys(false)) {
                    logger.info("Adding arena " + name + " to game " + id);

                    Location max = data.getLocation("arena." + name + ".max"), min = data.getLocation("arena." + name + ".min");
                    World world = plugin.getServer().getWorld(data.getString("arena." + name + ".world"));

                    Arena arena = new BattleArena(name, max, min, world);
                    ConfigurationSection spawnSection = arenaSection.getConfigurationSection(name + ".spawn");

                    if (spawnSection != null) {
                        for (String spawnIndex : spawnSection.getKeys(false)) {
                            Spawn spawn = new ArenaSpawn(Integer.parseInt(spawnIndex), data.getLocation("arena." + name + ".spawn." + spawnIndex + ".location"), spawnSection.getInt(spawnIndex + ".team"));
                            spawn.setTeamBase(spawnSection.getBoolean(spawnIndex + ".base"));
                            if (spawn.getLocation() != null) {
                                arena.getSpawns().add(spawn);
                            }
                        }
                    }

                    game.getArenaList().add(arena);

                    logger.info("Succesfully added arena " + name + " with " + arena.getSpawns().size() + " spawns to game " + id);
                }

                // Assign an arena to this game
                game.setArena(game.getArenaList().get(new Random().nextInt(game.getArenaList().size())));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set the game joining signs
        try {
            for (Game game : plugin.getGameManager().getGames()) {
                int id = game.getId();
                Location location = game.getDataFile().getLocation("sign");

                if (location == null) {
                    logger.warning("No join sign was found for game " + id);
                    continue;
                }

                BlockState state = location.getBlock().getState();

                if (!(state instanceof Sign)) {
                    logger.warning("The sign of game " + id + " was corrupted!");
                    continue;
                }

                GameSign sign = new BattleGameSign(plugin, game, (Sign) state);

                game.setGameSign(sign);
                sign.update();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.info("Loaded " + plugin.getGameManager().getGames().size() + " game(s) from the cache");
    }
}
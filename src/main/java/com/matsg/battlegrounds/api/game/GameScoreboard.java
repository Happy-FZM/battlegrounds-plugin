package com.matsg.battlegrounds.api.game;

import com.matsg.battlegrounds.api.player.GamePlayer;
import org.bukkit.World;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Set;

public interface GameScoreboard {

    Scoreboard createScoreboard();

    void display(Game game);

    void display(GamePlayer gamePlayer);

    String getScoreboardId();

    Set<World> getWorlds();
}
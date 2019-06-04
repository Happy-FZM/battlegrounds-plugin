package com.matsg.battlegrounds.game.mode.ffa;

import com.matsg.battlegrounds.TranslationKey;
import com.matsg.battlegrounds.api.Battlegrounds;
import com.matsg.battlegrounds.api.Translator;
import com.matsg.battlegrounds.api.event.GameEndEvent;
import com.matsg.battlegrounds.api.event.GamePlayerDeathEvent.DeathCause;
import com.matsg.battlegrounds.api.game.Game;
import com.matsg.battlegrounds.api.game.GameScoreboard;
import com.matsg.battlegrounds.api.game.Spawn;
import com.matsg.battlegrounds.api.game.Team;
import com.matsg.battlegrounds.api.game.Objective;
import com.matsg.battlegrounds.api.item.Weapon;
import com.matsg.battlegrounds.api.entity.GamePlayer;
import com.matsg.battlegrounds.api.entity.Hitbox;
import com.matsg.battlegrounds.api.Placeholder;
import com.matsg.battlegrounds.game.BattleTeam;
import com.matsg.battlegrounds.game.mode.ArenaGameMode;
import com.matsg.battlegrounds.game.mode.GameModeCountdown;
import com.matsg.battlegrounds.game.mode.GameModeType;
import com.matsg.battlegrounds.game.mode.Result;
import com.matsg.battlegrounds.game.mode.shared.SpawningBehavior;
import com.matsg.battlegrounds.util.EnumTitle;
import org.bukkit.ChatColor;

import java.util.List;

public class FreeForAll extends ArenaGameMode {

    private FFAConfig config;

    public FreeForAll(Battlegrounds plugin, Game game, Translator translator, SpawningBehavior spawningBehavior, FFAConfig config) {
        super(plugin, game, translator, spawningBehavior);
        this.config = config;
        this.name = translator.translate(TranslationKey.FFA_NAME);
        this.shortName = translator.translate(TranslationKey.FFA_SHORT);
    }

    public FFAConfig getConfig() {
        return config;
    }

    public GameModeType getType() {
        return GameModeType.FREE_FOR_ALL;
    }

    public void addPlayer(GamePlayer gamePlayer) {
        if (getTeam(gamePlayer) != null) {
            return;
        }
        Team team = new BattleTeam(0, gamePlayer.getName(), config.getArmorColor(), ChatColor.WHITE);
        team.addPlayer(gamePlayer);
        teams.add(team);
    }

    public Spawn getRespawnPoint(GamePlayer gamePlayer) {
        GamePlayer nearestPlayer = game.getPlayerManager().getNearestPlayer(gamePlayer.getLocation());
        return game.getArena().getRandomSpawn(nearestPlayer.getLocation(), config.getMinimumSpawnDistance());
    }

    public GameScoreboard getScoreboard() {
        GameScoreboard scoreboard = new FFAScoreboard(game, config);
        scoreboard.getWorlds().addAll(config.getScoreboardWorlds());
        scoreboard.setLayout(config.getScoreboardLayout());

        return config.isScoreboardEnabled() ? scoreboard : null;
    }

    public void onDeath(GamePlayer gamePlayer, DeathCause deathCause) {
        game.getPlayerManager().broadcastMessage(translator.translate(deathCause.getMessagePath(),
                new Placeholder("bg_player", gamePlayer.getName())
        ));
        handleDeath(gamePlayer);
    }

    public void onKill(GamePlayer gamePlayer, GamePlayer killer, Weapon weapon, Hitbox hitbox) {
        game.getPlayerManager().broadcastMessage(translator.translate(getKillMessageKey(hitbox),
                new Placeholder("bg_killer", killer.getName()),
                new Placeholder("bg_player", gamePlayer.getName()),
                new Placeholder("bg_weapon", weapon.getName())
        ));

        handleDeath(gamePlayer);
        killer.addExp(100);
        killer.setKills(killer.getKills() + 1);
        killer.getTeam().setScore(killer.getTeam().getScore() + 1);
        game.getPlayerManager().updateExpBar(killer);

        Objective objective = getAchievedObjective();

        if (objective != null) {
            game.callEvent(new GameEndEvent(game, objective, getTopTeam(), getSortedTeams()));
            game.stop();
        }
    }

    public void removePlayer(GamePlayer gamePlayer) {
        Team team = getTeam(gamePlayer);
        if (team == null) {
            return;
        }
        teams.remove(team);
    }

    public void start() {
        super.start();
        for (GamePlayer gamePlayer : game.getPlayerManager().getPlayers()) {
            gamePlayer.setLives(config.getLives());
            EnumTitle.FFA_START.send(gamePlayer.getPlayer());
        }
    }

    public void startCountdown() {
        GameModeCountdown countdown = new GameModeCountdown(game, translator, config.getCountdownLength());
        countdown.runTaskTimer(0, 20);

        game.setCountdown(countdown);
    }

    public void stop() {
        List<Team> teams = getSortedTeams();
        Objective objective = getAchievedObjective();
        Placeholder[] placeholders = new Placeholder[] {
                new Placeholder("bg_first", teams.size() > 0 && teams.get(0) != null ? teams.get(0).getPlayers()[0].getName() : "---"),
                new Placeholder("bg_first_score", teams.size() > 0 && teams.get(0) != null ? teams.get(0).getPlayers()[0].getKills() : 0),
                new Placeholder("bg_second", teams.size() > 1 && teams.get(1) != null ? teams.get(1).getPlayers()[0].getName() : "---"),
                new Placeholder("bg_second_score", teams.size() > 1 && teams.get(1) != null ? teams.get(1).getPlayers()[0].getKills() : 0),
                new Placeholder("bg_third", teams.size() > 2 && teams.get(2) != null ? teams.get(2).getPlayers()[0].getName() : "---"),
                new Placeholder("bg_third_score", teams.size() > 2 && teams.get(2) != null ? teams.get(2).getPlayers()[0].getKills() : 0)
        };

        for (String message : config.getEndMessage()) {
            game.getPlayerManager().broadcastMessage(translator.translate(TranslationKey.PREFIX) + translator.translate(message, placeholders));
        }

        for (Team team : teams) {
            Result result = Result.getResult(team, getSortedTeams());
            if (result != null) {
                for (GamePlayer gamePlayer : team.getPlayers()) {
                    objective.getTitle().send(gamePlayer.getPlayer(), new Placeholder("bg_result", translator.translate(result.getTranslationKey())));
                }
            }
        }

        this.teams.clear();
    }
}

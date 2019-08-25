package com.matsg.battlegrounds.game;

import com.matsg.battlegrounds.TranslationKey;
import com.matsg.battlegrounds.api.Translator;
import com.matsg.battlegrounds.api.game.Countdown;
import com.matsg.battlegrounds.api.game.Game;
import com.matsg.battlegrounds.api.Placeholder;
import com.matsg.battlegrounds.gui.scoreboard.LobbyScoreboard;
import com.matsg.battlegrounds.util.BattleRunnable;
import com.matsg.battlegrounds.util.BattleSound;

import java.util.ArrayList;
import java.util.List;

public class LobbyCountdown extends BattleRunnable implements Countdown {

    private boolean cancelled;
    private Game game;
    private int id, time;
    private List<Integer> display;
    private LobbyScoreboard scoreboard;
    private Translator translator;

    public LobbyCountdown(Game game, Translator translator, int time, int... display) {
        this.game = game;
        this.translator = translator;
        this.time = time;
        this.cancelled = false;
        this.display = new ArrayList<>();
        this.scoreboard = new LobbyScoreboard(game);

        for (int i : display) {
            this.display.add(i);
        }
    }

    public void cancel() {
        plugin.getServer().getScheduler().cancelTask(id);
    }

    public void cancelCountdown() {
        cancelled = true;
        game.getPlayerManager().broadcastMessage(translator.translate(TranslationKey.COUNTDOWN_CANCELLED));
    }

    public void run() {
        id = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            int countdown = time;

            public void run() {
                if (cancelled) {
                    cancel();
                    return;
                }

                if (game.getArena() == null || game.getPlayerManager().getPlayers().size() < game.getConfiguration().getMinPlayers()) {
                    game.stop();
                    return;
                }

                if (countdown <= 0) {
                    game.startCountdown();
                    cancel();
                    return;
                }

                if (display.contains(countdown)) {
                    game.getPlayerManager().broadcastMessage(translator.translate(TranslationKey.COUNTDOWN_NOTE, new Placeholder("bg_countdown", countdown)));
                    BattleSound.COUNTDOWN_NOTE.play(game);
                }

                scoreboard.setCountdown(countdown);
                scoreboard.display(game);
                countdown--;
            }
        }, 0, 20);
    }
}

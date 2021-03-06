package com.matsg.battlegrounds.command;

import com.matsg.battlegrounds.TranslationKey;
import com.matsg.battlegrounds.api.Battlegrounds;
import com.matsg.battlegrounds.api.game.Game;
import com.matsg.battlegrounds.api.util.Placeholder;
import com.matsg.battlegrounds.util.BattleRunnable;
import com.matsg.battlegrounds.util.Message;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class RemoveGame extends SubCommand {

    private List<CommandSender> senders;

    public RemoveGame(Battlegrounds plugin) {
        super(plugin, "removegame", Message.create(TranslationKey.DESCRIPTION_REMOVEGAME),
                "bg removegame [id]", "battlegrounds.removegame", false, "rg");
        this.senders = new ArrayList<>();
    }

    public void execute(final CommandSender sender, String[] args) {
        if (args.length == 1) {
            sender.sendMessage(Message.create(TranslationKey.SPECIFY_ID));
            return;
        }

        int id;

        try {
            id = Integer.parseInt(args[1]);
        } catch (Exception e) {
            sender.sendMessage(Message.create(TranslationKey.INVALID_ARGUMENT_TYPE, new Placeholder("bg_arg", args[1])));
            return;
        }

        if (!plugin.getGameManager().exists(id)) {
            sender.sendMessage(Message.create(TranslationKey.GAME_NOT_EXISTS, new Placeholder("bg_game", id)));
            return;
        }

        if (!senders.contains(sender)) {
            sender.sendMessage(Message.create(TranslationKey.GAME_CONFIRM_REMOVE, new Placeholder("bg_game", id)));

            senders.add(sender);

            new BattleRunnable() {
                public void run() {
                    senders.remove(sender);
                }
            }.runTaskLater(200);
            return;
        }

        Game game = plugin.getGameManager().getGame(id);
        game.getDataFile().removeFile();

        plugin.getGameManager().getGames().remove(game);

        sender.sendMessage(Message.create(TranslationKey.GAME_REMOVE, new Placeholder("bg_game", id)));

        senders.remove(sender);
    }
}

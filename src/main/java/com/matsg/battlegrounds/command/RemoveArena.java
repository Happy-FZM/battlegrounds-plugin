package com.matsg.battlegrounds.command;

import com.matsg.battlegrounds.TranslationKey;
import com.matsg.battlegrounds.api.Battlegrounds;
import com.matsg.battlegrounds.api.game.Arena;
import com.matsg.battlegrounds.api.game.Game;
import com.matsg.battlegrounds.api.util.Placeholder;
import com.matsg.battlegrounds.util.BattleRunnable;
import com.matsg.battlegrounds.util.Message;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class RemoveArena extends SubCommand {

    private List<CommandSender> senders;

    public RemoveArena(Battlegrounds plugin) {
        super(plugin,"removearena", Message.create(TranslationKey.DESCRIPTION_REMOVEARENA),
                "bg removearena [id] [arena]", "battlegrounds.removearena", false, "ra");
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

        Game game = plugin.getGameManager().getGame(id);

        if (args.length == 2) {
            sender.sendMessage(Message.create(TranslationKey.SPECIFY_NAME));
            return;
        }

        String name = args[2].replaceAll("_", " ");
        Arena arena = plugin.getGameManager().getArena(game, name);

        if (arena == null) {
            sender.sendMessage(Message.create(TranslationKey.ARENA_NOT_EXISTS, new Placeholder("bg_game", id), new Placeholder("bg_arena", name)));
            return;
        }

        if (!senders.contains(sender)) {
            sender.sendMessage(Message.create(TranslationKey.ARENA_CONFIRM_REMOVE, new Placeholder("bg_arena", name)));

            senders.add(sender);

            new BattleRunnable() {
                public void run() {
                    senders.remove(sender);
                }
            }.runTaskLater(200);
            return;
        }

        game.getArenaList().remove(arena);
        game.getDataFile().set("arena." + name, null);
        game.getDataFile().save();

        sender.sendMessage(Message.create(TranslationKey.ARENA_REMOVE, new Placeholder("bg_arena", name), new Placeholder("bg_game", id)));
    }
}
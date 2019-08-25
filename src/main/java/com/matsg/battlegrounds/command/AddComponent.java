package com.matsg.battlegrounds.command;

import com.matsg.battlegrounds.TranslationKey;
import com.matsg.battlegrounds.api.Battlegrounds;
import com.matsg.battlegrounds.api.Translator;
import com.matsg.battlegrounds.api.game.Arena;
import com.matsg.battlegrounds.api.game.Game;
import com.matsg.battlegrounds.api.game.GameMode;
import com.matsg.battlegrounds.api.Placeholder;
import com.matsg.battlegrounds.command.component.*;
import com.matsg.battlegrounds.command.validator.ArenaNameValidator;
import com.matsg.battlegrounds.command.validator.GameIdValidator;
import com.matsg.battlegrounds.command.validator.ValidationResponse;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class AddComponent extends Command {

    private Map<String, ComponentCommand> commands;

    public AddComponent(Battlegrounds plugin) {
        super(plugin);
        setAliases("ac", "add");
        setDescription(createMessage(TranslationKey.DESCRIPTION_ADDCOMPONENT));
        setName("addcomponent");
        setPermissionNode("bg.addcomponent");
        setPlayerOnly(true);
        setUsage("bg addcomponent [id] [arena] [section] [component]");

        Translator translator = plugin.getTranslator();

        commands = new HashMap<>();
        commands.put("door", new AddDoor(plugin, translator));
        commands.put("mobspawn", new AddMobSpawn(plugin, translator));
        commands.put("mysterybox", new AddMysteryBox(plugin, translator));
        commands.put("perk", new AddPerkMachine(plugin, translator));
        commands.put("section", new AddSection(plugin, translator));
        commands.put("spawn", new AddSpawn(plugin, translator));
        commands.put("wallweapon", new AddWallWeapon(plugin, translator));

        registerValidator(new GameIdValidator(plugin, translator, true));
        registerValidator(new ArenaNameValidator(plugin, translator, true));
    }

    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Game game = plugin.getGameManager().getGame(Integer.parseInt(args[1]));
        Arena arena = plugin.getGameManager().getArena(game, args[2].replaceAll("_", " "));

        if (args.length == 3) {
            player.sendMessage(createMessage(TranslationKey.SPECIFY_COMPONENT_TYPE));
            return;
        }

        ComponentCommand command = commands.get(args[3]);

        if (command == null) {
            player.sendMessage(createMessage(TranslationKey.INVALID_COMPONENT_TYPE, new Placeholder("bg_component", args[3])));
            return;
        }

        ComponentContext context = new ComponentContext();
        context.setArena(arena);
        context.setGame(game);
        context.setPlayer(player);

        // Validate the input for the component command
        ValidationResponse response = command.validateInput(args);

        if (!response.passed()) {
            player.sendMessage(response.getMessage());
            return;
        }

        int componentId = game.findAvailableComponentId();

        // Order of input arguments:
        // 0: Addcomponent
        // 1: Game id
        // 2: Arena name
        // 3: Component type
        // 4: Section name if required
        // >4: Anything else
        command.execute(context, componentId, args);
    }
}

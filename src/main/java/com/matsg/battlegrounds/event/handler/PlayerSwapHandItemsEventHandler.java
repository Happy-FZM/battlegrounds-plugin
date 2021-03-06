package com.matsg.battlegrounds.event.handler;

import com.matsg.battlegrounds.api.Battlegrounds;
import com.matsg.battlegrounds.api.event.handler.EventHandler;
import com.matsg.battlegrounds.api.game.Game;
import com.matsg.battlegrounds.api.item.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class PlayerSwapHandItemsEventHandler implements EventHandler<PlayerSwapHandItemsEvent> {

    private Battlegrounds plugin;

    public PlayerSwapHandItemsEventHandler(Battlegrounds plugin) {
        this.plugin = plugin;
    }

    public void handle(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getGame(player);

        if (game == null) {
            return;
        }

        event.setCancelled(true);

        Item item = game.getItemRegistry().getItem(event.getOffHandItem());

        if (item == null) {
            return;
        }

        item.onSwap(game.getPlayerManager().getGamePlayer(player));
    }
}

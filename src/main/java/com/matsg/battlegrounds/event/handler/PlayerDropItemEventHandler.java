package com.matsg.battlegrounds.event.handler;

import com.matsg.battlegrounds.api.Battlegrounds;
import com.matsg.battlegrounds.api.event.handler.EventHandler;
import com.matsg.battlegrounds.api.game.Game;
import com.matsg.battlegrounds.api.item.Droppable;
import com.matsg.battlegrounds.api.item.Item;
import com.matsg.battlegrounds.api.player.GamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerDropItemEventHandler implements EventHandler<PlayerDropItemEvent> {

    private Battlegrounds plugin;

    public PlayerDropItemEventHandler(Battlegrounds plugin) {
        this.plugin = plugin;
    }

    public void handle(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getGame(player);

        if (game == null) {
            return;
        }

        GamePlayer gamePlayer = game.getPlayerManager().getGamePlayer(player);
        ItemStack itemStack = event.getItemDrop().getItemStack();
        Item item = game.getItemRegistry().getWeaponIgnoreMetadata(gamePlayer, itemStack);

        if (item == null && (item = game.getItemRegistry().getItemIgnoreMetadata(itemStack)) == null || !(item instanceof Droppable) || !game.getState().isAllowItems()) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(((Droppable) item).onDrop(gamePlayer, event.getItemDrop()));
    }
}

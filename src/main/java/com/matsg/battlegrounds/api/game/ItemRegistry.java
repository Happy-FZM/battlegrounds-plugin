package com.matsg.battlegrounds.api.game;

import com.matsg.battlegrounds.api.item.Item;
import com.matsg.battlegrounds.api.item.Weapon;
import com.matsg.battlegrounds.api.entity.GamePlayer;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

public interface ItemRegistry {

    void addItem(Item item);

    void clear();

    Item getItem(ItemStack itemStack);

    Item getItemIgnoreMetadata(ItemStack itemStack);

    Iterable<Item> getItems();

    Weapon getWeapon(GamePlayer gamePlayer, ItemStack itemStack);

    Weapon getWeaponIgnoreMetadata(GamePlayer gamePlayer, ItemStack itemStack);

    void interact(GamePlayer gamePlayer, Item item, Action action);

    void removeItem(Item item);
}

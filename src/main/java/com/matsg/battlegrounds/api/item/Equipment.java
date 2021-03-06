package com.matsg.battlegrounds.api.item;

import com.matsg.battlegrounds.api.util.Sound;
import org.bukkit.entity.Item;

public interface Equipment extends Weapon, DamageSource, Droppable {

    Equipment clone();

    void deployEquipment();

    void deployEquipment(double velocity);

    int getAmount();

    Item getFirstDroppedItem();

    Sound[] getIgnitionSound();

    int getIgnitionTime();

    int getMaxAmount();

    double getVelocity();

    void ignite(Item item);

    boolean isBeingThrown();

    void setAmount(int amount);

    void setVelocity(double velocity);
}
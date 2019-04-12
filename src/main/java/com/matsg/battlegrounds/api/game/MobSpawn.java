package com.matsg.battlegrounds.api.game;

import com.matsg.battlegrounds.api.entity.MobType;
import org.bukkit.Location;

public interface MobSpawn extends ArenaComponent, Lockable {

    /**
     * Gets the barricade of the monster spawn. Can be null.
     *
     * @return The barricade of the monster spawn or null if it does not have one.
     */
    Barricade getBarricade();

    /**
     * Gets the spawn location of certain mob types.
     *
     * @param mobType The mob type to gets its spawn location of.
     * @return The spawn location of the specified mob type.
     */
    Location getSpawnLocation(MobType mobType);

    /**
     * Sets the barricade of the monster spawn.
     *
     * @param barricade The barricade.
     */
    void setBarricade(Barricade barricade);
}
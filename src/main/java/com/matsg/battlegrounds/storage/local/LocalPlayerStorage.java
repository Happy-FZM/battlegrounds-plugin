package com.matsg.battlegrounds.storage.local;

import com.matsg.battlegrounds.api.Battlegrounds;
import com.matsg.battlegrounds.api.storage.StoredPlayer;
import com.matsg.battlegrounds.api.item.Loadout;
import com.matsg.battlegrounds.api.entity.OfflineGamePlayer;
import com.matsg.battlegrounds.api.storage.PlayerStorage;
import com.matsg.battlegrounds.storage.DefaultLoadouts;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LocalPlayerStorage implements PlayerStorage {

    private Battlegrounds plugin;
    private DefaultLoadouts defaultLoadouts;
    private File folder;
    private List<StoredPlayer> storedPlayers;

    public LocalPlayerStorage(Battlegrounds plugin) throws IOException {
        this.plugin = plugin;
        this.defaultLoadouts = new DefaultLoadouts(plugin);
        this.folder = new File(plugin.getDataFolder().getPath() + "/players");
        this.storedPlayers = new ArrayList<>();

        if (!folder.exists()) {
            folder.mkdirs();
        }

        for (File file : folder.listFiles()) {
            storedPlayers.add(new PlayerYaml(plugin, UUID.fromString(file.getName().substring(0, file.getName().length() - 4))));
        }
    }

    public List<? extends OfflineGamePlayer> getList() {
        return storedPlayers;
    }

    public boolean contains(UUID uuid) {
        return getStoredPlayer(uuid) != null;
    }

    public StoredPlayer getStoredPlayer(UUID uuid) {
        for (StoredPlayer storedPlayer : storedPlayers) {
            if (storedPlayer.getUUID().equals(uuid)) {
                return storedPlayer;
            }
        }
        return null;
    }

    public List<? extends OfflineGamePlayer> getTopPlayers(int limit) {
        List<? extends OfflineGamePlayer> list = getList();
        Collections.sort(list, new Comparator<OfflineGamePlayer>() {
            public int compare(OfflineGamePlayer o1, OfflineGamePlayer o2) {
                return ((Integer) o2.getExp()).compareTo(o1.getExp()); // Reverse sort
            }
        });
        return list.stream().limit(limit).collect(Collectors.toList());
    }

    public StoredPlayer registerPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        try {
            StoredPlayer storedPlayer = new PlayerYaml(plugin, uuid);
            storedPlayer.createDefaultAttributes(player);

            for (int i = 1; i <= 5; i ++) {
                Loadout loadout = defaultLoadouts.getList().get(i - 1);
                storedPlayer.saveLoadout(loadout.getLoadoutNr(), loadout);
            }
            storedPlayers.add(storedPlayer);
        } catch (IOException e) {
            return null;
        }
        return getStoredPlayer(uuid);
    }

    public StoredPlayer updatePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        StoredPlayer storedPlayer = getStoredPlayer(uuid);
        storedPlayer.updateName(player.getName());
        return storedPlayer;
    }
}
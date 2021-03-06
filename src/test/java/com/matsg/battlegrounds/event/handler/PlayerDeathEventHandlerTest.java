package com.matsg.battlegrounds.event.handler;

import com.matsg.battlegrounds.BattleGameManager;
import com.matsg.battlegrounds.api.Battlegrounds;
import com.matsg.battlegrounds.api.GameManager;
import com.matsg.battlegrounds.api.event.GamePlayerDeathEvent.DeathCause;
import com.matsg.battlegrounds.api.game.Game;
import com.matsg.battlegrounds.api.game.PlayerManager;
import com.matsg.battlegrounds.api.gamemode.GameMode;
import com.matsg.battlegrounds.api.player.GamePlayer;
import com.matsg.battlegrounds.player.BattleGamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DeathCause.class)
public class PlayerDeathEventHandlerTest {

    private Battlegrounds plugin;
    private Game game;
    private GameManager gameManager;
    private GameMode gameMode;
    private GamePlayer gamePlayer;
    private Player player;
    private PlayerDeathEvent event;
    private PlayerManager playerManager;

    @Before
    public void setUp() {
        this.plugin = mock(Battlegrounds.class);
        this.game = mock(Game.class);
        this.gameMode = mock(GameMode.class);
        this.gamePlayer = mock(GamePlayer.class);
        this.player = mock(Player.class);
        this.playerManager = mock(PlayerManager.class);

        PowerMockito.mockStatic(DeathCause.class);

        this.event = new PlayerDeathEvent(player, null, 0, "Test");
        this.gameManager = new BattleGameManager();
        this.gamePlayer = new BattleGamePlayer(player, null);

        gameManager.getGames().add(game);

        when(game.getGameMode()).thenReturn(gameMode);
        when(game.getPlayerManager()).thenReturn(playerManager);
        when(plugin.getGameManager()).thenReturn(gameManager);
    }

    @Test
    public void testPlayerDeathWhenNotPlaying() {
        when(playerManager.getGamePlayer(player)).thenReturn(null);

        PlayerDeathEventHandler eventHandler = new PlayerDeathEventHandler(plugin);
        eventHandler.handle(event);

        assertEquals("Test", event.getDeathMessage());
        assertFalse(event.getKeepInventory());
        assertFalse(event.getKeepLevel());
    }

    @Test
    public void testUnknownDeathCause() {
        when(playerManager.getGamePlayer(player)).thenReturn(gamePlayer);

        PlayerDeathEventHandler eventHandler = new PlayerDeathEventHandler(plugin);
        eventHandler.handle(event);

        assertEquals(0, event.getDroppedExp());
        assertNull(event.getDeathMessage());
        assertTrue(event.getKeepInventory());
        assertTrue(event.getKeepLevel());

        verify(game, times(0)).callEvent(any(Event.class));
        verify(gameMode, times(0)).onDeath(any(GamePlayer.class), any(DeathCause.class));
    }

    @Test
    public void testEventCallAndGameModeDeathMethod() {
        EntityDamageEvent damageCause = new EntityDamageEvent(player, DamageCause.ENTITY_ATTACK, 0);

        when(player.getLastDamageCause()).thenReturn(damageCause);
        when(playerManager.getGamePlayer(player)).thenReturn(gamePlayer);

        when(DeathCause.fromDamageCause(any(DamageCause.class))).thenReturn(DeathCause.PLAYER_KILL);

        PlayerDeathEventHandler eventHandler = new PlayerDeathEventHandler(plugin);
        eventHandler.handle(event);

        assertEquals(0, event.getDroppedExp());
        assertNull(event.getDeathMessage());
        assertTrue(event.getKeepInventory());
        assertTrue(event.getKeepLevel());

        verify(game, times(1)).callEvent(any(Event.class));
        verify(gameMode, times(1)).onDeath(gamePlayer, DeathCause.PLAYER_KILL);
    }
}

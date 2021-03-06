package com.matsg.battlegrounds.event.handler;

import com.matsg.battlegrounds.gui.View;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class InventoryClickEventHandlerTest {

    private Inventory inventory;
    private InventoryClickEvent event;
    private Player player;

    @Before
    public void setUp() {
        this.inventory = mock(Inventory.class);
        this.event = mock(InventoryClickEvent.class);
        this.player = mock(Player.class);

        when(event.getInventory()).thenReturn(inventory);
        when(event.getWhoClicked()).thenReturn(player);
    }

    @Test
    public void testInventoryClickNoView() {
        InventoryClickEventHandler eventHandler = new InventoryClickEventHandler();
        eventHandler.handle(event);

        verify(event, times(0)).setCancelled(true);
    }

    @Test
    public void testInventoryClickWithView() {
        when(inventory.getHolder()).thenReturn(mock(View.class));

        InventoryClickEventHandler eventHandler = new InventoryClickEventHandler();
        eventHandler.handle(event);

        verify(event, times(1)).setCancelled(true);
    }
}

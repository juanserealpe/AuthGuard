package org.authguard.listeners;

import org.authguard.services.AuthService;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class AuthListener implements Listener {

    private final AuthService authService;
    private final String notAuthenticatedMessage;

    public AuthListener(AuthService authService, String notAuthenticatedMessage) {
        this.authService = authService;
        this.notAuthenticatedMessage = ChatColor.translateAlternateColorCodes('&', notAuthenticatedMessage);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!authService.isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            // Optionally teleport back to prevent slight jitter
            // event.getPlayer().teleport(event.getFrom());
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!authService.isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(notAuthenticatedMessage);
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase();
        if (!authService.isAuthenticated(event.getPlayer().getUniqueId())) {
            if (!command.startsWith("/login") && !command.startsWith("/register")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(notAuthenticatedMessage);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!authService.isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!authService.isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!authService.isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!authService.isAuthenticated(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        authService.logout(event.getPlayer().getUniqueId());
    }
}

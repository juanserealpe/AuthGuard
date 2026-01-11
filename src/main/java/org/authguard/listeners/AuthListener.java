package org.authguard.listeners;

import org.authguard.services.AuthService;
import org.authguard.services.SpawnManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthListener implements Listener {

    private final AuthService authService;
    private final SpawnManager spawnManager;
    private final String notAuthenticatedMessage;
    private final String notRegisteredMessage;
    private final String notRegisteredCommandBlocked;
    private final String registeredCommandBlocked;
    private final Plugin plugin;
    private final Map<UUID, Integer> reminderTasks = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> registrationCache = new ConcurrentHashMap<>();

    public AuthListener(
            AuthService authService,
            SpawnManager spawnManager,
            String notRegisteredMessage,
            String notAuthenticatedMessage,
            String notRegisteredCommandBlocked,
            String registeredCommandBlocked,
            Plugin plugin) {
        this.authService = authService;
        this.spawnManager = spawnManager;
        this.notAuthenticatedMessage = ChatColor.translateAlternateColorCodes('&', notAuthenticatedMessage);
        this.notRegisteredMessage = ChatColor.translateAlternateColorCodes('&', notRegisteredMessage);
        this.notRegisteredCommandBlocked = ChatColor.translateAlternateColorCodes('&', notRegisteredCommandBlocked);
        this.registeredCommandBlocked = ChatColor.translateAlternateColorCodes('&', registeredCommandBlocked);

        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (authService.isAuthenticated(uuid))
            return;

        authService.isRegistered(uuid).thenAccept(isRegistered -> {
            registrationCache.put(uuid, isRegistered);
            Bukkit.getScheduler().runTask(plugin, () -> {
                // Teleport to spawn if enabled and player is not authenticated
                if (!authService.isAuthenticated(uuid)) {
                    Optional<Location> spawn = spawnManager.getSpawn();
                    spawn.ifPresent(player::teleport);
                }

                int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

                    if (!player.isOnline()) {
                        cancelReminder(uuid);
                        return;
                    }

                    if (authService.isAuthenticated(uuid)) {
                        cancelReminder(uuid);
                        return;
                    }

                    if (registrationCache.getOrDefault(uuid, false)) {
                        player.sendMessage(notAuthenticatedMessage);
                    } else {
                        player.sendMessage(notRegisteredMessage);
                    }

                }, 0L, 5 * 20L).getTaskId();
                reminderTasks.put(uuid, taskId);
            });
        });
    }

    private void cancelReminder(UUID uuid) {
        Integer taskId = reminderTasks.remove(uuid);
        if (taskId != null)
            Bukkit.getScheduler().cancelTask(taskId);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!authService.isAuthenticated(event.getPlayer().getUniqueId()))
            event.setCancelled(true);
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
        UUID uuid = event.getPlayer().getUniqueId();
        if (!authService.isAuthenticated(uuid)) {
            String cmd = event.getMessage().toLowerCase();
            Boolean isRegistered = registrationCache.get(uuid);

            if (isRegistered != null && isRegistered) {
                // Registered user - only allow /login
                if (!cmd.startsWith("/login")) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(registeredCommandBlocked);
                }
            } else {
                // Non-registered user - only allow /register
                if (!cmd.startsWith("/register")) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(notRegisteredCommandBlocked);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!authService.isAuthenticated(event.getPlayer().getUniqueId()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!authService.isAuthenticated(event.getPlayer().getUniqueId()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!authService.isAuthenticated(event.getPlayer().getUniqueId()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!authService.isAuthenticated(event.getPlayer().getUniqueId()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        cancelReminder(uuid);
        registrationCache.remove(uuid);
        authService.logout(uuid);
    }
}

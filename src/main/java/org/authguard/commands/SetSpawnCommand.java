package org.authguard.commands;

import org.authguard.services.SpawnManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetSpawnCommand implements CommandExecutor {

    private final SpawnManager spawnManager;
    private final String successMessage;
    private final String noPermissionMessage;
    private final String consoleErrorMessage;

    public SetSpawnCommand(SpawnManager spawnManager, String successMessage,
            String noPermissionMessage, String consoleErrorMessage) {
        this.spawnManager = spawnManager;
        this.successMessage = ChatColor.translateAlternateColorCodes('&', successMessage);
        this.noPermissionMessage = ChatColor.translateAlternateColorCodes('&', noPermissionMessage);
        this.consoleErrorMessage = ChatColor.translateAlternateColorCodes('&', consoleErrorMessage);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(consoleErrorMessage);
            return true;
        }

        if (!player.hasPermission("authguard.spawn.manage")) {
            player.sendMessage(noPermissionMessage);
            return true;
        }

        spawnManager.setSpawn(player.getLocation());
        player.sendMessage(successMessage);
        return true;
    }
}

package org.authguard.commands;

import org.authguard.services.AuthService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LoginCommand implements CommandExecutor {

    private final AuthService authService;
    private final String usageMessage;
    private final String invalidPasswordMessage;
    private final String successMessage;
    private final String notRegisteredMessage;
    private final String internalErrorMessage;

    public LoginCommand(AuthService authService, String usageMessage, String invalidPasswordMessage,
            String successMessage, String notRegisteredMessage, String internalErrorMessage) {
        this.authService = authService;
        this.usageMessage = ChatColor.translateAlternateColorCodes('&', usageMessage);
        this.invalidPasswordMessage = ChatColor.translateAlternateColorCodes('&', invalidPasswordMessage);
        this.successMessage = ChatColor.translateAlternateColorCodes('&', successMessage);
        this.notRegisteredMessage = ChatColor.translateAlternateColorCodes('&', notRegisteredMessage);
        this.internalErrorMessage = ChatColor.translateAlternateColorCodes('&', internalErrorMessage);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(usageMessage);
            return true;
        }

        if (authService.isAuthenticated(player.getUniqueId())) {
            player.sendMessage(ChatColor.GREEN + "You are already logged in!");
            return true;
        }

        String password = args[0];

        authService.login(player.getUniqueId(), password).thenAccept(success -> {
            if (success) {
                player.sendMessage(successMessage);
            } else {
                // We should distinguish between "not registered" and "wrong password" if
                // possible,
                // but for now the service returns false for both or similar.
                // Let's check AuthService.login logic.
                player.sendMessage(invalidPasswordMessage);
            }
        }).exceptionally(ex -> {
            player.sendMessage(internalErrorMessage);
            return null;
        });

        return true;
    }
}

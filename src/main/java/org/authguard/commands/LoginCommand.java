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
    private final String invalidPasswordMessage;
    private final String successMessage;
    private final String internalErrorMessage;
    private final String alreadyAuthenticatedMessage;
    private final String errorConsoleCommandMessage;

    public LoginCommand(AuthService authService, String invalidPasswordMessage,
            String successMessage, String internalErrorMessage, String alreadyAuthenticatedMessage,
            String errorConsoleCommandMessage) {
        this.authService = authService;
        this.invalidPasswordMessage = ChatColor.translateAlternateColorCodes('&', invalidPasswordMessage);
        this.successMessage = ChatColor.translateAlternateColorCodes('&', successMessage);
        this.internalErrorMessage = ChatColor.translateAlternateColorCodes('&', internalErrorMessage);
        this.alreadyAuthenticatedMessage = ChatColor.translateAlternateColorCodes('&', alreadyAuthenticatedMessage);
        this.errorConsoleCommandMessage = ChatColor.translateAlternateColorCodes('&', errorConsoleCommandMessage);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(errorConsoleCommandMessage);
            return true;
        }

        if (authService.isAuthenticated(player.getUniqueId())) {
            player.sendMessage(alreadyAuthenticatedMessage);
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(invalidPasswordMessage);
            return true;
        }

        String password = args[0];

        authService.login(player.getUniqueId(), password).thenAccept(success -> {
            if (success) {
                player.sendMessage(successMessage);
            } else {
                player.sendMessage(invalidPasswordMessage);
            }
        }).exceptionally(ex -> {
            player.sendMessage(internalErrorMessage);
            return null;
        });
        return true;
    }
}

package org.authguard.commands;

import org.authguard.services.AuthService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RegisterCommand implements CommandExecutor {

    private final AuthService authService;
    private final String usageMessage;
    private final String alreadyRegisteredMessage;
    private final String successMessage;
    private final String internalErrorMessage;

    public RegisterCommand(AuthService authService, String usageMessage, String alreadyRegisteredMessage,
            String successMessage, String internalErrorMessage) {
        this.authService = authService;
        this.usageMessage = ChatColor.translateAlternateColorCodes('&', usageMessage);
        this.alreadyRegisteredMessage = ChatColor.translateAlternateColorCodes('&', alreadyRegisteredMessage);
        this.successMessage = ChatColor.translateAlternateColorCodes('&', successMessage);
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

        String password = args[0];

        authService.register(player.getUniqueId(), player.getName(), password).thenAccept(success -> {
            if (success) {
                player.sendMessage(successMessage);
            } else {
                player.sendMessage(alreadyRegisteredMessage);
            }
        }).exceptionally(ex -> {
            player.sendMessage(internalErrorMessage);
            return null;
        });

        return true;
    }
}

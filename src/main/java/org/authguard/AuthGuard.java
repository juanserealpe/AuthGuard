package org.authguard;

import org.authguard.commands.LoginCommand;
import org.authguard.commands.RegisterCommand;
import org.authguard.infrastructure.LuckPermsIntegration;
import org.authguard.listeners.AuthListener;
import org.authguard.repositories.IUserRepository;
import org.authguard.repositories.SQLiteUserRepository;
import org.authguard.services.AuthService;
import org.authguard.services.SessionManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.logging.Logger;

public final class AuthGuard extends JavaPlugin {

    private static AuthGuard instance;
    private IUserRepository userRepository;
    private AuthService authService;
    private SessionManager sessionManager;
    private LuckPermsIntegration luckPermsIntegration;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        Logger logger = getLogger();
        logger.info("AuthGuard is enabling...");

        // Initialize infrastructure
        File dbFile = new File(getDataFolder(), getConfig().getString("database.file", "authguard.db"));
        if (!dbFile.getParentFile().exists()) {
            dbFile.getParentFile().mkdirs();
        }

        this.userRepository = new SQLiteUserRepository(dbFile.getAbsolutePath(), logger);
        this.sessionManager = new SessionManager();
        this.luckPermsIntegration = new LuckPermsIntegration();

        String defaultRank = getConfig().getString("default-rank", "player");
        this.authService = new AuthService(userRepository, sessionManager, luckPermsIntegration, defaultRank);

        // Register listeners
        String notAuthenticatedMessage = getConfig().getString("messages.not-authenticated");
        getServer().getPluginManager().registerEvents(new AuthListener(authService, notAuthenticatedMessage), this);

        // Register commands
        getCommand("register").setExecutor(new RegisterCommand(
                authService,
                getConfig().getString("messages.usage-register"),
                getConfig().getString("messages.already-registered"),
                getConfig().getString("messages.registration-success"),
                getConfig().getString("messages.internal-error")));

        getCommand("login").setExecutor(new LoginCommand(
                authService,
                getConfig().getString("messages.usage-login"),
                getConfig().getString("messages.invalid-password"),
                getConfig().getString("messages.login-success"),
                getConfig().getString("messages.not-registered"),
                getConfig().getString("messages.internal-error")));

        logger.info("AuthGuard has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (userRepository != null) {
            userRepository.close();
        }
        if (sessionManager != null) {
            sessionManager.clear();
        }
        getLogger().info("AuthGuard has been disabled.");
    }

    public static AuthGuard getInstance() {
        return instance;
    }

    public AuthService getAuthService() {
        return authService;
    }
}

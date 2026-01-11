package org.authguard.services;

import org.authguard.infrastructure.LuckPermsIntegration;
import org.authguard.models.User;
import org.authguard.repositories.IUserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AuthService {

    private final IUserRepository userRepository;
    private final SessionManager sessionManager;
    private final LuckPermsIntegration luckPermsIntegration;
    private final String defaultRank;

    public AuthService(IUserRepository userRepository, SessionManager sessionManager,
                       LuckPermsIntegration luckPermsIntegration, String defaultRank) {
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
        this.luckPermsIntegration = luckPermsIntegration;
        this.defaultRank = defaultRank;
    }

    public CompletableFuture<Boolean> register(UUID uuid, String username, String password) {
        return userRepository.findByUuid(uuid).thenCompose(optionalUser -> {
            if (optionalUser.isPresent()) {
                return CompletableFuture.completedFuture(false);
            }

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            User newUser = new User(uuid, username, hashedPassword, defaultRank, LocalDateTime.now());

            return userRepository.save(newUser).thenApply(v -> {
                sessionManager.authenticate(uuid);
                luckPermsIntegration.assignRank(uuid, newUser.getRank());
                return true;
            });
        });
    }

    public CompletableFuture<Boolean> login(UUID uuid, String password) {
        return userRepository.findByUuid(uuid).thenApply(optionalUser -> {
            if (optionalUser.isEmpty()) {
                return false;
            }

            User user = optionalUser.get();
            if (BCrypt.checkpw(password, user.getPasswordHash())) {
                sessionManager.authenticate(uuid);
                luckPermsIntegration.assignRank(uuid, user.getRank());
                return true;
            }
            return false;
        });
    }

    public boolean isAuthenticated(UUID uuid) {
        return sessionManager.isAuthenticated(uuid);
    }

    public void logout(UUID uuid) {
        sessionManager.logout(uuid);
    }
}

package org.authguard.services;

import org.authguard.infrastructure.LuckPermsIntegration;
import org.authguard.models.User;
import org.authguard.repositories.IUserRepository;
import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDateTime;
import java.util.Optional;
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
            if (optionalUser.isPresent()) return CompletableFuture.completedFuture(false);
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
            User newUser = new User(uuid, username, hashedPassword, defaultRank, LocalDateTime.now(), LocalDateTime.now());
            return userRepository.save(newUser).thenApply(v -> {
                sessionManager.authenticate(uuid);
                luckPermsIntegration.assignRank(uuid, newUser.getRank());
                return true;
            });
        });
    }

    public CompletableFuture<Boolean> login(UUID uuid, String password) {
        return userRepository.findByUuid(uuid).thenCompose(optionalUser -> {
            if (optionalUser.isEmpty()) return CompletableFuture.completedFuture(false);
            User user = optionalUser.get();
            if (!BCrypt.checkpw(password, user.getPasswordHash())) return CompletableFuture.completedFuture(false);
            sessionManager.authenticate(uuid);
            luckPermsIntegration.assignRank(uuid, user.getRank());

            return userRepository.updateLastLogin(uuid)
                    .thenApply(v -> true);
        });
    }

    public boolean isAuthenticated(UUID uuid) {
        return sessionManager.isAuthenticated(uuid);
    }

    public CompletableFuture<Boolean> isRegistered(UUID uuid) {
        return userRepository.findByUuid(uuid)
                .thenApply(Optional::isPresent);
    }

    public void logout(UUID uuid) {
        sessionManager.logout(uuid);
    }
}

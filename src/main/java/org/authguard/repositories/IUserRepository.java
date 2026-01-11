package org.authguard.repositories;

import org.authguard.models.User;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IUserRepository {
    CompletableFuture<Optional<User>> findByUuid(UUID uuid);

    CompletableFuture<Void> save(User user);

    CompletableFuture<Void> updateLastLogin(UUID uuid);

    void close();
}

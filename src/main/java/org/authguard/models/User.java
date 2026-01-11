package org.authguard.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {
    private final UUID uuid;
    private final String username;
    private final String passwordHash;
    private final String rank;
    private final LocalDateTime createdAt;

    public User(UUID uuid, String username, String passwordHash, String rank, LocalDateTime createdAt) {
        this.uuid = uuid;
        this.username = username;
        this.passwordHash = passwordHash;
        this.rank = rank;
        this.createdAt = createdAt;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRank() {
        return rank;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

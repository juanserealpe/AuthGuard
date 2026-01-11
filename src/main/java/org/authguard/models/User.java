package org.authguard.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {
    private final UUID uuid;
    private final String username;
    private final String passwordHash;
    private final String rank;
    private final LocalDateTime createdAt;
    private final LocalDateTime lastConnection;

    public User(UUID uuid, String username, String passwordHash, String rank, LocalDateTime createdAt, LocalDateTime lastConnection) {
        this.uuid = uuid;
        this.username = username;
        this.passwordHash = passwordHash;
        this.rank = rank;
        this.createdAt = createdAt;
        this.lastConnection = lastConnection;
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

    public LocalDateTime getLastConnection(){
        return lastConnection;
    }

}

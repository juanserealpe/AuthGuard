package org.authguard.services;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SessionManager {
    private final Set<UUID> authenticatedPlayers = new HashSet<>();

    public void authenticate(UUID uuid) {
        authenticatedPlayers.add(uuid);
    }

    public void logout(UUID uuid) {
        authenticatedPlayers.remove(uuid);
    }

    public boolean isAuthenticated(UUID uuid) {
        return authenticatedPlayers.contains(uuid);
    }

    public void clear() {
        authenticatedPlayers.clear();
    }
}

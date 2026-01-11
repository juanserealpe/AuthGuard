package org.authguard.services;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private final Set<UUID> authenticatedPlayers = ConcurrentHashMap.newKeySet();

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

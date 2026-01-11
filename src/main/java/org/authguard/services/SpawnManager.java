package org.authguard.services;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

public class SpawnManager {

    private final Plugin plugin;
    private final FileConfiguration config;

    public SpawnManager(Plugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public void setSpawn(Location location) {
        config.set("spawn.enabled", true);
        config.set("spawn.world", location.getWorld().getName());
        config.set("spawn.x", location.getX());
        config.set("spawn.y", location.getY());
        config.set("spawn.z", location.getZ());
        config.set("spawn.yaw", location.getYaw());
        config.set("spawn.pitch", location.getPitch());
        plugin.saveConfig();
    }

    public void deleteSpawn() {
        config.set("spawn.enabled", false);
        plugin.saveConfig();
    }

    public boolean isSpawnEnabled() {
        return config.getBoolean("spawn.enabled", false);
    }

    public Optional<Location> getSpawn() {
        if (!isSpawnEnabled()) {
            return Optional.empty();
        }

        String worldName = config.getString("spawn.world");
        if (worldName == null || worldName.isEmpty()) {
            return Optional.empty();
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return Optional.empty();
        }

        double x = config.getDouble("spawn.x");
        double y = config.getDouble("spawn.y");
        double z = config.getDouble("spawn.z");
        float yaw = (float) config.getDouble("spawn.yaw");
        float pitch = (float) config.getDouble("spawn.pitch");

        return Optional.of(new Location(world, x, y, z, yaw, pitch));
    }
}

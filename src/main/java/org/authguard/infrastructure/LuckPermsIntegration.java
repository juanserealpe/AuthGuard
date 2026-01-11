package org.authguard.infrastructure;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import java.util.UUID;

public class LuckPermsIntegration {

    private LuckPerms luckPerms;

    public LuckPermsIntegration() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager()
                    .getRegistration(LuckPerms.class);
            if (provider != null) {
                this.luckPerms = provider.getProvider();
            }
        }
    }

    public boolean isAvailable() {
        return luckPerms != null;
    }

    public void assignRank(UUID uuid, String rank) {
        if (!isAvailable()) return;
        luckPerms.getUserManager().modifyUser(uuid, user -> {
            Node node = Node.builder("group." + rank).build();
            user.data().add(node);
        });
    }
}

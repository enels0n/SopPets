package net.enelson.soppets.service;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import net.enelson.soppets.SopPetsPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

public final class PlayerPetStorage {
    private final SopPetsPlugin plugin;
    private YamlConfiguration config;
    private File file;

    public PlayerPetStorage(SopPetsPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.file = new File(this.plugin.getDataFolder(), "players.yml");
        if (!this.file.exists()) {
            this.plugin.saveResource("players.yml", true);
        }
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public String getActivePet(UUID playerId) {
        return this.config.getString("players." + playerId.toString() + ".active-pet");
    }

    public void setActivePet(UUID playerId, String petId) {
        this.config.set("players." + playerId.toString() + ".active-pet", petId);
        save();
    }

    private void save() {
        try {
            this.config.save(this.file);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save players.yml", exception);
        }
    }
}

package net.enelson.soppets.service;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import net.enelson.soppets.SopPetsPlugin;
import net.enelson.soppets.model.PetDefinition;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public final class PetDefinitionService {
    private final SopPetsPlugin plugin;
    private final Map<String, PetDefinition> pets = new LinkedHashMap<String, PetDefinition>();

    public PetDefinitionService(SopPetsPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.pets.clear();
        File file = new File(this.plugin.getDataFolder(), "pets.yml");
        if (!file.exists()) {
            this.plugin.saveResource("pets.yml", true);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("pets");
        if (section == null) {
            return;
        }

        for (String petId : section.getKeys(false)) {
            String base = "pets." + petId;
            EntityType entityType;
            try {
                entityType = EntityType.valueOf(config.getString(base + ".entity-type", "FOX").toUpperCase());
            } catch (IllegalArgumentException exception) {
                this.plugin.getLogger().warning("Skipping pet '" + petId + "' because entity-type is invalid.");
                continue;
            }

            Vector offset = new Vector(
                config.getDouble(base + ".follow-offset.x", 1.0D),
                config.getDouble(base + ".follow-offset.y", 0.0D),
                config.getDouble(base + ".follow-offset.z", 1.0D)
            );

            PetDefinition definition = new PetDefinition(
                petId,
                ChatColor.translateAlternateColorCodes('&', config.getString(base + ".display-name", "&f" + petId)),
                entityType,
                config.getString(base + ".model-id", ""),
                config.getBoolean(base + ".use-fmm", false),
                config.getBoolean(base + ".baby", false),
                config.getBoolean(base + ".glowing", false),
                offset
            );
            this.pets.put(petId.toLowerCase(), definition);
        }
    }

    public PetDefinition getPet(String petId) {
        return petId == null ? null : this.pets.get(petId.toLowerCase());
    }

    public Collection<PetDefinition> getPets() {
        return this.pets.values();
    }
}

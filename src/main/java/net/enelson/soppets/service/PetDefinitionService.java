package net.enelson.soppets.service;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import net.enelson.soppets.SopPetsPlugin;
import net.enelson.soppets.model.PetDefinition;
import net.enelson.soppets.model.PetLevelOverride;
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
                config.getString(base + ".icon.material", ""),
                ChatColor.translateAlternateColorCodes('&', config.getString(base + ".icon.name", config.getString(base + ".display-name", "&f" + petId))),
                translateLore(config.getStringList(base + ".icon.lore")),
                Math.max(0, config.getInt(base + ".icon.custom-model-data", 0)),
                config.getDouble(base + ".movement-speed", 0.35D),
                readAppearanceSettings(config.getConfigurationSection(base + ".appearance")),
                offset,
                readLevelOverrides(config.getConfigurationSection(base + ".levels"))
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

    private java.util.List<String> translateLore(java.util.List<String> lines) {
        java.util.List<String> translated = new java.util.ArrayList<String>();
        for (String line : lines) {
            translated.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        return translated;
    }

    private Map<String, Object> readAppearanceSettings(ConfigurationSection section) {
        if (section == null) {
            return java.util.Collections.emptyMap();
        }
        return new LinkedHashMap<String, Object>(section.getValues(false));
    }

    private Map<Integer, PetLevelOverride> readLevelOverrides(ConfigurationSection section) {
        Map<Integer, PetLevelOverride> overrides = new LinkedHashMap<Integer, PetLevelOverride>();
        if (section == null) {
            return overrides;
        }

        for (String levelKey : section.getKeys(false)) {
            int level;
            try {
                level = Integer.parseInt(levelKey);
            } catch (NumberFormatException exception) {
                continue;
            }

            String base = section.getCurrentPath() + "." + levelKey;
            Vector offset = null;
            if (section.contains(levelKey + ".follow-offset")) {
                offset = new Vector(
                    section.getDouble(levelKey + ".follow-offset.x", 1.0D),
                    section.getDouble(levelKey + ".follow-offset.y", 0.0D),
                    section.getDouble(levelKey + ".follow-offset.z", 1.0D)
                );
            }

            overrides.put(level, new PetLevelOverride(
                level,
                section.contains(levelKey + ".display-name")
                    ? ChatColor.translateAlternateColorCodes('&', section.getString(levelKey + ".display-name", "&f"))
                    : null,
                section.contains(levelKey + ".model-id") ? section.getString(levelKey + ".model-id", "") : null,
                section.contains(levelKey + ".glowing") ? Boolean.valueOf(section.getBoolean(levelKey + ".glowing")) : null,
                section.contains(levelKey + ".icon.material") ? section.getString(levelKey + ".icon.material", "") : null,
                section.contains(levelKey + ".icon.name")
                    ? ChatColor.translateAlternateColorCodes('&', section.getString(levelKey + ".icon.name", "&f"))
                    : null,
                section.contains(levelKey + ".icon.lore") ? translateLore(section.getStringList(levelKey + ".icon.lore")) : null,
                section.contains(levelKey + ".icon.custom-model-data") ? Integer.valueOf(Math.max(0, section.getInt(levelKey + ".icon.custom-model-data", 0))) : null,
                section.contains(levelKey + ".movement-speed") ? Double.valueOf(section.getDouble(levelKey + ".movement-speed", 0.35D)) : null,
                readAppearanceSettings(section.getConfigurationSection(levelKey + ".appearance")),
                offset
            ));
        }

        return overrides;
    }
}

package net.enelson.soppets.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public final class PetDefinition {
    private final String id;
    private final String displayName;
    private final EntityType entityType;
    private final String modelId;
    private final boolean useFmm;
    private final boolean baby;
    private final boolean glowing;
    private final String iconMaterialName;
    private final String iconDisplayName;
    private final List<String> iconLore;
    private final int iconCustomModelData;
    private final double movementSpeed;
    private final Map<String, Object> appearanceSettings;
    private final Vector followOffset;
    private final Map<Integer, PetLevelOverride> levelOverrides;

    public PetDefinition(
        String id,
        String displayName,
        EntityType entityType,
        String modelId,
        boolean useFmm,
        boolean baby,
        boolean glowing,
        String iconMaterialName,
        String iconDisplayName,
        List<String> iconLore,
        int iconCustomModelData,
        double movementSpeed,
        Map<String, Object> appearanceSettings,
        Vector followOffset,
        Map<Integer, PetLevelOverride> levelOverrides
    ) {
        this.id = id;
        this.displayName = displayName;
        this.entityType = entityType;
        this.modelId = modelId;
        this.useFmm = useFmm;
        this.baby = baby;
        this.glowing = glowing;
        this.iconMaterialName = iconMaterialName;
        this.iconDisplayName = iconDisplayName;
        this.iconLore = iconLore == null ? Collections.<String>emptyList() : new ArrayList<String>(iconLore);
        this.iconCustomModelData = iconCustomModelData;
        this.movementSpeed = movementSpeed;
        this.appearanceSettings = appearanceSettings == null ? Collections.<String, Object>emptyMap() : new LinkedHashMap<String, Object>(appearanceSettings);
        this.followOffset = followOffset;
        this.levelOverrides = levelOverrides == null ? Collections.<Integer, PetLevelOverride>emptyMap() : new LinkedHashMap<Integer, PetLevelOverride>(levelOverrides);
    }

    public String getId() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public EntityType getEntityType() {
        return this.entityType;
    }

    public String getModelId() {
        return this.modelId;
    }

    public boolean isUseFmm() {
        return this.useFmm;
    }

    public boolean isBaby() {
        return this.baby;
    }

    public boolean isGlowing() {
        return this.glowing;
    }

    public String getIconMaterialName() {
        return this.iconMaterialName;
    }

    public String getIconDisplayName() {
        return this.iconDisplayName;
    }

    public List<String> getIconLore() {
        return new ArrayList<String>(this.iconLore);
    }

    public int getIconCustomModelData() {
        return this.iconCustomModelData;
    }

    public double getMovementSpeed() {
        return this.movementSpeed;
    }

    public Map<String, Object> getAppearanceSettings() {
        return new LinkedHashMap<String, Object>(this.appearanceSettings);
    }

    public String getAppearanceString(String key) {
        Object value = this.appearanceSettings.get(key);
        return value == null ? null : String.valueOf(value);
    }

    public boolean hasAppearanceSetting(String key) {
        return this.appearanceSettings.containsKey(key);
    }

    public boolean getAppearanceBoolean(String key, boolean defaultValue) {
        Object value = this.appearanceSettings.get(key);
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }

    public int getAppearanceInt(String key, int defaultValue) {
        Object value = this.appearanceSettings.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException exception) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public Vector getFollowOffset() {
        return this.followOffset.clone();
    }

    public Map<Integer, PetLevelOverride> getLevelOverrides() {
        return new LinkedHashMap<Integer, PetLevelOverride>(this.levelOverrides);
    }
}

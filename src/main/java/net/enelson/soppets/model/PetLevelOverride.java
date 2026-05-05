package net.enelson.soppets.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.util.Vector;

public final class PetLevelOverride {
    private final int level;
    private final String displayName;
    private final String modelId;
    private final Boolean glowing;
    private final String iconMaterialName;
    private final String iconDisplayName;
    private final List<String> iconLore;
    private final Integer iconCustomModelData;
    private final Double movementSpeed;
    private final Map<String, Object> appearanceSettings;
    private final Vector followOffset;

    public PetLevelOverride(
        int level,
        String displayName,
        String modelId,
        Boolean glowing,
        String iconMaterialName,
        String iconDisplayName,
        List<String> iconLore,
        Integer iconCustomModelData,
        Double movementSpeed,
        Map<String, Object> appearanceSettings,
        Vector followOffset
    ) {
        this.level = level;
        this.displayName = displayName;
        this.modelId = modelId;
        this.glowing = glowing;
        this.iconMaterialName = iconMaterialName;
        this.iconDisplayName = iconDisplayName;
        this.iconLore = iconLore == null ? Collections.<String>emptyList() : new ArrayList<String>(iconLore);
        this.iconCustomModelData = iconCustomModelData;
        this.movementSpeed = movementSpeed;
        this.appearanceSettings = appearanceSettings == null ? Collections.<String, Object>emptyMap() : new LinkedHashMap<String, Object>(appearanceSettings);
        this.followOffset = followOffset == null ? null : followOffset.clone();
    }

    public int getLevel() {
        return this.level;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getModelId() {
        return this.modelId;
    }

    public Boolean getGlowing() {
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

    public Integer getIconCustomModelData() {
        return this.iconCustomModelData;
    }

    public Double getMovementSpeed() {
        return this.movementSpeed;
    }

    public Map<String, Object> getAppearanceSettings() {
        return new LinkedHashMap<String, Object>(this.appearanceSettings);
    }

    public Vector getFollowOffset() {
        return this.followOffset == null ? null : this.followOffset.clone();
    }
}

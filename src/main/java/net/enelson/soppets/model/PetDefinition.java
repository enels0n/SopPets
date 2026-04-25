package net.enelson.soppets.model;

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
    private final Vector followOffset;

    public PetDefinition(String id, String displayName, EntityType entityType, String modelId, boolean useFmm, boolean baby, boolean glowing, Vector followOffset) {
        this.id = id;
        this.displayName = displayName;
        this.entityType = entityType;
        this.modelId = modelId;
        this.useFmm = useFmm;
        this.baby = baby;
        this.glowing = glowing;
        this.followOffset = followOffset;
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

    public Vector getFollowOffset() {
        return this.followOffset.clone();
    }
}

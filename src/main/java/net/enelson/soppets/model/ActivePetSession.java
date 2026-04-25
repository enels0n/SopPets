package net.enelson.soppets.model;

import java.util.UUID;
import org.bukkit.entity.LivingEntity;

public final class ActivePetSession {
    public enum AnimationState {
        NONE,
        SPAWN,
        IDLE,
        WALK
    }

    private final UUID ownerId;
    private final PetDefinition definition;
    private final LivingEntity entity;
    private final Object fmmHandle;
    private AnimationState animationState;

    public ActivePetSession(UUID ownerId, PetDefinition definition, LivingEntity entity, Object fmmHandle) {
        this.ownerId = ownerId;
        this.definition = definition;
        this.entity = entity;
        this.fmmHandle = fmmHandle;
        this.animationState = AnimationState.NONE;
    }

    public UUID getOwnerId() {
        return this.ownerId;
    }

    public PetDefinition getDefinition() {
        return this.definition;
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    public Object getFmmHandle() {
        return this.fmmHandle;
    }

    public AnimationState getAnimationState() {
        return this.animationState;
    }

    public void setAnimationState(AnimationState animationState) {
        this.animationState = animationState;
    }
}

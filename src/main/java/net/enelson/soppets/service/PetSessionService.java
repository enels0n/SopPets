package net.enelson.soppets.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.enelson.soppets.SopPetsPlugin;
import net.enelson.soppets.model.ActivePetSession;
import net.enelson.soppets.model.PetDefinition;
import net.enelson.soppets.model.PetLevelOverride;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public final class PetSessionService {
    private final SopPetsPlugin plugin;
    private final PlayerPetStorage playerPetStorage;
    private final PetDefinitionService petDefinitionService;
    private final FmmBridgeService fmmBridgeService;
    private final VanillaPetAppearanceService vanillaPetAppearanceService;
    private final Map<UUID, ActivePetSession> sessions = new LinkedHashMap<UUID, ActivePetSession>();
    private PendingSpawn pendingSpawn;
    private BukkitTask followTask;

    public PetSessionService(
        SopPetsPlugin plugin,
        PlayerPetStorage playerPetStorage,
        PetDefinitionService petDefinitionService,
        FmmBridgeService fmmBridgeService,
        VanillaPetAppearanceService vanillaPetAppearanceService
    ) {
        this.plugin = plugin;
        this.playerPetStorage = playerPetStorage;
        this.petDefinitionService = petDefinitionService;
        this.fmmBridgeService = fmmBridgeService;
        this.vanillaPetAppearanceService = vanillaPetAppearanceService;
    }

    public void reload() {
        for (UUID playerId : this.sessions.keySet().toArray(new UUID[0])) {
            Player player = Bukkit.getPlayer(playerId);
            String activePetId = this.playerPetStorage.getActivePet(playerId);
            despawnPet(playerId);
            if (player != null && player.isOnline() && activePetId != null && !activePetId.isEmpty()) {
                summonPet(player, activePetId);
            }
        }
    }

    public String summonPet(Player player, String petId) {
        PetDefinition definition = this.petDefinitionService.getPet(petId);
        if (definition == null) {
            return org.bukkit.ChatColor.RED + "Unknown pet.";
        }
        if (!hasAccess(player, definition)) {
            return org.bukkit.ChatColor.RED + "You do not have access to that pet.";
        }
        definition = resolveEffectiveDefinition(player, definition);

        despawnPet(player.getUniqueId());

        Location spawnLocation = resolveSpawnLocation(player, definition);
        this.pendingSpawn = new PendingSpawn(spawnLocation, definition.getEntityType());
        LivingEntity entity;
        try {
            entity = (LivingEntity) player.getWorld().spawnEntity(spawnLocation, definition.getEntityType());
        } finally {
            this.pendingSpawn = null;
        }
        preparePetEntity(player, entity, definition);
        Object fmmHandle = null;
        if (definition.isUseFmm()) {
            fmmHandle = this.fmmBridgeService.createModel(definition.getModelId(), entity);
            if (fmmHandle == null) {
                this.plugin.getLogger().warning(
                    "Failed to attach FMM model '" + definition.getModelId() + "' to pet '" + definition.getId() + "'."
                );
            }
        }
        ActivePetSession session = new ActivePetSession(player.getUniqueId(), definition, entity, fmmHandle);
        this.sessions.put(player.getUniqueId(), session);
        this.playerPetStorage.setActivePet(player.getUniqueId(), definition.getId());
        updateAnimationState(session, ActivePetSession.AnimationState.SPAWN);
        return null;
    }

    public boolean hasAccess(Player player, PetDefinition definition) {
        if (player == null || definition == null) {
            return false;
        }
        return player.hasPermission("soppets.admin")
            || player.hasPermission("soppets.pet.*")
            || player.hasPermission(getAccessPermission(definition));
    }

    public String getAccessPermission(PetDefinition definition) {
        return "soppets.pet." + definition.getId().toLowerCase();
    }

    public int getPetLevel(Player player, PetDefinition definition) {
        if (player == null || definition == null || !this.plugin.getConfig().getBoolean("pet-levels.enabled", false)) {
            return 1;
        }
        int resolved = Math.max(1, this.plugin.getConfig().getInt("pet-levels.default-level", 1));
        for (Integer level : definition.getLevelOverrides().keySet()) {
            if (level.intValue() > resolved && player.hasPermission(getLevelPermission(definition, level.intValue()))) {
                resolved = level.intValue();
            }
        }
        return resolved;
    }

    public String getLevelPermission(PetDefinition definition, int level) {
        return "soppets.pet." + definition.getId().toLowerCase() + ".level." + level;
    }

    public PetDefinition resolveEffectiveDefinition(Player player, PetDefinition baseDefinition) {
        if (baseDefinition == null) {
            return null;
        }
        int level = getPetLevel(player, baseDefinition);
        PetLevelOverride override = baseDefinition.getLevelOverrides().get(Integer.valueOf(level));
        if (override == null) {
            return baseDefinition;
        }

        Map<String, Object> mergedAppearance = baseDefinition.getAppearanceSettings();
        mergedAppearance.putAll(override.getAppearanceSettings());

        return new PetDefinition(
            baseDefinition.getId(),
            override.getDisplayName() == null ? baseDefinition.getDisplayName() : override.getDisplayName(),
            baseDefinition.getEntityType(),
            override.getModelId() == null ? baseDefinition.getModelId() : override.getModelId(),
            baseDefinition.isUseFmm(),
            baseDefinition.isBaby(),
            override.getGlowing() == null ? baseDefinition.isGlowing() : override.getGlowing().booleanValue(),
            override.getIconMaterialName() == null ? baseDefinition.getIconMaterialName() : override.getIconMaterialName(),
            override.getIconDisplayName() == null ? baseDefinition.getIconDisplayName() : override.getIconDisplayName(),
            override.getIconLore().isEmpty() ? baseDefinition.getIconLore() : override.getIconLore(),
            override.getIconCustomModelData() == null ? baseDefinition.getIconCustomModelData() : override.getIconCustomModelData().intValue(),
            override.getMovementSpeed() == null ? baseDefinition.getMovementSpeed() : override.getMovementSpeed().doubleValue(),
            mergedAppearance,
            override.getFollowOffset() == null ? baseDefinition.getFollowOffset() : override.getFollowOffset(),
            baseDefinition.getLevelOverrides()
        );
    }

    public void despawnPet(UUID playerId) {
        ActivePetSession session = this.sessions.remove(playerId);
        if (session == null) {
            return;
        }
        this.fmmBridgeService.stopCurrentAnimations(session.getFmmHandle());
        this.fmmBridgeService.removeModel(session.getFmmHandle());
        if (session.getEntity() != null && !session.getEntity().isDead()) {
            session.getEntity().remove();
        }
    }

    public boolean isPetEntity(org.bukkit.entity.Entity entity) {
        if (entity == null) {
            return false;
        }
        for (ActivePetSession session : this.sessions.values()) {
            if (session.getEntity() != null && session.getEntity().getUniqueId().equals(entity.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public boolean matchesPendingSpawn(Entity entity, Location location) {
        PendingSpawn pending = this.pendingSpawn;
        if (pending == null || entity == null || location == null) {
            return false;
        }
        if (pending.getEntityType() != entity.getType()) {
            return false;
        }
        if (pending.getLocation().getWorld() == null || location.getWorld() == null) {
            return false;
        }
        if (!pending.getLocation().getWorld().equals(location.getWorld())) {
            return false;
        }
        return pending.getLocation().distanceSquared(location) <= 4.0D;
    }

    public void startFollowTask() {
        stopFollowTask();
        final long interval = Math.max(1L, this.plugin.getConfig().getLong("follow.interval-ticks", 10L));
        this.followTask = Bukkit.getScheduler().runTaskTimer(this.plugin, new Runnable() {
            @Override
            public void run() {
                tickFollow();
            }
        }, interval, interval);
    }

    public void shutdown() {
        stopFollowTask();
        for (UUID playerId : this.sessions.keySet().toArray(new UUID[0])) {
            despawnPet(playerId);
        }
    }

    private void stopFollowTask() {
        if (this.followTask != null) {
            this.followTask.cancel();
            this.followTask = null;
        }
    }

    private void tickFollow() {
        double teleportDistance = this.plugin.getConfig().getDouble("follow.teleport-distance", 14.0D);
        double moveDistance = this.plugin.getConfig().getDouble("follow.move-distance", 3.0D);
        double verticalOffset = this.plugin.getConfig().getDouble("follow.vertical-offset", 0.2D);

        for (UUID playerId : this.sessions.keySet().toArray(new UUID[0])) {
            ActivePetSession session = this.sessions.get(playerId);
            if (session == null) {
                continue;
            }

            Player player = Bukkit.getPlayer(playerId);
            LivingEntity entity = session.getEntity();
            if (player == null || !player.isOnline() || entity == null || entity.isDead() || !entity.getWorld().equals(player.getWorld())) {
                despawnPet(playerId);
                continue;
            }

            Location target = resolveFollowLocation(player, session.getDefinition(), verticalOffset);
            double distance = entity.getLocation().distance(target);
            if (distance >= teleportDistance) {
                this.plugin.getPaperPetFollowService().stop(entity);
                entity.teleport(target);
                updateAnimationState(session, ActivePetSession.AnimationState.WALK);
                continue;
            }

            if (distance >= moveDistance) {
                boolean handledByPaperPathfinder = this.plugin.getPaperPetFollowService().moveToward(entity, player, target, distance);
                if (!handledByPaperPathfinder) {
                    Vector direction = target.toVector().subtract(entity.getLocation().toVector());
                    Vector velocity = direction.normalize().multiply(Math.min(0.45D, Math.max(0.18D, distance * 0.12D)));
                    velocity.setY(Math.max(-0.08D, Math.min(0.18D, target.getY() - entity.getLocation().getY())));
                    entity.setVelocity(velocity);
                }
                updateAnimationState(session, ActivePetSession.AnimationState.WALK);
            } else {
                this.plugin.getPaperPetFollowService().stop(entity);
                entity.setVelocity(new Vector(0.0D, entity.getVelocity().getY() * 0.25D, 0.0D));
                updateAnimationState(session, ActivePetSession.AnimationState.IDLE);
            }
        }
    }

    private Location resolveSpawnLocation(Player player, PetDefinition definition) {
        return resolveFollowLocation(player, definition, this.plugin.getConfig().getDouble("follow.vertical-offset", 0.2D));
    }

    private Location resolveFollowLocation(Player player, PetDefinition definition, double verticalOffset) {
        Location base = player.getLocation().clone();
        Vector backward = base.getDirection().normalize().multiply(-definition.getFollowOffset().getZ());
        Vector sideways = base.getDirection().clone().crossProduct(new Vector(0.0D, 1.0D, 0.0D)).normalize().multiply(definition.getFollowOffset().getX());
        base.add(backward);
        base.add(sideways);
        base.add(0.0D, definition.getFollowOffset().getY() + verticalOffset, 0.0D);
        return base;
    }

    private void preparePetEntity(Player owner, LivingEntity entity, PetDefinition definition) {
        entity.setCustomNameVisible(true);
        entity.setCustomName(definition.getDisplayName());
        entity.setInvulnerable(true);
        entity.setSilent(true);
        entity.setGlowing(definition.isGlowing());
        entity.setRemoveWhenFarAway(false);
        entity.setCollidable(false);

        if (entity instanceof Creature) {
            ((Creature) entity).setAI(true);
            ((Creature) entity).setTarget(null);
        }
        if (entity instanceof Tameable) {
            ((Tameable) entity).setOwner(owner);
        }
        if (definition.isBaby() && entity instanceof Ageable) {
            ((Ageable) entity).setBaby();
        }
        this.vanillaPetAppearanceService.apply(entity, definition);
        if (entity.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
            entity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(definition.getMovementSpeed());
        }
    }

    private void updateAnimationState(ActivePetSession session, ActivePetSession.AnimationState targetState) {
        if (session == null || session.getFmmHandle() == null || targetState == null) {
            return;
        }
        if (session.getAnimationState() == targetState) {
            return;
        }

        String animationName = resolveAnimationName(targetState);
        boolean played = false;
        if (animationName != null && this.fmmBridgeService.hasAnimation(session.getFmmHandle(), animationName)) {
            this.fmmBridgeService.stopCurrentAnimations(session.getFmmHandle());
            played = this.fmmBridgeService.playAnimation(
                session.getFmmHandle(),
                animationName,
                targetState == ActivePetSession.AnimationState.IDLE || targetState == ActivePetSession.AnimationState.WALK
            );
        } else if (targetState == ActivePetSession.AnimationState.SPAWN) {
            if (this.fmmBridgeService.hasAnimation(session.getFmmHandle(), "idle")) {
                this.fmmBridgeService.stopCurrentAnimations(session.getFmmHandle());
                played = this.fmmBridgeService.playAnimation(session.getFmmHandle(), "idle", true);
                targetState = ActivePetSession.AnimationState.IDLE;
            }
        }

        if (played) {
            session.setAnimationState(targetState);
        }
    }

    private String resolveAnimationName(ActivePetSession.AnimationState state) {
        switch (state) {
            case SPAWN:
                return "spawn";
            case IDLE:
                return "idle";
            case WALK:
                return "walk";
            default:
                return null;
        }
    }

    private static final class PendingSpawn {
        private final Location location;
        private final EntityType entityType;

        private PendingSpawn(Location location, EntityType entityType) {
            this.location = location.clone();
            this.entityType = entityType;
        }

        public Location getLocation() {
            return this.location;
        }

        public EntityType getEntityType() {
            return this.entityType;
        }
    }
}

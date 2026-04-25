package net.enelson.soppets.service;

import com.destroystokyo.paper.entity.Pathfinder;
import net.enelson.soppets.SopPetsPlugin;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public final class PaperPetFollowService {
    private final SopPetsPlugin plugin;

    public PaperPetFollowService(SopPetsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean moveToward(LivingEntity entity, Player owner, Location target, double distance) {
        if (!(entity instanceof Mob)) {
            return false;
        }

        Mob mob = (Mob) entity;
        mob.setTarget(null);
        mob.setAware(true);
        mob.lookAt(owner);

        Pathfinder pathfinder = mob.getPathfinder();
        if (distance <= this.plugin.getConfig().getDouble("follow.move-distance", 3.0D) * 0.75D) {
            pathfinder.stopPathfinding();
            return true;
        }

        double speed = distance >= 8.0D ? 1.55D : 1.2D;
        return pathfinder.moveTo(target, speed);
    }

    public void stop(LivingEntity entity) {
        if (!(entity instanceof Mob)) {
            return;
        }
        ((Mob) entity).getPathfinder().stopPathfinding();
    }
}

package net.enelson.soppets.listener;

import net.enelson.soppets.SopPetsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public final class PetSpawnBypassListener implements Listener {
    private final SopPetsPlugin plugin;

    public PetSpawnBypassListener(SopPetsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!event.isCancelled()) {
            return;
        }
        if (!this.plugin.getPetSessionService().matchesPendingSpawn(event.getEntity(), event.getLocation())) {
            return;
        }
        event.setCancelled(false);
    }
}

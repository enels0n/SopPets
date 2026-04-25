package net.enelson.soppets.listener;

import net.enelson.soppets.SopPetsPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public final class PetProtectionListener implements Listener {
    private final SopPetsPlugin plugin;

    public PetProtectionListener(SopPetsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPetDamage(EntityDamageEvent event) {
        if (this.plugin.getPetSessionService().isPetEntity(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPetDamageByEntity(EntityDamageByEntityEvent event) {
        if (this.plugin.getPetSessionService().isPetEntity(event.getDamager()) || this.plugin.getPetSessionService().isPetEntity(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        Entity entity = event.getEntity();
        if (this.plugin.getPetSessionService().isPetEntity(entity)) {
            event.setCancelled(true);
        }
    }
}

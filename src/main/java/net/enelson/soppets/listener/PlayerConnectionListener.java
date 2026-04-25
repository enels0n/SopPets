package net.enelson.soppets.listener;

import net.enelson.soppets.SopPetsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerConnectionListener implements Listener {
    private final SopPetsPlugin plugin;

    public PlayerConnectionListener(SopPetsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final String activePetId = this.plugin.getPlayerPetStorage().getActivePet(player.getUniqueId());
        if (activePetId == null || activePetId.isEmpty()) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getPetSessionService().summonPet(player, activePetId);
            }
        }, 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.plugin.getPetSessionService().despawnPet(event.getPlayer().getUniqueId());
    }
}

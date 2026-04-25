package net.enelson.soppets.listener;

import net.enelson.soppets.SopPetsPlugin;
import net.enelson.soppets.ui.PetsMenuHolder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public final class PetMenuListener implements Listener {
    private final SopPetsPlugin plugin;

    public PetMenuListener(SopPetsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        if (!(event.getInventory().getHolder() instanceof PetsMenuHolder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        PetsMenuHolder holder = (PetsMenuHolder) event.getInventory().getHolder();

        if (event.getRawSlot() == event.getInventory().getSize() - 5) {
            this.plugin.getPetSessionService().despawnPet(player.getUniqueId());
            this.plugin.getPlayerPetStorage().setActivePet(player.getUniqueId(), null);
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Pet hidden.");
            return;
        }

        String petId = holder.getPetId(event.getRawSlot());
        if (petId == null) {
            return;
        }

        String error = this.plugin.getPetSessionService().summonPet(player, petId);
        if (error != null) {
            player.sendMessage(error);
            return;
        }
        player.closeInventory();
        player.sendMessage(ChatColor.GREEN + "Pet summoned.");
    }
}

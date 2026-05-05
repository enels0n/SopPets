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

        if (event.getRawSlot() == PetsMenuHolder.HIDE_BUTTON_SLOT) {
            this.plugin.getPetSessionService().despawnPet(player.getUniqueId());
            this.plugin.getPlayerPetStorage().setActivePet(player.getUniqueId(), null);
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Pet hidden.");
            return;
        }

        if (event.getRawSlot() == PetsMenuHolder.FILTER_BUTTON_SLOT) {
            boolean enabled = !holder.isOwnedOnlyFilterEnabled();
            this.plugin.getPlayerPetStorage().setOwnedOnlyFilterEnabled(player.getUniqueId(), enabled);
            player.openInventory(this.plugin.getPetMenuService().createMainMenu(player, 0));
            player.sendMessage(
                enabled
                    ? ChatColor.GREEN + "Pet menu now shows only pets you own."
                    : ChatColor.YELLOW + "Pet menu now shows all pets."
            );
            return;
        }

        if (event.getRawSlot() == PetsMenuHolder.PREVIOUS_PAGE_SLOT && holder.getCurrentPage() > 0) {
            player.openInventory(this.plugin.getPetMenuService().createMainMenu(player, holder.getCurrentPage() - 1));
            return;
        }

        if (event.getRawSlot() == PetsMenuHolder.NEXT_PAGE_SLOT && holder.getCurrentPage() + 1 < holder.getTotalPages()) {
            player.openInventory(this.plugin.getPetMenuService().createMainMenu(player, holder.getCurrentPage() + 1));
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

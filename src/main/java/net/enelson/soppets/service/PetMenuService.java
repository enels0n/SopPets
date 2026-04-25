package net.enelson.soppets.service;

import java.util.Collection;
import java.util.Collections;
import net.enelson.soppets.SopPetsPlugin;
import net.enelson.soppets.model.PetDefinition;
import net.enelson.soppets.ui.PetsMenuHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class PetMenuService {
    private final SopPetsPlugin plugin;

    public PetMenuService(SopPetsPlugin plugin) {
        this.plugin = plugin;
    }

    public Inventory createMainMenu(Player player) {
        Collection<PetDefinition> pets = this.plugin.getPetDefinitionService().getPets();
        int size = 27;
        while (size < pets.size() + 9) {
            size += 9;
        }

        PetsMenuHolder holder = new PetsMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, Math.min(54, size), ChatColor.DARK_AQUA + "Pets");
        String activePetId = this.plugin.getPlayerPetStorage().getActivePet(player.getUniqueId());

        int slot = 10;
        for (PetDefinition definition : pets) {
            if (slot >= inventory.getSize() - 9) {
                break;
            }
            if (slot % 9 == 8) {
                slot += 2;
            }
            holder.bindPet(slot, definition.getId());
            inventory.setItem(slot, createPetItem(definition, definition.getId().equalsIgnoreCase(activePetId)));
            slot++;
        }

        inventory.setItem(inventory.getSize() - 5, createHideItem());
        return inventory;
    }

    private ItemStack createPetItem(PetDefinition definition, boolean active) {
        ItemStack item = new ItemStack(resolveIcon(definition.getEntityType()));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(definition.getDisplayName());
        meta.setLore(java.util.Arrays.asList(
            "",
            ChatColor.GRAY + "ID: " + ChatColor.WHITE + definition.getId(),
            ChatColor.GRAY + "Engine: " + ChatColor.WHITE + (definition.isUseFmm() ? "FreeMinecraftModels" : "Vanilla"),
            active ? ChatColor.GREEN + "Currently active" : ChatColor.YELLOW + "Click to summon"
        ));
        if (active) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createHideItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Hide pet");
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "Click to remove your active pet"));
        item.setItemMeta(meta);
        return item;
    }

    private Material resolveIcon(EntityType type) {
        try {
            return Material.valueOf(type.name() + "_SPAWN_EGG");
        } catch (IllegalArgumentException exception) {
            return Material.BONE;
        }
    }
}

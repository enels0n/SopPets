package net.enelson.soppets.service;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
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
    private static final int[] PET_SLOTS = new int[] {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };

    private final SopPetsPlugin plugin;

    public PetMenuService(SopPetsPlugin plugin) {
        this.plugin = plugin;
    }

    public Inventory createMainMenu(Player player) {
        return createMainMenu(player, 0);
    }

    public Inventory createMainMenu(Player player, int requestedPage) {
        Collection<PetDefinition> pets = this.plugin.getPetDefinitionService().getPets();
        boolean ownedOnly = this.plugin.getPlayerPetStorage().isOwnedOnlyFilterEnabled(player.getUniqueId());
        List<PetDefinition> visiblePets = new ArrayList<PetDefinition>();
        for (PetDefinition definition : pets) {
            if (ownedOnly && !this.plugin.getPetSessionService().hasAccess(player, definition)) {
                continue;
            }
            visiblePets.add(definition);
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) visiblePets.size() / (double) PET_SLOTS.length));
        int currentPage = Math.max(0, Math.min(requestedPage, totalPages - 1));
        PetsMenuHolder holder = new PetsMenuHolder(ownedOnly, currentPage, totalPages);
        Inventory inventory = Bukkit.createInventory(holder, 54, ChatColor.DARK_AQUA + "Pets");
        String activePetId = this.plugin.getPlayerPetStorage().getActivePet(player.getUniqueId());

        int startIndex = currentPage * PET_SLOTS.length;
        int endIndex = Math.min(startIndex + PET_SLOTS.length, visiblePets.size());
        for (int index = startIndex; index < endIndex; index++) {
            PetDefinition definition = visiblePets.get(index);
            int slot = PET_SLOTS[index - startIndex];
            inventory.setItem(
                slot,
                createPetItem(
                    player,
                    definition,
                    definition.getId().equalsIgnoreCase(activePetId),
                    this.plugin.getPetSessionService().hasAccess(player, definition)
                )
            );
            holder.bindPet(slot, definition.getId());
        }

        inventory.setItem(PetsMenuHolder.HIDE_BUTTON_SLOT, createHideItem());
        inventory.setItem(PetsMenuHolder.FILTER_BUTTON_SLOT, createFilterItem(ownedOnly));
        inventory.setItem(PetsMenuHolder.PAGE_INFO_SLOT, createPageInfoItem(currentPage, totalPages, visiblePets.size()));
        if (currentPage > 0) {
            inventory.setItem(PetsMenuHolder.PREVIOUS_PAGE_SLOT, createPreviousPageItem(currentPage));
        }
        if (currentPage + 1 < totalPages) {
            inventory.setItem(PetsMenuHolder.NEXT_PAGE_SLOT, createNextPageItem(currentPage, totalPages));
        }
        return inventory;
    }

    private ItemStack createPetItem(Player player, PetDefinition definition, boolean active, boolean unlocked) {
        PetDefinition effectiveDefinition = this.plugin.getPetSessionService().resolveEffectiveDefinition(player, definition);
        ItemStack item = new ItemStack(resolveIcon(effectiveDefinition));
        ItemMeta meta = item.getItemMeta();
        if (effectiveDefinition.getIconCustomModelData() > 0) {
            meta.setCustomModelData(effectiveDefinition.getIconCustomModelData());
        }
        meta.setDisplayName(applyIconPlaceholders(player, effectiveDefinition.getIconDisplayName(), definition, active, unlocked));
        meta.setLore(buildPetLore(player, effectiveDefinition, definition, active, unlocked));
        if (active) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private List<String> buildPetLore(Player player, PetDefinition effectiveDefinition, PetDefinition baseDefinition, boolean active, boolean unlocked) {
        List<String> configuredLore = effectiveDefinition.getIconLore();
        if (configuredLore.isEmpty()) {
            List<String> fallbackLore = new java.util.ArrayList<String>();
            fallbackLore.add("");
            fallbackLore.add(ChatColor.GRAY + "ID: " + ChatColor.WHITE + baseDefinition.getId());
            fallbackLore.add(ChatColor.GRAY + "Engine: " + ChatColor.WHITE + (effectiveDefinition.isUseFmm() ? "FreeMinecraftModels" : "Vanilla"));
            fallbackLore.add(ChatColor.GRAY + "Permission: " + ChatColor.WHITE + this.plugin.getPetSessionService().getAccessPermission(baseDefinition));
            fallbackLore.add(ChatColor.GRAY + "Level: " + ChatColor.WHITE + this.plugin.getPetSessionService().getPetLevel(player, baseDefinition));
            fallbackLore.add("");
            fallbackLore.add(resolveStatusLine(active, unlocked));
            if (player.hasPermission("soppets.admin")) {
                fallbackLore.add("");
                fallbackLore.add(ChatColor.DARK_GRAY + "Admin preview enabled");
            }
            return fallbackLore;
        }

        List<String> renderedLore = new java.util.ArrayList<String>();
        for (String line : configuredLore) {
            renderedLore.add(applyIconPlaceholders(player, line, baseDefinition, active, unlocked));
        }
        return renderedLore;
    }

    private ItemStack createHideItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Hide pet");
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "Click to remove your active pet"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createFilterItem(boolean ownedOnly) {
        ItemStack item = new ItemStack(ownedOnly ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Owned filter");
        meta.setLore(java.util.Arrays.asList(
            ChatColor.GRAY + "Current mode: " + (ownedOnly ? ChatColor.GREEN + "Only owned" : ChatColor.YELLOW + "Show all"),
            "",
            ChatColor.YELLOW + "Click to toggle"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPageInfoItem(int currentPage, int totalPages, int totalPets) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Page " + (currentPage + 1) + ChatColor.GRAY + "/" + ChatColor.AQUA + totalPages);
        meta.setLore(java.util.Arrays.asList(
            ChatColor.GRAY + "Visible pets: " + ChatColor.WHITE + totalPets,
            ChatColor.DARK_GRAY + "Use arrows to browse"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPreviousPageItem(int currentPage) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Previous page");
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "Go to page " + currentPage));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createNextPageItem(int currentPage, int totalPages) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Next page");
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "Go to page " + Math.min(totalPages, currentPage + 2)));
        item.setItemMeta(meta);
        return item;
    }

    private Material resolveIcon(PetDefinition definition) {
        String configuredMaterial = definition.getIconMaterialName();
        if (configuredMaterial != null && !configuredMaterial.trim().isEmpty()) {
            try {
                return Material.valueOf(configuredMaterial.trim().toUpperCase());
            } catch (IllegalArgumentException exception) {
                this.plugin.getLogger().warning(
                    "Invalid icon material '" + configuredMaterial + "' for pet '" + definition.getId() + "'. Falling back to spawn egg."
                );
            }
        }
        EntityType type = definition.getEntityType();
        try {
            return Material.valueOf(type.name() + "_SPAWN_EGG");
        } catch (IllegalArgumentException exception) {
            return Material.BONE;
        }
    }

    private String applyIconPlaceholders(Player player, String input, PetDefinition definition, boolean active, boolean unlocked) {
        if (input == null) {
            return null;
        }
        int level = this.plugin.getPetSessionService().getPetLevel(player, definition);
        return input
            .replace("%pet_id%", definition.getId())
            .replace("%pet_name%", definition.getDisplayName())
            .replace("%pet_engine%", definition.isUseFmm() ? "FreeMinecraftModels" : "Vanilla")
            .replace("%pet_permission%", this.plugin.getPetSessionService().getAccessPermission(definition))
            .replace("%pet_level%", Integer.toString(level))
            .replace("%pet_level_permission%", this.plugin.getPetSessionService().getLevelPermission(definition, level))
            .replace("%pet_status%", ChatColor.stripColor(resolveStatusLine(active, unlocked)))
            .replace("%pet_status_colored%", resolveStatusLine(active, unlocked))
            .replace("%pet_owned%", unlocked ? "yes" : "no")
            .replace("%pet_active%", active ? "yes" : "no");
    }

    private String resolveStatusLine(boolean active, boolean unlocked) {
        if (!unlocked) {
            return ChatColor.RED + "Locked";
        }
        if (active) {
            return ChatColor.GREEN + "Currently active";
        }
        return ChatColor.YELLOW + "Click to summon";
    }
}

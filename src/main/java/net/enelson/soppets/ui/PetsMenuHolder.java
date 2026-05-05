package net.enelson.soppets.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class PetsMenuHolder implements InventoryHolder {
    public static final int PREVIOUS_PAGE_SLOT = 45;
    public static final int FILTER_BUTTON_SLOT = 48;
    public static final int PAGE_INFO_SLOT = 49;
    public static final int HIDE_BUTTON_SLOT = 50;
    public static final int NEXT_PAGE_SLOT = 53;

    private final Map<Integer, String> petIdsBySlot = new LinkedHashMap<Integer, String>();
    private final boolean ownedOnlyFilterEnabled;
    private final int currentPage;
    private final int totalPages;

    public PetsMenuHolder(boolean ownedOnlyFilterEnabled, int currentPage, int totalPages) {
        this.ownedOnlyFilterEnabled = ownedOnlyFilterEnabled;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
    }

    public void bindPet(int slot, String petId) {
        this.petIdsBySlot.put(slot, petId);
    }

    public String getPetId(int slot) {
        return this.petIdsBySlot.get(slot);
    }

    public boolean isOwnedOnlyFilterEnabled() {
        return this.ownedOnlyFilterEnabled;
    }

    public int getCurrentPage() {
        return this.currentPage;
    }

    public int getTotalPages() {
        return this.totalPages;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}

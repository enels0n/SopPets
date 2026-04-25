package net.enelson.soppets.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class PetsMenuHolder implements InventoryHolder {
    private final Map<Integer, String> petIdsBySlot = new LinkedHashMap<Integer, String>();

    public void bindPet(int slot, String petId) {
        this.petIdsBySlot.put(slot, petId);
    }

    public String getPetId(int slot) {
        return this.petIdsBySlot.get(slot);
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}

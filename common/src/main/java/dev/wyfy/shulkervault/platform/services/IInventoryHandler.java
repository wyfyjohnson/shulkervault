package dev.wyfy.shulkervault.platform.services;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Platform-agnostic interface for handling oversized item inventories.
 * Implementations should support stacks larger than the default max stack size.
 */
public interface IInventoryHandler {

    /**
     * Gets the number of slots in this inventory.
     */
    int getSlots();

    /**
     * Gets the ItemStack in the given slot.
     */
    ItemStack getStackInSlot(int slot);

    /**
     * Sets the ItemStack in the given slot.
     */
    void setStackInSlot(int slot, ItemStack stack);

    /**
     * Inserts an ItemStack into the given slot and returns the remainder.
     */
    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

    /**
     * Extracts items from the given slot.
     */
    ItemStack extractItem(int slot, int amount, boolean simulate);

    /**
     * Gets the maximum stack size for the given slot.
     */
    int getSlotLimit(int slot);

    /**
     * Checks if the given ItemStack can be inserted into the slot.
     */
    boolean isItemValid(int slot, ItemStack stack);

    /**
     * Serializes the inventory to NBT.
     */
    CompoundTag serializeNBT(HolderLookup.Provider registries);

    /**
     * Deserializes the inventory from NBT.
     */
    void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt);

    /**
     * Marks the inventory as changed.
     */
    void setChanged(Runnable onChange);
}

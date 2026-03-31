package dev.wyfy.shulkervault.platform;

import dev.wyfy.shulkervault.platform.services.IInventoryHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * NeoForge implementation of IInventoryHandler using ItemStackHandler.
 */
public class NeoForgeInventoryHandler implements IInventoryHandler {

    private final ItemStackHandler handler;
    private Runnable onChange;

    public NeoForgeInventoryHandler(int size, int maxStackSize) {
        this.handler = new ItemStackHandler(size) {
            @Override
            protected void onContentsChanged(int slot) {
                if (onChange != null) {
                    onChange.run();
                }
            }

            @Override
            public int getSlotLimit(int slot) {
                return maxStackSize;
            }
        };
    }

    @Override
    public int getSlots() {
        return handler.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return handler.getStackInSlot(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        handler.setStackInSlot(slot, stack);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return handler.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return handler.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return handler.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return handler.isItemValid(slot, stack);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        return handler.serializeNBT(registries);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
        handler.deserializeNBT(registries, nbt);
    }

    @Override
    public void setChanged(Runnable onChange) {
        this.onChange = onChange;
    }

    /**
     * Gets the underlying ItemStackHandler for NeoForge-specific operations.
     */
    public ItemStackHandler getHandler() {
        return handler;
    }
}

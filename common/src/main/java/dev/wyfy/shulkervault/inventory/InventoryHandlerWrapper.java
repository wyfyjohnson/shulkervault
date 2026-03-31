package dev.wyfy.shulkervault.inventory;

import dev.wyfy.shulkervault.platform.services.IInventoryHandler;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Wraps an IInventoryHandler to provide a Container interface.
 */
public class InventoryHandlerWrapper implements Container {

    private final IInventoryHandler handler;

    public InventoryHandlerWrapper(IInventoryHandler handler) {
        this.handler = handler;
    }

    @Override
    public int getContainerSize() {
        return handler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return handler.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return handler.extractItem(slot, amount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = handler.getStackInSlot(slot);
        handler.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        handler.setStackInSlot(slot, stack);
    }

    @Override
    public void setChanged() {
        // Changes are handled by the IInventoryHandler
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < handler.getSlots(); i++) {
            handler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public int getMaxStackSize() {
        // Return the slot limit for slot 0 (they should all be the same)
        return handler.getSlotLimit(0);
    }

    public IInventoryHandler getHandler() {
        return handler;
    }
}

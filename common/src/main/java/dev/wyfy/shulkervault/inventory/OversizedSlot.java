package dev.wyfy.shulkervault.inventory;

import dev.wyfy.shulkervault.platform.services.IInventoryHandler;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A slot that works with IInventoryHandler and supports oversized stacks.
 */
public class OversizedSlot extends Slot {

    private final IInventoryHandler handler;

    public OversizedSlot(IInventoryHandler handler, int slot, int x, int y) {
        super(new InventoryHandlerWrapper(handler), slot, x, y);
        this.handler = handler;
    }

    @Override
    public int getMaxStackSize() {
        return handler.getSlotLimit(this.getContainerSlot());
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return handler.getSlotLimit(this.getContainerSlot());
    }
}

package dev.wyfy.shulkervault.platform;

import dev.wyfy.shulkervault.platform.services.IInventoryHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;

/**
 * Fabric implementation of IInventoryHandler using a custom inventory system.
 */
public class FabricInventoryHandler implements IInventoryHandler {

    private final NonNullList<ItemStack> stacks;
    private final int maxStackSize;
    private Runnable onChange;

    public FabricInventoryHandler(int size, int maxStackSize) {
        this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        this.maxStackSize = maxStackSize;
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return stacks.get(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        stacks.set(slot, stack);
        onContentsChanged();
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack existing = stacks.get(slot);

        int limit = getSlotLimit(slot);

        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(stack, existing)) {
                return stack;
            }

            limit -= existing.getCount();
        }

        if (limit <= 0) {
            return stack;
        }

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty()) {
                stacks.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack.copy());
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged();
        }

        return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) {
            return ItemStack.EMPTY;
        }

        ItemStack existing = stacks.get(slot);

        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int toExtract = Math.min(amount, existing.getCount());

        ItemStack result = existing.copyWithCount(toExtract);

        if (!simulate) {
            existing.shrink(toExtract);
            if (existing.isEmpty()) {
                stacks.set(slot, ItemStack.EMPTY);
            }
            onContentsChanged();
        }

        return result;
    }

    @Override
    public int getSlotLimit(int slot) {
        ItemStack stack = stacks.get(slot);
        if (!stack.isEmpty()) {
            return Math.min(64, stack.getMaxStackSize());
        }
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        CompoundTag nbt = new CompoundTag();
        ContainerHelper.saveAllItems(nbt, stacks, registries);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
        stacks.clear();
        ContainerHelper.loadAllItems(nbt, stacks, registries);
    }

    @Override
    public void setChanged(Runnable onChange) {
        this.onChange = onChange;
    }

    private void onContentsChanged() {
        if (onChange != null) {
            onChange.run();
        }
    }

    /**
     * Gets the underlying NonNullList for Fabric-specific operations.
     */
    public NonNullList<ItemStack> getStacks() {
        return stacks;
    }
}

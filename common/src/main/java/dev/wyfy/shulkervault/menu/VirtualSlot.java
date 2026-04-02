package dev.wyfy.shulkervault.menu;

import dev.wyfy.shulkervault.platform.services.IInventoryHandler;
import net.minecraft.world.item.ItemStack;

/**
 * Utility class for working with groups of backing slots that represent a single "virtual" slot.
 * Each virtual slot is backed by 4 physical slots to allow stacks larger than the normal max.
 */
public class VirtualSlot {

    private static final int BACKING_SLOTS_PER_VIRTUAL = 4;

    /**
     * Gets the summed ItemStack for a group of backing slots.
     * Returns an ItemStack with the item type from the first non-empty backing slot
     * and a count equal to the sum of all matching backing slots.
     *
     * @param handler The inventory handler
     * @param groupIndex The virtual slot group index (0-26 for a 3x9 grid)
     * @return ItemStack with summed count, or EMPTY if all backing slots are empty
     */
    public static ItemStack getSummedStack(IInventoryHandler handler, int groupIndex) {
        int firstBackingSlot = groupIndex * BACKING_SLOTS_PER_VIRTUAL;
        ItemStack firstNonEmpty = ItemStack.EMPTY;
        int totalCount = 0;

        for (int i = 0; i < BACKING_SLOTS_PER_VIRTUAL; i++) {
            ItemStack stack = handler.getStackInSlot(firstBackingSlot + i);
            if (!stack.isEmpty()) {
                if (firstNonEmpty.isEmpty()) {
                    firstNonEmpty = stack;
                    totalCount = stack.getCount();
                } else if (ItemStack.isSameItemSameComponents(firstNonEmpty, stack)) {
                    totalCount += stack.getCount();
                }
            }
        }

        if (firstNonEmpty.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = firstNonEmpty.copy();
        result.setCount(totalCount);
        return result;
    }

    /**
     * Gets the total count of items in a group of backing slots.
     *
     * @param handler The inventory handler
     * @param groupIndex The virtual slot group index (0-26 for a 3x9 grid)
     * @return Total count of all matching items in the group
     */
    public static int getSummedCount(IInventoryHandler handler, int groupIndex) {
        ItemStack summed = getSummedStack(handler, groupIndex);
        return summed.isEmpty() ? 0 : summed.getCount();
    }

    /**
     * Checks if a group of backing slots has any items.
     *
     * @param handler The inventory handler
     * @param groupIndex The virtual slot group index (0-26 for a 3x9 grid)
     * @return true if any backing slot in the group is non-empty
     */
    public static boolean hasItem(IInventoryHandler handler, int groupIndex) {
        int firstBackingSlot = groupIndex * BACKING_SLOTS_PER_VIRTUAL;
        for (int i = 0; i < BACKING_SLOTS_PER_VIRTUAL; i++) {
            if (!handler.getStackInSlot(firstBackingSlot + i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the first backing slot index for a given virtual slot group.
     *
     * @param groupIndex The virtual slot group index (0-26 for a 3x9 grid)
     * @return The index of the first backing slot in the group
     */
    public static int getFirstBackingSlot(int groupIndex) {
        return groupIndex * BACKING_SLOTS_PER_VIRTUAL;
    }

    /**
     * Gets the number of backing slots per virtual slot.
     *
     * @return The number of backing slots (always 4)
     */
    public static int getBackingSlotCount() {
        return BACKING_SLOTS_PER_VIRTUAL;
    }
}

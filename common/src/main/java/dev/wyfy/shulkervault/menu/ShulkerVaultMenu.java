package dev.wyfy.shulkervault.menu;

import dev.wyfy.shulkervault.block.entity.ShulkerVaultBlockEntity;
import dev.wyfy.shulkervault.platform.Services;
import dev.wyfy.shulkervault.platform.services.IInventoryHandler;
import dev.wyfy.shulkervault.registry.ModRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.SimpleContainer;

import java.util.ArrayList;
import java.util.List;

public class ShulkerVaultMenu extends AbstractContainerMenu {

    private static final int BACKING_SLOTS_PER_VIRTUAL = 4;
    private static final int VIRTUAL_SLOT_COUNT = 27;
    private static final int SLOT_COUNT = VIRTUAL_SLOT_COUNT * BACKING_SLOTS_PER_VIRTUAL; // 108 total backing slots
    private static final int PLAYER_INVENTORY_ROWS = 3;
    private static final int PLAYER_INVENTORY_COLS = 9;
    private static final int HOTBAR_SLOTS = 9;

    private final IInventoryHandler inventory;
    private final ShulkerVaultBlockEntity blockEntity;
    private final VaultLocation location;

    /**
     * Custom slot that wraps IInventoryHandler instead of Container.
     */
    private static class BackingSlot extends Slot {
        private final IInventoryHandler handler;
        private final int handlerSlot;

        public BackingSlot(IInventoryHandler handler, int slot, int x, int y) {
            super(new SimpleContainer(0), 0, x, y);
            this.handler = handler;
            this.handlerSlot = slot;
        }

        @Override
        public ItemStack getItem() {
            return handler.getStackInSlot(handlerSlot);
        }

        @Override
        public void set(ItemStack stack) {
            handler.setStackInSlot(handlerSlot, stack);
        }

        @Override
        public ItemStack remove(int amount) {
            return handler.extractItem(handlerSlot, amount, false);
        }

        @Override
        public int getMaxStackSize() {
            return handler.getSlotLimit(handlerSlot);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return handler.isItemValid(handlerSlot, stack);
        }

        @Override
        public void setChanged() {
            handler.setStackInSlot(handlerSlot, handler.getStackInSlot(handlerSlot));
        }
    }

    /**
     * Client-side constructor - reads location from network buffer
     */
    public ShulkerVaultMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, VaultLocation.read(buf));
    }

    /**
     * Constructor that accepts VaultLocation directly
     */
    public ShulkerVaultMenu(int containerId, Inventory playerInventory, VaultLocation location) {
        this(containerId, playerInventory, null, location, Services.PLATFORM.createInventoryHandler(SLOT_COUNT, 64));
    }

    /**
     * Server-side constructor for opening from a block entity
     */
    public ShulkerVaultMenu(int containerId, Inventory playerInventory, ShulkerVaultBlockEntity blockEntity, VaultLocation location) {
        this(containerId, playerInventory, blockEntity, location, null);
    }

    /**
     * Main constructor
     */
    private ShulkerVaultMenu(int containerId, Inventory playerInventory, ShulkerVaultBlockEntity blockEntity, VaultLocation location, IInventoryHandler clientInventory) {
        super(ModRegistry.SHULKER_VAULT_MENU_TYPE, containerId);

        this.location = location;
        this.blockEntity = blockEntity;

        dev.wyfy.shulkervault.Constants.LOG.info("[ShulkerVaultMenu] Constructor - blockEntity: {}, location: {}, player: {}",
            blockEntity != null ? "exists" : "null", location, playerInventory.player.getName().getString());

        if (blockEntity != null) {
            // Opening from a block entity
            this.inventory = blockEntity.getInventory();
            dev.wyfy.shulkervault.Constants.LOG.info("[ShulkerVaultMenu] Calling startOpen on block entity");
            blockEntity.startOpen(playerInventory.player);
        } else if (clientInventory != null) {
            // Client-side with dummy inventory
            this.inventory = clientInventory;
            dev.wyfy.shulkervault.Constants.LOG.info("[ShulkerVaultMenu] Using client-side dummy inventory");
        } else if (location instanceof VaultLocation.InItem inItem) {
            // Server-side opening from held item - load inventory from the item
            this.inventory = Services.PLATFORM.createInventoryHandler(SLOT_COUNT, 64);

            ItemStack stack = playerInventory.player.getItemInHand(inItem.hand());
            ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
            if (contents != null) {
                List<ItemStack> items = contents.stream().toList();
                for (int i = 0; i < Math.min(items.size(), SLOT_COUNT); i++) {
                    inventory.setStackInSlot(i, items.get(i));
                }
            }
            dev.wyfy.shulkervault.Constants.LOG.info("[ShulkerVaultMenu] Loaded inventory from held item in hand: {}", inItem.hand());
        } else {
            // Fallback - create empty inventory
            this.inventory = Services.PLATFORM.createInventoryHandler(SLOT_COUNT, 64);
            dev.wyfy.shulkervault.Constants.LOG.info("[ShulkerVaultMenu] Created fallback inventory");
        }

        // Shulker Vault backing slots: all 108 slots registered
        // First slot of each group (every 4th slot) is positioned at the visible grid location
        // Other 3 slots are positioned off-screen at -9999, -9999 for sync but no render
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int groupIndex = col + row * 9;
                int firstBackingSlot = groupIndex * BACKING_SLOTS_PER_VIRTUAL;

                // Add all 4 backing slots for this group
                for (int i = 0; i < BACKING_SLOTS_PER_VIRTUAL; i++) {
                    int backingSlot = firstBackingSlot + i;
                    if (i == 0) {
                        // First slot: visible at normal grid position
                        this.addSlot(new BackingSlot(this.inventory, backingSlot, 8 + col * 18, 18 + row * 18));
                    } else {
                        // Other slots: off-screen for sync only
                        this.addSlot(new BackingSlot(this.inventory, backingSlot, -9999, -9999));
                    }
                }
            }
        }

        // Player inventory (3x9 grid)
        for (int row = 0; row < PLAYER_INVENTORY_ROWS; ++row) {
            for (int col = 0; col < PLAYER_INVENTORY_COLS; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 86 + row * 18));
            }
        }

        // Player hotbar (1x9 row)
        for (int col = 0; col < HOTBAR_SLOTS; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 144));
        }
    }

    @Override
    public void clicked(int slotId, int dragType, net.minecraft.world.inventory.ClickType clickType, Player player) {
        // Check if this is a visible vault slot (indices 0, 4, 8, 12, ..., 104)
        if (slotId >= 0 && slotId < SLOT_COUNT && slotId % BACKING_SLOTS_PER_VIRTUAL == 0) {
            ItemStack carried = this.getCarried();

            // Only handle insertion when player has an item on cursor and it's a PICKUP click
            if (!carried.isEmpty() && clickType == net.minecraft.world.inventory.ClickType.PICKUP) {
                // Check if the group is empty or has compatible items
                ItemStack existingStack = ItemStack.EMPTY;
                for (int i = 0; i < BACKING_SLOTS_PER_VIRTUAL; i++) {
                    ItemStack backingStack = inventory.getStackInSlot(slotId + i);
                    if (!backingStack.isEmpty()) {
                        existingStack = backingStack;
                        break;
                    }
                }

                // Only proceed if group is empty or has same item type
                if (existingStack.isEmpty() || ItemStack.isSameItemSameComponents(existingStack, carried)) {
                    if (dragType == 0) {
                        // Left click: insert as much as possible across all backing slots
                        ItemStack remaining = carried.copy();
                        for (int i = 0; i < BACKING_SLOTS_PER_VIRTUAL; i++) {
                            if (remaining.isEmpty()) {
                                break;
                            }
                            remaining = inventory.insertItem(slotId + i, remaining, false);
                        }
                        this.setCarried(remaining.isEmpty() ? ItemStack.EMPTY : remaining);
                        return;
                    } else if (dragType == 1) {
                        // Right click: insert one item
                        ItemStack toInsert = carried.copy();
                        toInsert.setCount(1);

                        for (int i = 0; i < BACKING_SLOTS_PER_VIRTUAL; i++) {
                            ItemStack remaining = inventory.insertItem(slotId + i, toInsert, false);
                            if (remaining.isEmpty()) {
                                // Successfully inserted one item
                                carried.shrink(1);
                                this.setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
                                return;
                            }
                        }
                        // If we couldn't insert, fall through to vanilla behavior
                    }
                }
            }
        }

        // For all other cases (extraction, swapping, non-vault slots, etc.), use vanilla behavior
        super.clicked(slotId, dragType, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index < SLOT_COUNT) {
                // Moving from vault to player inventory
                // Extract up to the item's max stack size
                int maxExtract = Math.min(slotStack.getCount(), slotStack.getMaxStackSize());
                ItemStack extractStack = slotStack.copy();
                extractStack.setCount(maxExtract);

                if (!this.moveItemStackTo(extractStack, SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }

                // Remove the extracted amount from the slot
                slotStack.shrink(maxExtract - extractStack.getCount());
                slot.set(slotStack);
            } else {
                // Moving from player inventory to vault
                // Iterate through the 27 groups of 4 backing slots
                boolean moved = false;

                for (int groupIndex = 0; groupIndex < VIRTUAL_SLOT_COUNT; groupIndex++) {
                    int firstBackingSlot = groupIndex * BACKING_SLOTS_PER_VIRTUAL;

                    // Find the first non-empty backing slot to check compatibility
                    ItemStack existingStack = ItemStack.EMPTY;
                    for (int i = 0; i < BACKING_SLOTS_PER_VIRTUAL; i++) {
                        ItemStack checkStack = inventory.getStackInSlot(firstBackingSlot + i);
                        if (!checkStack.isEmpty()) {
                            existingStack = checkStack;
                            break;
                        }
                    }

                    // Check if this group is compatible (empty or same item)
                    if (existingStack.isEmpty() || ItemStack.isSameItemSameComponents(existingStack, slotStack)) {
                        // Try to insert into each backing slot sequentially
                        for (int i = 0; i < BACKING_SLOTS_PER_VIRTUAL; i++) {
                            if (slotStack.isEmpty()) {
                                break;
                            }

                            ItemStack remaining = inventory.insertItem(firstBackingSlot + i, slotStack, false);
                            int inserted = slotStack.getCount() - remaining.getCount();

                            if (inserted > 0) {
                                slotStack = remaining;
                                moved = true;
                            }
                        }

                        if (slotStack.isEmpty()) {
                            break;
                        }
                    }
                }

                if (!moved) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity == null || blockEntity.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        dev.wyfy.shulkervault.Constants.LOG.info("[ShulkerVaultMenu] removed - player: {}, location: {}",
            player.getName().getString(), location);
        super.removed(player);

        if (!player.level().isClientSide) {
            if (location instanceof VaultLocation.InBlock inBlock) {
                // Closing a block entity vault
                if (blockEntity != null) {
                    dev.wyfy.shulkervault.Constants.LOG.info("[ShulkerVaultMenu] Calling stopOpen on block entity");
                    blockEntity.stopOpen(player);
                }
            } else if (location instanceof VaultLocation.InItem inItem) {
                // Closing a held item vault - save inventory back to the item
                dev.wyfy.shulkervault.Constants.LOG.info("[ShulkerVaultMenu] Saving inventory back to held item in hand: {}", inItem.hand());

                ItemStack stack = player.getItemInHand(inItem.hand());
                if (!stack.isEmpty()) {
                    // Collect all 108 slots into a list
                    List<ItemStack> items = new ArrayList<>();
                    for (int i = 0; i < SLOT_COUNT; i++) {
                        items.add(inventory.getStackInSlot(i));
                    }

                    // Save to item using ItemContainerContents
                    ItemContainerContents contents = ItemContainerContents.fromItems(items);
                    stack.set(DataComponents.CONTAINER, contents);
                    dev.wyfy.shulkervault.Constants.LOG.info("[ShulkerVaultMenu] Saved {} items to held item", items.stream().filter(s -> !s.isEmpty()).count());
                }
            }
        }
    }

    public IInventoryHandler getInventory() {
        return inventory;
    }
}

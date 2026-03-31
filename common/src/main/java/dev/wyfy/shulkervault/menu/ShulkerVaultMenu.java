package dev.wyfy.shulkervault.menu;

import dev.wyfy.shulkervault.block.entity.ShulkerVaultBlockEntity;
import dev.wyfy.shulkervault.inventory.OversizedSlot;
import dev.wyfy.shulkervault.platform.Services;
import dev.wyfy.shulkervault.platform.services.IInventoryHandler;
import dev.wyfy.shulkervault.registry.ModRegistry;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ShulkerVaultMenu extends AbstractContainerMenu {

    private static final int VAULT_SLOTS = 27;
    private static final int PLAYER_INVENTORY_ROWS = 3;
    private static final int PLAYER_INVENTORY_COLS = 9;
    private static final int HOTBAR_SLOTS = 9;
    private static final int MAX_STACK_SIZE = 576;

    private final IInventoryHandler inventory;
    private final ShulkerVaultBlockEntity blockEntity;

    public ShulkerVaultMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, null);
    }

    public ShulkerVaultMenu(int containerId, Inventory playerInventory, ShulkerVaultBlockEntity blockEntity) {
        super(ModRegistry.SHULKER_VAULT_MENU_TYPE, containerId);

        dev.wyfy.shulkervault.Constants.LOG.info("[ShulkerVaultMenu] Constructor - blockEntity: {}, player: {}",
            blockEntity != null ? "exists" : "null", playerInventory.player.getName().getString());

        if (blockEntity != null) {
            this.blockEntity = blockEntity;
            this.inventory = blockEntity.getInventory();
            dev.wyfy.shulkervault.Constants.LOG.info("[ShulkerVaultMenu] Calling startOpen on block entity");
            blockEntity.startOpen(playerInventory.player);
        } else {
            // Client-side dummy inventory
            dev.wyfy.shulkervault.Constants.LOG.info("[ShulkerVaultMenu] Creating dummy inventory (client side)");
            this.blockEntity = null;
            this.inventory = Services.PLATFORM.createInventoryHandler(VAULT_SLOTS, MAX_STACK_SIZE);
        }

        // Shulker Vault slots (3x9 grid)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new OversizedSlot(inventory, col + row * 9, 8 + col * 18, 18 + row * 18));
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
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index < VAULT_SLOTS) {
                // Moving from vault to player inventory
                if (!this.moveItemStackTo(slotStack, VAULT_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player inventory to vault
                if (!this.moveItemStackTo(slotStack, 0, VAULT_SLOTS, false)) {
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
        dev.wyfy.shulkervault.Constants.LOG.info("[ShulkerVaultMenu] removed - player: {}, blockEntity: {}",
            player.getName().getString(), blockEntity != null ? "exists" : "null");
        super.removed(player);
        if (blockEntity != null) {
            dev.wyfy.shulkervault.Constants.LOG.info("[ShulkerVaultMenu] Calling stopOpen on block entity");
            blockEntity.stopOpen(player);
        }
    }

    public IInventoryHandler getInventory() {
        return inventory;
    }
}

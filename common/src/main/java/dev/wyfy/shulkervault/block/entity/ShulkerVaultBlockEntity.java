package dev.wyfy.shulkervault.block.entity;

import dev.wyfy.shulkervault.Constants;
import dev.wyfy.shulkervault.menu.ShulkerVaultMenu;
import dev.wyfy.shulkervault.menu.VaultLocation;
import dev.wyfy.shulkervault.platform.Services;
import dev.wyfy.shulkervault.platform.services.IInventoryHandler;
import dev.wyfy.shulkervault.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

public class ShulkerVaultBlockEntity extends BlockEntity implements MenuProvider {

    public enum AnimationStatus {
        CLOSED,
        OPENING,
        CLOSING
    }

    private static final int SLOT_COUNT = 108;

    private final IInventoryHandler inventory;

    private int openCount = 0;
    private float progress = 0.0f;
    private float progressOld = 0.0f;
    private AnimationStatus animationStatus = AnimationStatus.CLOSED;

    public ShulkerVaultBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModRegistry.SHULKER_VAULT_BLOCK_ENTITY_TYPE, pos, blockState);
        this.inventory = Services.PLATFORM.createInventoryHandler(SLOT_COUNT, 64);
        this.inventory.setChanged(this::setChanged);
    }

    public void tick() {
        progressOld = progress;

        if (animationStatus == AnimationStatus.OPENING) {
            progress += 0.1f;
            if (progress > 1.0f) {
                progress = 1.0f;
            }
        } else if (animationStatus == AnimationStatus.CLOSING) {
            progress -= 0.1f;
            if (progress < 0.0f) {
                progress = 0.0f;
            }
        }
    }

    public float getProgress(float partialTick) {
        return Mth.lerp(partialTick, progressOld, progress);
    }

    public AABB getLidBoundingBox() {
    Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
    float extend = progress * 0.5F;
    return switch (facing) {
        case UP    -> new AABB(0, 0.5 + extend,  0,       1,       1.0 + extend, 1);
        case DOWN  -> new AABB(0, -extend,        0,       1,       0.5 - extend, 1);
        case NORTH -> new AABB(0, 0,             -extend,  1, 1.0,  0.5 - extend);
        case SOUTH -> new AABB(0, 0,              0.5 + extend, 1, 1.0, 1.0 + extend);
        case WEST  -> new AABB(-extend,  0, 0,   0.5 - extend, 1.0, 1);
        case EAST  -> new AABB(0.5 + extend, 0,  0,  1.0 + extend, 1.0, 1);
    };
}

    public void startOpen(Player player) {
        Constants.LOG.info("[ShulkerVaultBlockEntity] startOpen called - player: {}, isSpectator: {}, openCount: {}",
            player.getName().getString(), player.isSpectator(), openCount);
        if (!player.isSpectator()) {
            if (openCount < 0) {
                openCount = 0;
            }
            ++openCount;

            if (openCount == 1) {
                Constants.LOG.info("[ShulkerVaultBlockEntity] Opening vault animation - setting status to OPENING");
                animationStatus = AnimationStatus.OPENING;
                if (level != null) {
                    level.blockEvent(worldPosition, getBlockState().getBlock(), 1, 1);
                }
            }
        }
    }

    public void stopOpen(Player player) {
        Constants.LOG.info("[ShulkerVaultBlockEntity] stopOpen called - player: {}, isSpectator: {}, openCount: {}",
            player.getName().getString(), player.isSpectator(), openCount);
        if (!player.isSpectator()) {
            --openCount;

            if (openCount <= 0) {
                Constants.LOG.info("[ShulkerVaultBlockEntity] Closing vault animation - setting status to CLOSING");
                openCount = 0;
                animationStatus = AnimationStatus.CLOSING;
                if (level != null) {
                    level.blockEvent(worldPosition, getBlockState().getBlock(), 1, 0);
                }
            }
        }
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            if (type > 0) {
                animationStatus = AnimationStatus.OPENING;
            } else if (type == 0) {
                animationStatus = AnimationStatus.CLOSING;
            }
            return true;
        }
        return super.triggerEvent(id, type);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
    }

    /**
     * Gets the inventory handler for this block entity.
     */
    public IInventoryHandler getInventory() {
        return inventory;
    }

    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    // MenuProvider implementation
    @Override
    public Component getDisplayName() {
        return Component.translatable("container." + Constants.MOD_ID + ".shulker_vault");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ShulkerVaultMenu(containerId, playerInventory, this, new VaultLocation.InBlock(this.worldPosition));
    }

    /**
     * Saves the inventory contents to an ItemStack's NBT for shulker-box-like behavior.
     */
    public void saveToItem(ItemStack stack, HolderLookup.Provider registries) {
        if (!isEmpty()) {
            stack.set(net.minecraft.core.component.DataComponents.CONTAINER,
                net.minecraft.world.item.component.ItemContainerContents.fromItems(getItemsAsList()));
        }
    }

    /**
     * Loads inventory contents from an ItemStack's NBT.
     */
    public void loadFromItem(ItemStack stack, HolderLookup.Provider registries) {
        var contents = stack.get(net.minecraft.core.component.DataComponents.CONTAINER);
        if (contents != null) {
            java.util.List<ItemStack> items = contents.stream().toList();
            for (int i = 0; i < Math.min(items.size(), inventory.getSlots()); i++) {
                inventory.setStackInSlot(i, items.get(i));
            }
        }
    }

    private boolean isEmpty() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private java.util.List<ItemStack> getItemsAsList() {
        java.util.List<ItemStack> items = new java.util.ArrayList<>();
        for (int i = 0; i < inventory.getSlots(); i++) {
            items.add(inventory.getStackInSlot(i));
        }
        return items;
    }
}

package dev.wyfy.shulkervault.block.entity;

import dev.wyfy.shulkervault.Constants;
import dev.wyfy.shulkervault.menu.ShulkerVaultMenu;
import dev.wyfy.shulkervault.platform.Services;
import dev.wyfy.shulkervault.platform.services.IInventoryHandler;
import dev.wyfy.shulkervault.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ShulkerVaultBlockEntity extends BlockEntity implements GeoBlockEntity, MenuProvider {

    private static final int SLOT_COUNT = 27;
    private static final int MAX_STACK_SIZE = 576; // 9x normal stack size

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final IInventoryHandler inventory;

    private int openCount = 0;
    private boolean isOpen = false;

    private static final RawAnimation OPEN_ANIMATION = RawAnimation.begin().thenPlay("animation");
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin(); // Idle state

    public ShulkerVaultBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModRegistry.SHULKER_VAULT_BLOCK_ENTITY_TYPE, pos, blockState);
        this.inventory = Services.PLATFORM.createInventoryHandler(SLOT_COUNT, MAX_STACK_SIZE);
        this.inventory.setChanged(this::setChanged);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", state -> {
            if (isOpen) {
                return state.setAndContinue(OPEN_ANIMATION);
            }
            return state.setAndContinue(IDLE_ANIMATION);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public void tick() {
        // Animation logic is handled by the controller
    }

    public void startOpen(Player player) {
        Constants.LOG.info("[ShulkerVaultBlockEntity] startOpen called - player: {}, isSpectator: {}, openCount: {}",
            player.getName().getString(), player.isSpectator(), openCount);
        if (!player.isSpectator()) {
            if (openCount < 0) {
                openCount = 0;
            }
            ++openCount;

            if (openCount == 1 && !isOpen) {
                Constants.LOG.info("[ShulkerVaultBlockEntity] Opening vault animation - setting isOpen to true");
                isOpen = true;
                playOpenAnimation();
            }
        }
    }

    public void stopOpen(Player player) {
        Constants.LOG.info("[ShulkerVaultBlockEntity] stopOpen called - player: {}, isSpectator: {}, openCount: {}",
            player.getName().getString(), player.isSpectator(), openCount);
        if (!player.isSpectator()) {
            --openCount;

            if (openCount <= 0 && isOpen) {
                Constants.LOG.info("[ShulkerVaultBlockEntity] Closing vault animation - setting isOpen to false");
                openCount = 0;
                isOpen = false;
                playCloseAnimation();
            }
        }
    }

    private void playOpenAnimation() {
        Constants.LOG.info("[ShulkerVaultBlockEntity] playOpenAnimation - level: {}, isClientSide: {}, openCount: {}",
            level != null, level != null && level.isClientSide, openCount);
        if (level != null && !level.isClientSide) {
            Constants.LOG.info("[ShulkerVaultBlockEntity] Sending blockEvent to trigger animation");
            level.blockEvent(worldPosition, getBlockState().getBlock(), 1, openCount);
        }
    }

    private void playCloseAnimation() {
        Constants.LOG.info("[ShulkerVaultBlockEntity] playCloseAnimation - level: {}, isClientSide: {}, openCount: {}",
            level != null, level != null && level.isClientSide, openCount);
        if (level != null && !level.isClientSide) {
            Constants.LOG.info("[ShulkerVaultBlockEntity] Sending blockEvent to close animation");
            level.blockEvent(worldPosition, getBlockState().getBlock(), 1, openCount);
        }
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        Constants.LOG.info("[ShulkerVaultBlockEntity] triggerEvent(int,int) called - id: {}", id);
        if (id == 1) {
            triggerAnim("controller", "animation");
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
        return new ShulkerVaultMenu(containerId, playerInventory, this);
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

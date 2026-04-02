package dev.wyfy.shulkervault.item;

import dev.wyfy.shulkervault.menu.ShulkerVaultMenu;
import dev.wyfy.shulkervault.menu.VaultLocation;
import dev.wyfy.shulkervault.platform.Services;
import dev.wyfy.shulkervault.platform.services.IInventoryHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class ShulkerVaultItem extends BlockItem {

    public ShulkerVaultItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            VaultLocation location = new VaultLocation.InItem(hand);

            SimpleMenuProvider menuProvider = new SimpleMenuProvider(
                (containerId, playerInventory, p) -> new ShulkerVaultMenu(containerId, playerInventory, null, location),
                stack.getHoverName()
            );

            serverPlayer.openMenu(menuProvider);

            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}

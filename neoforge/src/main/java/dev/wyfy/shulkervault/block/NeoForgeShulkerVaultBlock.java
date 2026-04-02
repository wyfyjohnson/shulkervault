package dev.wyfy.shulkervault.block;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import dev.wyfy.shulkervault.block.entity.ShulkerVaultBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * NeoForge-specific ShulkerVaultBlock that implements Create's IWrenchable interface.
 * Create will automatically detect this interface and enable wrench support.
 */
public class NeoForgeShulkerVaultBlock extends ShulkerVaultBlock implements IWrenchable {

    public NeoForgeShulkerVaultBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction newFacing = state.getValue(FACING).getClockWise();
        level.setBlock(pos, state.setValue(FACING, newFacing), 3);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity be = level.getBlockEntity(pos);
        ItemStack drop = new ItemStack(state.getBlock());
        if (be instanceof ShulkerVaultBlockEntity vault) {
            vault.saveToItem(drop, level.registryAccess());
        }
        level.removeBlock(pos, false);
        if (context.getPlayer() != null) {
            context.getPlayer().getInventory().placeItemBackInInventory(drop);
        }
        return InteractionResult.SUCCESS;
    }
}

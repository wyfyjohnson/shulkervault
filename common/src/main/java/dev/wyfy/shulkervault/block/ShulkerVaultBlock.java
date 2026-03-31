package dev.wyfy.shulkervault.block;

import dev.wyfy.shulkervault.block.entity.ShulkerVaultBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ShulkerVaultBlock extends BaseEntityBlock {

    public static final MapCodec<ShulkerVaultBlock> CODEC = simpleCodec(ShulkerVaultBlock::new);

    public ShulkerVaultBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShulkerVaultBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        dev.wyfy.shulkervault.Constants.LOG.info("[ShulkerVaultBlock] useWithoutItem called - isClientSide: {}, pos: {}", level.isClientSide, pos);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ShulkerVaultBlockEntity blockEntity) {
            dev.wyfy.shulkervault.Constants.LOG.info("[ShulkerVaultBlock] Opening menu for player: {}", player.getName().getString());
            player.openMenu(blockEntity);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return (lvl, pos, st, be) -> {
            if (be instanceof ShulkerVaultBlockEntity vault) {
                vault.tick();
            }
        };
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof ShulkerVaultBlockEntity vault) {
            vault.loadFromItem(stack, level.registryAccess());
        }
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof ShulkerVaultBlockEntity vault) {
            ItemStack stack = new ItemStack(this);
            vault.saveToItem(stack, params.getLevel().registryAccess());
            List<ItemStack> drops = new ArrayList<>();
            drops.add(stack);
            return drops;
        }
        return super.getDrops(state, params);
    }

    @Override
    public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ShulkerVaultBlockEntity vault) {
            vault.saveToItem(stack, level.registryAccess());
        }
        return stack;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        dev.wyfy.shulkervault.Constants.LOG.info("[ShulkerVaultBlock] triggerEvent - id: {}, param: {}, isClientSide: {}", id, param, level.isClientSide);
        super.triggerEvent(state, level, pos, id, param);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ShulkerVaultBlockEntity vault) {
            if (id == 1) {
                dev.wyfy.shulkervault.Constants.LOG.info("[ShulkerVaultBlock] Animation event - param: {}, triggering anim", param);
                // Trigger animation on the client side
                if (param > 0) {
                    vault.triggerAnim("controller", "animation");
                }
                return true;
            }
        }
        return false;
    }
}

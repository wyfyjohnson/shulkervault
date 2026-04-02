package dev.wyfy.shulkervault.block;

import dev.wyfy.shulkervault.block.entity.ShulkerVaultBlockEntity;
import dev.wyfy.shulkervault.menu.VaultLocation;
import dev.wyfy.shulkervault.platform.Services;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class ShulkerVaultBlock extends BaseEntityBlock {

    public static final MapCodec<ShulkerVaultBlock> CODEC = simpleCodec(ShulkerVaultBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public ShulkerVaultBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
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
            Services.PLATFORM.openExtendedMenu((ServerPlayer) player, blockEntity, buf -> new VaultLocation.InBlock(pos).write(buf));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    if (level.getBlockEntity(pos) instanceof ShulkerVaultBlockEntity vault) {
        VoxelShape base = getBaseShape(state.getValue(FACING));
        AABB lid = vault.getLidBoundingBox();
        VoxelShape lidShape = Shapes.box(lid.minX, lid.minY, lid.minZ, lid.maxX, lid.maxY, lid.maxZ);
        return Shapes.or(base, lidShape);
    }
    return Shapes.block();
}

@Override
public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return getCollisionShape(state, level, pos, context);
}

private static VoxelShape getBaseShape(Direction facing) {
    return switch (facing) {
        case UP    -> Shapes.box(0,   0,   0,   1,   0.5, 1  );
        case DOWN  -> Shapes.box(0,   0.5, 0,   1,   1.0, 1  );
        case NORTH -> Shapes.box(0,   0,   0.5, 1,   1.0, 1.0);
        case SOUTH -> Shapes.box(0,   0,   0.0, 1,   1.0, 0.5);
        case WEST  -> Shapes.box(0.5, 0,   0,   1.0, 1.0, 1  );
        case EAST  -> Shapes.box(0.0, 0,   0,   0.5, 1.0, 1  );
    };
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
        super.triggerEvent(state, level, pos, id, param);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(id, param);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        if (contents != null) {
            List<ItemStack> items = contents.stream().toList();

            // Process items in groups of 4 (matching backing slot layout)
            List<Component> itemLines = new ArrayList<>();
            int totalBackingSlots = 108; // 27 virtual slots × 4 backing slots

            for (int groupIndex = 0; groupIndex < 27; groupIndex++) {
                int firstBackingSlot = groupIndex * 4;

                // Sum counts across the 4 backing slots if they're the same item
                ItemStack representativeStack = ItemStack.EMPTY;
                int totalCount = 0;

                for (int i = 0; i < 4; i++) {
                    int slotIndex = firstBackingSlot + i;
                    if (slotIndex < items.size()) {
                        ItemStack slotStack = items.get(slotIndex);
                        if (!slotStack.isEmpty()) {
                            if (representativeStack.isEmpty()) {
                                representativeStack = slotStack;
                                totalCount = slotStack.getCount();
                            } else if (ItemStack.isSameItemSameComponents(representativeStack, slotStack)) {
                                totalCount += slotStack.getCount();
                            }
                        }
                    }
                }

                // Add to tooltip if this group has items
                if (!representativeStack.isEmpty()) {
                    Component itemComponent = Component.translatable(representativeStack.getDescriptionId())
                        .append(Component.literal(" x" + totalCount));
                    itemLines.add(itemComponent);
                }
            }

            // Cap at showing 5 item types
            int displayCount = Math.min(5, itemLines.size());
            for (int i = 0; i < displayCount; i++) {
                tooltipComponents.add(itemLines.get(i));
            }

            // Add "..." if there are more items
            if (itemLines.size() > 5) {
                tooltipComponents.add(Component.literal("..."));
            }
        }
    }
}

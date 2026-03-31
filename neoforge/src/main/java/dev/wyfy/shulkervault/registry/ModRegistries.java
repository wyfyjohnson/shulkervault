package dev.wyfy.shulkervault.registry;

import dev.wyfy.shulkervault.Constants;
import dev.wyfy.shulkervault.block.ShulkerVaultBlock;
import dev.wyfy.shulkervault.block.entity.ShulkerVaultBlockEntity;
import dev.wyfy.shulkervault.menu.ShulkerVaultMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRegistries {

    // DeferredRegisters
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, Constants.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Constants.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Constants.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    // Blocks
    public static final DeferredHolder<Block, ShulkerVaultBlock> SHULKER_VAULT_BLOCK = BLOCKS.register("shulker_vault",
            () -> new ShulkerVaultBlock(BlockBehaviour.Properties.of()
                    .strength(2.0f)
                    .mapColor(MapColor.COLOR_PURPLE)
                    .noOcclusion()));

    // Items
    public static final DeferredHolder<Item, BlockItem> SHULKER_VAULT_ITEM = ITEMS.register("shulker_vault",
            () -> new BlockItem(SHULKER_VAULT_BLOCK.get(), new Item.Properties()));

    // Block Entities
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShulkerVaultBlockEntity>> SHULKER_VAULT_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("shulker_vault", () -> BlockEntityType.Builder.of(
                    ShulkerVaultBlockEntity::new,
                    SHULKER_VAULT_BLOCK.get()
            ).build(null));

    // Menus
    public static final DeferredHolder<MenuType<?>, MenuType<ShulkerVaultMenu>> SHULKER_VAULT_MENU =
            MENUS.register("shulker_vault", () -> IMenuTypeExtension.create(
                    (windowId, inv, data) -> new ShulkerVaultMenu(windowId, inv)
            ));

    // Creative Tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SHULKER_VAULT_TAB = CREATIVE_TABS.register("shulker_vault",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + Constants.MOD_ID))
                    .icon(() -> new ItemStack(SHULKER_VAULT_ITEM.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(SHULKER_VAULT_ITEM.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        MENUS.register(eventBus);
        CREATIVE_TABS.register(eventBus);
    }

    public static void init() {
        // Update the common registry references
        dev.wyfy.shulkervault.registry.ModRegistry.SHULKER_VAULT_BLOCK_ENTITY_TYPE = SHULKER_VAULT_BLOCK_ENTITY.get();
        dev.wyfy.shulkervault.registry.ModRegistry.SHULKER_VAULT_MENU_TYPE = SHULKER_VAULT_MENU.get();
    }
}

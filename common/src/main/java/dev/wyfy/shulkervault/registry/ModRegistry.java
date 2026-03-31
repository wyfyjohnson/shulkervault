package dev.wyfy.shulkervault.registry;

import dev.wyfy.shulkervault.Constants;
import dev.wyfy.shulkervault.block.ShulkerVaultBlock;
import dev.wyfy.shulkervault.block.entity.ShulkerVaultBlockEntity;
import dev.wyfy.shulkervault.menu.ShulkerVaultMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * This class holds references to registry objects for the Shulker Vault mod.
 * The actual registration is done in the loader-specific modules.
 * This class only holds references that are set by the loader-specific code.
 */
public class ModRegistry {

    // These are set by the loader-specific registration code
    public static BlockEntityType<ShulkerVaultBlockEntity> SHULKER_VAULT_BLOCK_ENTITY_TYPE;
    public static MenuType<ShulkerVaultMenu> SHULKER_VAULT_MENU_TYPE;

    /**
     * This method should be called from loader-specific initialization code.
     * It allows us to log that the common registry has been initialized.
     */
    public static void init() {
        Constants.LOG.info("Initialized Shulker Vault registry objects");
    }
}

package dev.wyfy.shulkervault;

import dev.wyfy.shulkervault.block.entity.ShulkerVaultBlockEntity;
import dev.wyfy.shulkervault.platform.NeoForgeInventoryHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Registers NeoForge capabilities for Shulker Vault block entities.
 * This allows Create funnels, conveyors, and other mods to interact with the inventory.
 */
@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class CapabilityRegistration {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            dev.wyfy.shulkervault.registry.ModRegistries.SHULKER_VAULT_BLOCK_ENTITY.get(),
            (blockEntity, side) -> {
                if (blockEntity.getInventory() instanceof NeoForgeInventoryHandler neoForgeHandler) {
                    return neoForgeHandler.getHandler();
                }
                return null;
            }
        );
    }
}

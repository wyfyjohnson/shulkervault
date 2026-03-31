package dev.wyfy.shulkervault;

import dev.wyfy.shulkervault.registry.ModRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Constants.MOD_ID)
public class ShulkerVault {

    public ShulkerVault(IEventBus modBus) {
        Constants.LOG.info("Initializing Shulker Vault mod");

        // Register all deferred registers
        ModRegistries.register(modBus);

        // Common setup
        modBus.addListener(this::commonSetup);

        // Use NeoForge to bootstrap the Common mod
        CommonClass.init();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModRegistries::init);
    }
}
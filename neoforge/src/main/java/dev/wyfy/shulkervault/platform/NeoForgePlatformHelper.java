package dev.wyfy.shulkervault.platform;

import dev.wyfy.shulkervault.platform.services.IInventoryHandler;
import dev.wyfy.shulkervault.platform.services.IPlatformHelper;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public IInventoryHandler createInventoryHandler(int size, int maxStackSize) {
        return new NeoForgeInventoryHandler(size, maxStackSize);
    }
}
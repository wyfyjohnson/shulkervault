package dev.wyfy.shulkervault.platform;

import dev.wyfy.shulkervault.platform.services.IInventoryHandler;
import dev.wyfy.shulkervault.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public IInventoryHandler createInventoryHandler(int size, int maxStackSize) {
        return new FabricInventoryHandler(size, maxStackSize);
    }
}

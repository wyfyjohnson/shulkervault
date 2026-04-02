package dev.wyfy.shulkervault.client;

import dev.wyfy.shulkervault.client.renderer.ShulkerVaultBlockRenderer;
import dev.wyfy.shulkervault.client.screen.ShulkerVaultScreen;
import dev.wyfy.shulkervault.registry.ModRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = dev.wyfy.shulkervault.Constants.MOD_ID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Client initialization
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModRegistries.SHULKER_VAULT_BLOCK_ENTITY.get(),
                ShulkerVaultBlockRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModRegistries.SHULKER_VAULT_MENU.get(), ShulkerVaultScreen::new);
    }
}

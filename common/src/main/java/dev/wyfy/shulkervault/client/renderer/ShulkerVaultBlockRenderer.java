package dev.wyfy.shulkervault.client.renderer;

import dev.wyfy.shulkervault.Constants;
import dev.wyfy.shulkervault.block.entity.ShulkerVaultBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ShulkerVaultBlockRenderer extends GeoBlockRenderer<ShulkerVaultBlockEntity> {

    public ShulkerVaultBlockRenderer() {
        super(new DefaultedBlockGeoModel<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "shulker_vault")));
    }
}

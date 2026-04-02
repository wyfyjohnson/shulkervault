package dev.wyfy.shulkervault.client.screen;

import dev.wyfy.shulkervault.Constants;
import dev.wyfy.shulkervault.menu.ShulkerVaultMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ShulkerVaultScreen extends AbstractContainerScreen<ShulkerVaultMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Constants.MOD_ID, "textures/gui/container/shulker_vault.png"
    );
    private static final int BACKING_SLOTS_PER_VIRTUAL = 4;
    private static final int TOTAL_BACKING_SLOTS = 108;

    public ShulkerVaultScreen(ShulkerVaultMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 168;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // No custom count rendering here — renderSlot handles it below.
        super.renderLabels(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        int idx = this.menu.slots.indexOf(slot);
        boolean isVisibleVaultSlot = idx >= 0
                && idx < TOTAL_BACKING_SLOTS
                && idx % BACKING_SLOTS_PER_VIRTUAL == 0;

        if (!isVisibleVaultSlot) {
            super.renderSlot(guiGraphics, slot);
            return;
        }

        int totalCount = 0;
        ItemStack rep = ItemStack.EMPTY;
        for (int i = 0; i < BACKING_SLOTS_PER_VIRTUAL; i++) {
            ItemStack s = this.menu.slots.get(idx + i).getItem();
            if (!s.isEmpty()) {
                if (rep.isEmpty()) rep = s;
                if (ItemStack.isSameItemSameComponents(rep, s)) totalCount += s.getCount();
            }
        }

        if (rep.isEmpty()) return; // truly empty slot, render nothing

        String countLabel = totalCount > 1 ? formatCount(totalCount) : (totalCount == 1 ? null : "");
        guiGraphics.renderItem(rep, this.leftPos + slot.x, this.topPos + slot.y);
        guiGraphics.renderItemDecorations(this.font, rep, this.leftPos + slot.x, this.topPos + slot.y, countLabel);
    }

    private String formatCount(int count) {
        if (count > 999) {
            return String.format("%.1fk", count / 1000.0f);
        }
        return String.valueOf(count);
    }
}

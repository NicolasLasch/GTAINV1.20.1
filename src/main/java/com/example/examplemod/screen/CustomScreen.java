package com.example.examplemod.screen;

import com.example.examplemod.container.CustomContainer;
import com.example.examplemod.filter.FilterManager;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.OpenCustomContainerPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CustomScreen extends AbstractContainerScreen<CustomContainer> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("examplemod", "textures/gui/custom_inventory.png");
    private EditBox amountField;

    public CustomScreen(CustomContainer screenContainer, Inventory inv, Component title) {
        super(screenContainer, inv, title);
        this.imageWidth = 176; // Adjust size as needed
        this.imageHeight = 166; // Adjust size as needed
        this.leftPos = 0; // Align to top-left
        this.topPos = 0; // Align to top-left
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        int defaultColor = 0xB3000000; // Black color
        int selectedColor = 0xB3737373; // White color
        for (Slot slot : this.menu.slots) {
            boolean isSelected = slot == this.getSlotUnderMouse();
            int backgroundColor = isSelected ? selectedColor : defaultColor;
            drawRoundedRect(guiGraphics, this.leftPos + slot.x - 8, this.topPos + slot.y - 8, 32, 32, backgroundColor, isSelected);
        }
    }



    private void drawRoundedRect(GuiGraphics guiGraphics, int x, int y, int width, int height, int color, boolean isSelected) {
        RenderSystem.enableBlend(); // Enable blending
        RenderSystem.defaultBlendFunc(); // Use default blend function

        guiGraphics.fill(x, y, x + width, y + height, color);

        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        float scale = 2F;
        guiGraphics.drawString(this.font, "BackPack", 14 / scale, 120 / scale, 0xFFFFFF, false);
        scale = 1F;
        guiGraphics.drawString(this.font, "1", 11 + 6 / scale, 257 / scale, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "2", 47 + 6 / scale, 257 / scale, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "3", 83 + 6 / scale, 257 / scale, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "4", 119 + 6 / scale, 257 / scale, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "5", 155 + 6 / scale, 257 / scale, 0xFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.renderDraggedItemWithBackground(guiGraphics, mouseX, mouseY);
    }

    private void renderDraggedItemWithBackground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ItemStack draggedStack = this.menu.getCarried();

        if (!draggedStack.isEmpty()) {
            int backgroundColor = 0xB3737373;
            guiGraphics.fill(mouseX - 16, mouseY - 16, mouseX + 16, mouseY + 16, backgroundColor);
        }
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = 0;
        this.topPos = 0;

        this.amountField = new EditBox(this.font, this.leftPos + 335, this.topPos + 290, 40, 20, Component.literal("Amount"));
        this.amountField.setMaxLength(2);
        this.amountField.setValue("0");
        this.addRenderableWidget(this.amountField);

        // Adding custom buttons for filters
        this.addRenderableWidget(new CustomButton(this.leftPos + 129, this.topPos + 60, 15, 15, new ResourceLocation("examplemod", "textures/icons/all.png"), button -> {
            FilterManager.setFilterMode(0);
            refreshContainer();
        }));

        this.addRenderableWidget(new CustomButton(this.leftPos + 149, this.topPos + 60, 15, 15, new ResourceLocation("examplemod", "textures/icons/food.png"), button -> {
            FilterManager.setFilterMode(1);
            refreshContainer();
        }));

        this.addRenderableWidget(new CustomButton(this.leftPos + 169, this.topPos + 60, 15, 15, new ResourceLocation("examplemod", "textures/icons/clothes.png"), button -> {
            FilterManager.setFilterMode(2);
            refreshContainer();
        }));
    }

    private void refreshContainer() {
        Minecraft mc = Minecraft.getInstance();
        mc.player.closeContainer();
        NetworkHandler.INSTANCE.sendToServer(new OpenCustomContainerPacket());
    }

    private class CustomButton extends Button {
        private final ResourceLocation icon;
        private final boolean isIconButton;

        public CustomButton(int x, int y, int width, int height, ResourceLocation icon, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, Button.DEFAULT_NARRATION);
            this.icon = icon;
            this.isIconButton = true;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            Minecraft minecraft = Minecraft.getInstance();
            int borderColor = this.isHoveredOrFocused() ? 0xFFFFFFFF : 0xFF000000;
            drawRoundedRect(guiGraphics, this.getX() - 2, this.getY() - 2, this.getWidth() + 4, this.getHeight() + 4, borderColor, this.isHoveredOrFocused());

            if (isIconButton && icon != null) {
                guiGraphics.blit(icon, this.getX() + 2, this.getY() + 2, 0, 0, this.getWidth() - 4, this.getHeight() - 4, this.getWidth() - 4, this.getHeight() - 4);
            }
        }

        private void drawRoundedRect(GuiGraphics guiGraphics, int x, int y, int width, int height, int color, boolean isSelected) {
            RenderSystem.enableBlend(); // Enable blending
            RenderSystem.defaultBlendFunc(); // Use default blend function

            guiGraphics.fill(x, y, x + width, y + height, color);

            RenderSystem.disableBlend();
        }
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        super.slotClicked(slot, slotId, mouseButton, type);
        // Handle client-side visuals if needed
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Disable drop key (Q key by default)
        if (this.minecraft.options.keyDrop.matches(keyCode, scanCode)) {
            return false; // Do nothing when drop key is pressed
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Slot slot = getSlotUnderMouse();
        if (slot == null && !this.menu.getCarried().isEmpty()) {
            return false; // Prevent dropping the item
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}

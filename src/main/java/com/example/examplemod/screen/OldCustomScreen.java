package com.example.examplemod.screen;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.container.CustomContainer;
import com.example.examplemod.container.OldCustomContainer;
import com.example.examplemod.filter.FilterManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class OldCustomScreen extends AbstractContainerScreen<OldCustomContainer> {

    private static final ResourceLocation ROUNDED_RECT_TEXTURE = new ResourceLocation("examplemod", "textures/gui/rounded_rect.png");
    private static final ResourceLocation WEIGHT_BAR_TEXTURE = new ResourceLocation("examplemod", "textures/gui/weight_bar.png");

    private Slot draggedSlot = null;  // The slot currently being dragged
    private ItemStack draggedStack = ItemStack.EMPTY;  // The item stack being dragged
    private int dragOffsetX, dragOffsetY;  // The offset of the drag start
    private Slot selectedSlot = null;
    private Slot secondSelectedSlot = null;
    private EditBox amountField;
    private int filterMode;
    private final Font customFont;

    public OldCustomScreen(OldCustomContainer screenContainer, Inventory inv, Component title) {
        super(screenContainer, inv, title);
        this.imageWidth = 176; // Adjust size as needed
        this.imageHeight = 166; // Adjust size as needed
        this.leftPos = 0; // Align to top-left
        this.topPos = 0; // Align to top-left
        this.filterMode = FilterManager.getFilterMode();
        this.customFont = Minecraft.getInstance().font;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        int defaultColor = 0xB3000000; // Black color with 70% opacity
        int selectedColor = 0xFFFFFFFF; // White color

        // Draw the inventory slots
        for (Slot slot : this.menu.slots) {
            boolean isSelected = slot == this.getSlotUnderMouse();
            int backgroundColor = isSelected ? selectedColor : defaultColor;
            drawRoundedRect(guiGraphics, this.leftPos + slot.x - 8, this.topPos + slot.y - 8, 32, 32, backgroundColor, isSelected);
        }

        // Draw the text field background
        drawRoundedRect(guiGraphics, this.amountField.getX() - 2, this.amountField.getY() - 2, this.amountField.getWidth() + 4, this.amountField.getHeight() + 4, 0xFF000000, false);
    }

    private void drawRoundedRect(GuiGraphics guiGraphics, int x, int y, int width, int height, int color, boolean isSelected) {
        guiGraphics.fill(x, y, x + width, y + height, color);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        float scale = 2F;
        guiGraphics.drawString(this.customFont, "BackPack", 14 / scale, 120 / scale, 0xFFFFFF, false);
        scale = 1F;
        guiGraphics.drawString(this.customFont, "1", 11 + 6 / scale, 257 / scale, 0xFFFFFF, false);
        guiGraphics.drawString(this.customFont, "2", 47 + 6 / scale, 257 / scale, 0xFFFFFF, false);
        guiGraphics.drawString(this.customFont, "3", 83 + 6 / scale, 257 / scale, 0xFFFFFF, false);
        guiGraphics.drawString(this.customFont, "4", 119 + 6 / scale, 257 / scale, 0xFFFFFF, false);
        guiGraphics.drawString(this.customFont, "5", 155 + 6 / scale, 257 / scale, 0xFFFFFF, false);

        double totalWeight = this.menu.getTotalWeight();
        renderWeightBar(guiGraphics, totalWeight, 30, 10, 10); // Assuming 30 kg max weight
    }

    private void renderWeightBar(GuiGraphics guiGraphics, double weight, double maxWeight, int x, int y) {
        int barWidth = 100;
        int filled = (int) ((weight / maxWeight) * barWidth);
        guiGraphics.fill(x, y, x + barWidth, y + 10, 0xFF000000);
        guiGraphics.fill(x, y, x + filled, y + 10, 0xFFFFFFFF);
        guiGraphics.drawString(this.customFont, String.format("%.2f/%.2f kg", weight, maxWeight), x + barWidth + 5, y, 0xFFFFFF);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.amountField.render(guiGraphics, mouseX, mouseY, partialTicks);
        // Render the dragged item stack under the cursor
        if (!draggedStack.isEmpty() && draggedSlot != null) {
            guiGraphics.renderItem(draggedStack, mouseX - 8, mouseY - 8);
        }
        if (draggedSlot != null) {
            drawRoundedRect(guiGraphics, mouseX - 16, mouseY - 16, 32, 32, 0xB3000000, true);
        }

        drawEntityOnScreen(this.leftPos + 350, this.topPos + 260, 60, (float) (this.leftPos + 370) - mouseX, (float) (this.topPos + 260 - 50) - mouseY, Minecraft.getInstance().player);
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

        this.addRenderableWidget(Button.builder(Component.literal("Give"), button -> {
            if (selectedSlot != null && selectedSlot.hasItem()) {
                try {
                    int amount = Integer.parseInt(this.amountField.getValue());
                    if (amount > 0 && amount <= 64) {
                        // Your custom event handler
                        // ClientEventHandler.onGiveButtonClick(selectedSlot, amount);
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid input
                }
            }
        }).bounds(this.leftPos + 325, this.topPos + 320, 60, 20).build());

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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Slot clickedSlot = this.getSlotUnderMouse();

        if (clickedSlot != null && button == 0) { // Left-click
            if (this.draggedSlot == null) {
                // Start dragging
                this.draggedSlot = clickedSlot;
                this.menu.pickUpItem(clickedSlot.index);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.draggedSlot != null && button == 0) { // Dragging with left-click
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.draggedSlot != null && button == 0) { // Left-click release
            Slot targetSlot = this.getSlotUnderMouse();

            if (targetSlot != null && targetSlot != this.draggedSlot) {
                this.menu.swapItems(this.draggedSlot.index, targetSlot.index);
            } else if (targetSlot != null) {
                this.menu.placeItem(targetSlot.index);
            }

            this.draggedSlot = null;
            this.draggedStack = ItemStack.EMPTY;
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    // Helper to refresh the container
    private void refreshContainer() {
        Minecraft mc = Minecraft.getInstance();
        mc.player.closeContainer();
        mc.setScreen(new CustomScreen(new CustomContainer(0, mc.player.getInventory()), mc.player.getInventory(), Component.literal("Backpack")));
    }

    public static void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, LivingEntity livingEntity) {
        // Existing code for drawing the entity
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Disable dropping items using the drop key
        if (keyCode == this.minecraft.options.keyDrop.getKey().getValue()) {
            return false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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
}

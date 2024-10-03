package com.example.examplemod.screen;

import com.example.examplemod.container.CustomContainer;
import com.example.examplemod.filter.FilterManager;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.OpenCustomContainerPacket;
import com.example.examplemod.network.ServerboundSelectTradePacket;
import com.example.examplemod.network.ServerboundTradePacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CustomScreen extends AbstractContainerScreen<CustomContainer> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("examplemod", "textures/gui/custom_inventory.png");
    private EditBox amountField;
    private EditBox recipientField;
    private Button tradeButton;

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

        this.addRenderableWidget(Button.builder(
                        Component.literal("Trade"),
                        button -> {
                            NetworkHandler.INSTANCE.sendToServer(new ServerboundSelectTradePacket());
                            initiateTrade();
                        })
                .pos(this.leftPos + 335, this.topPos + 370)
                .size(50, 20)
                .build()
        );

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

    private void initiateTrade() {
        ItemStack tradeItem = this.menu.getSlot(0).getItem();
        if (tradeItem.isEmpty()) {
            return;
        }

        String amountStr = amountField.getValue();
        int tradeAmount;
        try {
            tradeAmount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            // Handle invalid number format
            return;
        }

        // Check if the player has enough items in slot 40
        if (tradeItem.getCount() < tradeAmount) {
            // Show an error message if the player doesn't have enough items
            return;
        }

        // Proceed to the next step to select a nearby player for trade
        openPlayerSelectionScreen();
    }

    private void openPlayerSelectionScreen() {
        // Get nearby players within a 5-block radius
        List<Player> nearbyPlayers = getNearbyPlayers(5); // This should be a method that checks the server for nearby players

        if (nearbyPlayers.isEmpty()) {
            System.out.println("There is no players nearby");
            return;
        }

        for (Player player : nearbyPlayers) {
            this.addRenderableWidget(Button.builder(
                            Component.literal(player.getName().getString()),
                            button -> initiatePlayerTrade(player)
                    ).pos(this.leftPos + 350, this.topPos + 30 + nearbyPlayers.indexOf(player) * 20) // Adjust position based on index
                    .size(80, 20)
                    .build());
        }
    }

    private List<Player> getNearbyPlayers(int radius) {
        Player player = Minecraft.getInstance().player;
        List<Player> nearbyPlayers = player.level().getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(radius));
        nearbyPlayers.remove(player);
        return nearbyPlayers;
    }


    private void initiatePlayerTrade(Player recipient) {
        ItemStack tradeItem = this.menu.getSlot(0).getItem();
        String amountStr = amountField.getValue();
        int tradeAmount = Integer.parseInt(amountStr);

        // Send trade packet to server
        NetworkHandler.INSTANCE.sendToServer(new ServerboundTradePacket(recipient.getUUID(), tradeItem, tradeAmount));
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
    }
}

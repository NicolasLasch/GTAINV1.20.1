package com.example.examplemod.screen;

import com.example.examplemod.container.CustomContainer;
import com.example.examplemod.filter.FilterManager;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.OpenCustomContainerPacket;
import com.example.examplemod.network.ServerboundSelectTradePacket;
import com.example.examplemod.network.ServerboundTradePacket;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomScreen extends AbstractContainerScreen<CustomContainer> {

    private boolean isPopupOpen = false;
    private static final ResourceLocation TEXTURE = new ResourceLocation("examplemod", "textures/gui/custom_inventory.png");
    private EditBox amountField;
    private EditBox recipientField;
    private Button tradeButton;
    private List<Button> popupButtons = new ArrayList<>();
    private double previousMouseX, previousMouseY;

    public CustomScreen(CustomContainer screenContainer, Inventory inv, Component title) {
        super(screenContainer, inv, title);
        this.imageWidth = 176; // Adjust size as needed
        this.imageHeight = 166; // Adjust size as needed
        this.leftPos = 0; // Align to top-left
        this.topPos = 0; // Align to top-left
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        int defaultColor = 0xB3000000;
        int selectedColor = 0xB3737373;
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
        float scale = 1F;
        guiGraphics.drawString(this.font, "BackPack", 61 + 6 / scale, 60 / scale, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "1", 61 + 6 / scale, 257 / scale, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "2", 107 + 6 / scale, 257 / scale, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "3", 143 + 6 / scale, 257 / scale, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "4", 169 + 6 / scale, 257 / scale, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "5", 205 + 6 / scale, 257 / scale, 0xFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.renderDraggedItemWithBackground(guiGraphics, mouseX, mouseY);
        renderEntityInInventoryFollowsMouse(guiGraphics, this.leftPos + 425, this.topPos + 285, 80, (float)(this.leftPos + 445) - mouseX, (float)(this.topPos + 290-80) - mouseY, Minecraft.getInstance().player);

        if (isPopupOpen) {
            renderTradePopup(guiGraphics, mouseX, mouseY);
        }
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

        this.amountField = new CustomEditBox(this.font, this.leftPos + 415, this.topPos + 340, 20, 20, Component.literal("Amount"));
        this.amountField.setMaxLength(2);
        this.amountField.setValue("0");
        this.addRenderableWidget(this.amountField);

// Create a custom Button
        this.addRenderableWidget(new TradeButton(
                this.leftPos + 400, this.topPos + 365, 50, 20,
                Component.literal("Trade"),
                button -> {
                    NetworkHandler.INSTANCE.sendToServer(new ServerboundSelectTradePacket());
                    initiateTrade();
                }
        ));

        // Adding custom buttons for filters
        this.addRenderableWidget(new CustomButton(this.leftPos + 229, this.topPos + 60, 15, 15, new ResourceLocation("examplemod", "textures/icons/all.png"), button -> {
            FilterManager.setFilterMode(0);
            refreshContainer();
        }));

        this.addRenderableWidget(new CustomButton(this.leftPos + 249, this.topPos + 60, 15, 15, new ResourceLocation("examplemod", "textures/icons/food.png"), button -> {
            FilterManager.setFilterMode(1);
            refreshContainer();
        }));

        this.addRenderableWidget(new CustomButton(this.leftPos + 269, this.topPos + 60, 15, 15, new ResourceLocation("examplemod", "textures/icons/clothes.png"), button -> {
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

        if (tradeItem.getCount() < tradeAmount) {
            // Show an error message if the player doesn't have enough items
            return;
        }

        // Proceed to the next step to select a nearby player for trade
        isPopupOpen = true;
    }

    // Modify the openPlayerSelectionScreen method to store references to the buttons in the popup
    private void openPlayerSelectionScreen() {
        // Get nearby players within a 5-block radius
        List<Player> nearbyPlayers = getNearbyPlayers(5);

        // Remove any existing buttons when reopening the popup
        if (!isPopupOpen) {
            clearPopupButtons();
            return;
        }

        if (nearbyPlayers.isEmpty()) {
            System.out.println("There are no players nearby");
            return;
        }

        CrossButton closeButton = new CrossButton(this.leftPos + 480,
                this.topPos + 30,
                20,
                20,
                new ResourceLocation("examplemod", "textures/icons/cross.png"),
                button -> {
                    isPopupOpen = false;
                    clearPopupButtons();
                });
        // Add the close button to the popup and the list
        this.addRenderableWidget(closeButton);
        popupButtons.add(closeButton);

        // Render nearby players as buttons inside the popup
        for (Player player : nearbyPlayers) {
            CustomPlayerButton playerButton = new CustomPlayerButton(
                    this.leftPos + 365,
                    this.topPos + 80 + nearbyPlayers.indexOf(player) * 30,
                    120,
                    20,
                    player,
                    button -> initiatePlayerTrade(player));
            this.addRenderableWidget(playerButton);
            popupButtons.add(playerButton);
        }
    }

    private void clearPopupButtons() {
        for (Button button : popupButtons) {
            this.removeWidget(button);
        }
        popupButtons.clear();
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

    private void renderTradePopup(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Popup background
        int popupX = this.leftPos + 350;
        int popupY = this.topPos + 30;
        int popupWidth = 150;
        int popupHeight = 300;
        int backgroundColor = 0xB3000000; // Black background with transparency

        guiGraphics.fill(popupX, popupY, popupX + popupWidth, popupY + popupHeight, backgroundColor);

        // Render trade item and amount
        ItemStack tradeItem = this.menu.getSlot(0).getItem();
        String amountStr = amountField.getValue();
        guiGraphics.drawString(this.font, "Item: " + tradeItem.getDisplayName().getString(), popupX + 10, popupY + 10, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "Amount: " + amountStr, popupX + 10, popupY + 30, 0xFFFFFF, false);
        openPlayerSelectionScreen();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int popupX = this.leftPos + 350;
        int popupY = this.topPos + 30;
        int popupWidth = 150;
        int popupHeight = 300;

        if(isPopupOpen){
            if (mouseX < popupX || mouseX > popupX + popupWidth || mouseY < popupY || mouseY > popupY + popupHeight) {
                // Close the popup if the left mouse button is clicked
                if (button == 0) {
                    isPopupOpen = false;  // Your method to close the popup
                    clearPopupButtons();
                    return true;   // Consume the click event
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button); // Pass event to other widgets if inside
    }

    private void refreshContainer() {
        Minecraft mc = Minecraft.getInstance();
        mc.player.closeContainer();
        NetworkHandler.INSTANCE.sendToServer(new OpenCustomContainerPacket());
    }

    private void restoreMousePosition() {
        try {
            MouseHandler mouseHelper = Minecraft.getInstance().mouseHandler;

            // Log the intended restoration position
            System.out.println("Restoring Mouse Position: x= " + previousMouseX +  " y= " + previousMouseY);

            // Reflection to set the mouse position
            long windowPointer = Minecraft.getInstance().getWindow().getWindow();
            Class<?> mouseHelperClass = MouseHandler.class;

            java.lang.reflect.Method setPositionMethod = mouseHelperClass.getDeclaredMethod("onMove", long.class, double.class, double.class);
            setPositionMethod.setAccessible(true);
            setPositionMethod.invoke(mouseHelper, windowPointer, previousMouseX, previousMouseY);

        } catch (Exception e) {
            System.out.println("Error");
        }
    }

    public class CustomEditBox extends EditBox {
        public CustomEditBox(Font font, int x, int y, int width, int height, Component placeholder) {
            super(font, x, y, width, height, placeholder);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            // Draw the black background
            drawRoundedRect(guiGraphics, this.getX(), this.getY(), this.width, this.height, 0xB3000000, false);

            // Draw the text
            guiGraphics.drawString(Minecraft.getInstance().font, this.getValue(), this.getX() + 4, this.getY() + (this.height - 8) / 2, 0xFFFFFFFF, false);

            // Draw the cursor if the edit box is focused
            if (this.isFocused() && this.getValue().length() > 0) {
                int cursorX = this.getX() + 4 + Minecraft.getInstance().font.width(this.getValue().substring(0, this.getCursorPosition()));
                guiGraphics.fill(cursorX, this.getY() + 2, cursorX + 1, this.getY() + this.height - 2, 0xFFFFFFFF);
            }
        }
    }

    public class TradeButton extends Button {
        public TradeButton(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            // Draw the black background
            drawRoundedRect(guiGraphics, this.getX(), this.getY(), this.width, this.height, 0xB3000000, false);

            // Draw the button text
            int textWidth = Minecraft.getInstance().font.width(this.getMessage());
            int textX = this.getX() + (this.width - textWidth) / 2;
            int textY = this.getY() + (this.height - 8) / 2; // Center text vertically
            guiGraphics.drawString(Minecraft.getInstance().font, this.getMessage().getString(), textX, textY, 0xFFFFFFFF, false);
        }
    }



    public class CustomPlayerButton extends Button {
        private final Player player;

        public CustomPlayerButton(int x, int y, int width, int height, Player player, OnPress onPress) {
            super(x, y, width, height, Component.literal(player.getName().getString()), onPress, Button.DEFAULT_NARRATION);
            this.player = player;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            int borderColor = 0xFFFFFFFF; // White border color
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, borderColor);

            // Draw the player head on the left
            drawPlayerHead(this.getX() + 2, this.getY() + 2, 16, 16, player, guiGraphics);

            // Draw the player's name on the right
            guiGraphics.drawString(Minecraft.getInstance().font, player.getName().getString(), this.getX() + 20, this.getY() + 8, 0x000000, false);
        }

        // Render player head (custom rendering method)
        private void drawPlayerHead(int x, int y, int width, int height, Player player, GuiGraphics guiGraphics) {
            Minecraft minecraft = Minecraft.getInstance();
            SkinManager skinManager = minecraft.getSkinManager();
            ResourceLocation playerSkin = getPlayerSkin(player, skinManager);

            RenderSystem.setShaderTexture(0, playerSkin);

            // Draw player head from the skin texture
            guiGraphics.blit(playerSkin, x, y, width, height, 8, 8, 8, 8, 64, 64);
        }

        private ResourceLocation getPlayerSkin(Player player, SkinManager skinManager) {
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> skinData = skinManager.getInsecureSkinInformation(player.getGameProfile());

            // Check if the player has a custom skin or use the default one
            if (skinData.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                return skinManager.registerTexture(skinData.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
            } else {
                return DefaultPlayerSkin.getDefaultSkin(player.getUUID());
            }
        }
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

    private class CrossButton extends Button {
        private final ResourceLocation icon;
        private final boolean isIconButton;

        public CrossButton(int x, int y, int width, int height, ResourceLocation icon, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, Button.DEFAULT_NARRATION);
            this.icon = icon;
            this.isIconButton = true;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            if (isIconButton && icon != null) {
                guiGraphics.blit(icon, this.getX(), this.getY(), 0, 0, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
            }
        }
    }

    public static void renderEntityInInventoryFollowsMouse(GuiGraphics pGuiGraphics, int pX, int pY, int pScale, float mouseX, float mouseY, LivingEntity entity) {
        // Reduce the mouse sensitivity to avoid the entity looking too far down
        float f = (float)Math.atan(mouseX / 80.0F);  // Increased divisor to reduce the effect
        float f1 = (float)Math.atan(mouseY / 80.0F);  // Same adjustment for the Y axis
        renderEntityInInventoryFollowsAngle(pGuiGraphics, pX, pY, pScale, f, f1, entity);
    }

    public static void renderEntityInInventoryFollowsAngle(GuiGraphics pGuiGraphics, int pX, int pY, int pScale, float angleXComponent, float angleYComponent, LivingEntity entity) {
        // Adjust the angles for more appropriate entity rotation
        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float)Math.PI);  // Rotate the entity upright
        Quaternionf quaternionf1 = (new Quaternionf()).rotateX(angleYComponent * 20.0F * 0.017453292F); // Apply X rotation (mouse Y)
        quaternionf.mul(quaternionf1);

        // Backup the entity's original rotations
        float originalBodyYaw = entity.yBodyRot;
        float originalYaw = entity.getYRot();
        float originalPitch = entity.getXRot();
        float originalHeadYawO = entity.yHeadRotO;
        float originalHeadYaw = entity.yHeadRot;

        // Set new rotations based on the mouse movement
        entity.yBodyRot = 180.0F + angleXComponent * 20.0F;  // Body yaw (side-to-side)
        entity.setYRot(180.0F + angleXComponent * 40.0F);    // Yaw (side-to-side)
        entity.setXRot(-angleYComponent * 20.0F);            // Pitch (up-down)
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();

        // Render the entity with the updated angles
        renderEntityInInventory(pGuiGraphics, pX, pY, pScale, quaternionf, quaternionf1, entity);

        // Restore the original rotations
        entity.yBodyRot = originalBodyYaw;
        entity.setYRot(originalYaw);
        entity.setXRot(originalPitch);
        entity.yHeadRotO = originalHeadYawO;
        entity.yHeadRot = originalHeadYaw;
    }

    public static void renderEntityInInventory(GuiGraphics pGuiGraphics, int pX, int pY, int pScale, Quaternionf rotation, @Nullable Quaternionf cameraOrientation, LivingEntity entity) {
        // Push the pose (matrix stack)
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().translate(pX, pY, 50.0);  // Position the entity on the screen
        pGuiGraphics.pose().mulPoseMatrix((new Matrix4f()).scaling(pScale, pScale, -pScale));  // Apply the scale

        // Apply the Z-axis rotation (flip entity upright)
        pGuiGraphics.pose().mulPose(rotation);

        // Prepare lighting for entity rendering
        Lighting.setupForEntityInInventory();

        // Get the entity renderer and render without shadows
        EntityRenderDispatcher entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher();
        if (cameraOrientation != null) {
            cameraOrientation.conjugate();
            entityRenderer.overrideCameraOrientation(cameraOrientation);
        }

        entityRenderer.setRenderShadow(false);  // Disable shadows for inventory rendering
        RenderSystem.runAsFancy(() -> {
            entityRenderer.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, pGuiGraphics.pose(), pGuiGraphics.bufferSource(), 15728880);
        });
        pGuiGraphics.flush();  // Ensure rendering is completed

        // Restore shadows and pop the pose stack
        entityRenderer.setRenderShadow(true);
        pGuiGraphics.pose().popPose();

        // Restore lighting for 3D items
        Lighting.setupFor3DItems();
    }


    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        super.slotClicked(slot, slotId, mouseButton, type);
    }


}

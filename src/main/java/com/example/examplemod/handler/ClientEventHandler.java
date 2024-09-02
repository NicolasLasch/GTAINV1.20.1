package com.example.examplemod.handler;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.network.OpenCustomContainerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = ExampleMod.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.keyInventory.consumeClick()) {
            if (mc.screen == null || mc.screen instanceof InventoryScreen) {
                // Close any currently open screen, if it's the inventory screen
                if (mc.screen != null) {
                    mc.player.closeContainer();
                }

                if (mc.player.isCreative()) {
                    // Only allow vanilla inventory screen for creative mode
                    mc.setScreen(new InventoryScreen(mc.player));
                } else {
                    // Send a packet to the server to open the custom container
                    NetworkHandler.INSTANCE.sendToServer(new OpenCustomContainerPacket());
                }
            }
        }
    }
}

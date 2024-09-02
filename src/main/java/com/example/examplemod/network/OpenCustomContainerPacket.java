package com.example.examplemod.network;

import com.example.examplemod.container.CustomContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenCustomContainerPacket {

    public OpenCustomContainerPacket() {
        // No data to serialize/deserialize
    }

    public OpenCustomContainerPacket(FriendlyByteBuf buf) {
        // Deserialize data from the buffer (not used here)
    }

    public void toBytes(FriendlyByteBuf buf) {
        // Serialize data to the buffer (not used here)
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                player.openMenu(new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return Component.literal("Backpack");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
                        return new CustomContainer(id, playerInventory);
                    }
                });
            }
        });
        context.get().setPacketHandled(true);
    }
}

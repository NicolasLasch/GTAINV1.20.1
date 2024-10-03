package com.example.examplemod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundSelectTradePacket {

    // Constructor
    public ServerboundSelectTradePacket() {
        // No data needed in this example
    }

    // Decode the packet from the buffer
    public ServerboundSelectTradePacket(FriendlyByteBuf buf) {
        // Read any data from buffer (if needed)
    }

    // Encode the packet to the buffer
    public void toBytes(FriendlyByteBuf buf) {
        // Write any data to buffer (if needed)
    }

    // Handle the packet when received
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Perform server-side actions when the packet is received
            // This is where you handle trade logic (e.g., showing nearby players)
        });
        context.setPacketHandled(true);
    }
}

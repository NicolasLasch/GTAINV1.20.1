package com.example.examplemod.network;

import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;

import java.util.Optional;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("examplemod", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void registerMessages() {
        int id = 0;
        INSTANCE.registerMessage(id++, OpenCustomContainerPacket.class, OpenCustomContainerPacket::toBytes, OpenCustomContainerPacket::new, OpenCustomContainerPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));

        INSTANCE.registerMessage(id++, ServerboundSelectTradePacket.class, ServerboundSelectTradePacket::toBytes, ServerboundSelectTradePacket::new, ServerboundSelectTradePacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));

        INSTANCE.registerMessage(id++, ServerboundTradePacket.class, ServerboundTradePacket::toBytes, ServerboundTradePacket::new, ServerboundTradePacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }
}

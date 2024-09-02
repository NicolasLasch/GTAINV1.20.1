package com.example.examplemod.command;

import com.example.examplemod.ExampleMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MOD_ID)
public class GameModeChanger {

    // Method to set the player's game mode to Creative
    public static void setPlayerToCreative(ServerPlayer player) {
        player.setGameMode(GameType.CREATIVE);
    }

    public static void setPlayerToSurvival(ServerPlayer player) {
        player.setGameMode(GameType.SURVIVAL);
    }

    // Example usage: Set the player to Creative mode when they join the game
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
    }
}

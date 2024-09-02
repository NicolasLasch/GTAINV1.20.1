package com.example.examplemod;

import com.example.examplemod.container.CustomContainer;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.screen.CustomScreen;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType.MenuSupplier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mod(ExampleMod.MOD_ID)
public class ExampleMod {
    public static final String MOD_ID = "examplemod";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final Type ITEM_WEIGHT_MAP_TYPE = new TypeToken<Map<String, Double>>() {}.getType();
    public static Map<ResourceLocation, Double> itemWeights = new HashMap<>();

    // Registering the MenuType
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MOD_ID);
    public static final RegistryObject<MenuType<CustomContainer>> CUSTOM_CONTAINER = MENUS.register("custom_container",
            () -> new MenuType<>(CustomContainer::new, FeatureFlagSet.of(FeatureFlags.VANILLA)));

    public ExampleMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register event listeners
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::addCreativeContents);

        // Registering the MenuType
        MENUS.register(modEventBus);
        NetworkHandler.registerMessages();
        // Registering the mod to the event bus
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Common setup complete.");
    }


    private void clientSetup(final FMLClientSetupEvent event) {
        // Register the screen for our custom container
        loadItemWeights();
        MenuScreens.register(CUSTOM_CONTAINER.get(), CustomScreen::new);
    }

    private void loadItemWeights() {
        ResourceLocation itemWeightsLocation = new ResourceLocation(MOD_ID, "item_weights.json");
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

        try {
            Optional<Resource> resourceOptional = resourceManager.getResource(itemWeightsLocation);
            if (resourceOptional.isPresent()) {
                try (InputStreamReader reader = new InputStreamReader(resourceOptional.get().open())) {
                    Map<String, Double> loadedWeights = GSON.fromJson(reader, ITEM_WEIGHT_MAP_TYPE);
                    if (loadedWeights != null) {
                        loadedWeights.forEach((itemId, weight) -> itemWeights.put(new ResourceLocation(itemId), weight));
                    }
                }
            } else {
                System.err.println("Resource not found: " + itemWeightsLocation);
            }
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
        }

    }

    public static double getItemWeight(Item item) {
        if (item == null) {
            return 0.01;
        }

        String itemName = item.getDescriptionId();
        return itemWeights.getOrDefault(itemName, 0.01);
    }

    private void addCreativeContents(final BuildCreativeModeTabContentsEvent event) {
        // Add items to creative mode tabs (if needed)
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Server starting event triggered.");
    }
}

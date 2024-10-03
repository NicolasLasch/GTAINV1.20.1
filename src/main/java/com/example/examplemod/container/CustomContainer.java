package com.example.examplemod.container;

import com.example.examplemod.container.slot.TradeSlot;
import com.example.examplemod.custom.ArmorSlot;
import com.example.examplemod.filter.FilterManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import com.example.examplemod.ExampleMod;

import java.util.ArrayList;
import java.util.List;

public class CustomContainer extends AbstractContainerMenu {

    private final Inventory playerInventory;

    public CustomContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory);
    }

    public CustomContainer(int windowId, Inventory playerInventory) {
        super(ExampleMod.CUSTOM_CONTAINER.get(), windowId);
        this.playerInventory = playerInventory;

        this.addSlot(new TradeSlot(playerInventory, 27, 335, 340));

        List<Integer> filledSlots = new ArrayList<>();
        int mode = FilterManager.getFilterMode();
        switch (mode) {
            case 1:
                for (int i = 9; i < 29; i++) {
                    if (!playerInventory.getItem(i).isEmpty() && playerInventory.getItem(i).isEdible()) {
                        filledSlots.add(i);
                    }
                }
                break;
            case 2:
                for (int i = 9; i < 29; i++) {
                    if (!playerInventory.getItem(i).isEmpty() && playerInventory.getItem(i).getItem() instanceof ArmorItem) {
                        filledSlots.add(i);
                    }
                }
                break;
            default:
                for (int i = 9; i < 29; i++) {
                    if (!playerInventory.getItem(i).isEmpty()) {
                        filledSlots.add(i);}
                }
                break;
        }

        int nextEmptySlot = getNextEmptySlotIndex(playerInventory);
        if (nextEmptySlot >= 0 && nextEmptySlot < 29 && !filledSlots.contains(nextEmptySlot)) {
            filledSlots.add(nextEmptySlot);
        }
        this.addSlot(new ArmorSlot(playerInventory, 39, 270, 96, EquipmentSlot.HEAD));
        this.addSlot(new ArmorSlot(playerInventory, 38, 270, 132, EquipmentSlot.CHEST));
        this.addSlot(new ArmorSlot(playerInventory, 37, 270, 168, EquipmentSlot.LEGS));
        this.addSlot(new ArmorSlot(playerInventory, 36, 270, 204, EquipmentSlot.FEET));

        int totalSlots = 20; // Total number of slots you want to handle
        int filledSlotCount = filledSlots.size(); // Number of filled slots
        int filledSlotIndex = 0; // Index to track the current filled slot

        for (int slotId = 0; slotId < totalSlots; ++slotId) {
            int x, y;

            // Check if the current slot ID is in filled slots
            if (filledSlotIndex < filledSlotCount && filledSlots.contains(slotId)) {
                // If it's a filled slot, calculate its position
                int i = filledSlotIndex / 5; // Calculate row index (0 to 3 for 20 slots)
                int j = filledSlotIndex % 5; // Calculate column index (0 to 4 for 5 columns)

                x = 20 + j * 36; // Calculate x position
                y = 96 + i * 36; // Calculate y position

                this.addSlot(new Slot(playerInventory, slotId, x, y)); // Add filled slot

                filledSlotIndex++; // Move to the next filled slot index
            } else {
                // For empty slots, place them at (0, 0)
                x = -1000;
                y = -1000;
                // Here, you can decide whether to add an empty slot visually or just skip it.
                // If you want to add a visual representation of an empty slot:
                this.addSlot(new Slot(playerInventory, slotId, x, y)); // -1 or another value to indicate an empty slot
            }
        }

        /*
        for (int i = 9; i < 29; i++) {
            if (disableSlots.contains(i)) {
                this.addSlot(new Slot(playerInventory, i, i * 40, 0));
            }
        }
        */

        // Add hotbar slots (0-8)

        for (int k = 0; k < 5; ++k) {
            int x = 20 + k * 36;
            int y = 260;
            this.addSlot(new Slot(playerInventory, k, x, y));
        }
    }

    private int getNextEmptySlotIndex(Inventory playerInventory) {
        for (int i = 9; i < 29; i++) {
            if (playerInventory.getItem(i).isEmpty()) {
                return i;
            }
        }
        return 100;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack newStack = slot.getItem();
            ItemStack originalStack = newStack.copy();
            int containerSize = this.playerInventory.getContainerSize();

            if (index < containerSize) {
                // Moving item from player inventory to container
                if (!this.moveItemStackTo(newStack, containerSize, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving item from container to player inventory
                if (!this.moveItemStackTo(newStack, 0, containerSize, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (newStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (newStack.getCount() == originalStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, newStack);
        }

        return ItemStack.EMPTY;
    }
}

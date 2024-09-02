package com.example.examplemod.container;

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
        List<Integer> filledSlots = new ArrayList<>();
        int mode = FilterManager.getFilterMode();
        switch (mode) {
            case 1:
                for (int i = 9; i < 29; i++) {
                    if (!playerInventory.getItem(i).isEmpty() && playerInventory.getItem(i).isEdible()) {
                        filledSlots.add(i);
                        System.out.println(i);
                    }
                }
                break;
            case 2:
                for (int i = 9; i < 29; i++) {
                    if (!playerInventory.getItem(i).isEmpty() && playerInventory.getItem(i).getItem() instanceof ArmorItem) {
                        filledSlots.add(i);
                        System.out.println(i);
                    }
                }
                break;
            default:
                for (int i = 9; i < 29; i++) {
                    if (!playerInventory.getItem(i).isEmpty()) {
                        filledSlots.add(i);
                    }
                }
                break;
        }

        filledSlots.add(getNextEmptySlotIndex(playerInventory));
        System.out.println(getNextEmptySlotIndex(playerInventory));
        System.out.println("INV ITEM SELECTED");
        this.addSlot(new ArmorSlot(playerInventory, 39, 270, 96, EquipmentSlot.HEAD));
        this.addSlot(new ArmorSlot(playerInventory, 38, 270, 132, EquipmentSlot.CHEST));
        this.addSlot(new ArmorSlot(playerInventory, 37, 270, 168, EquipmentSlot.LEGS));
        this.addSlot(new ArmorSlot(playerInventory, 36, 270, 204, EquipmentSlot.FEET));

        int slot = 0;
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 5; ++j) {
                if (slot < filledSlots.size()){
                    int x = 20 + j * 36;
                    int y = 96 +  i * 36;
                    this.addSlot(new Slot(playerInventory, filledSlots.get(slot), x, y));
                    System.out.println(filledSlots.get(slot));
                    slot++;
                    }
            }
        }

        for (int i = 9; i < 29; i++) {
            if (!filledSlots.contains(i)) {
                this.addSlot(new Slot(playerInventory, i, -1000, -1000));
            }
        }

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
        return 36;
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

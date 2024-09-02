package com.example.examplemod.container;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import com.example.examplemod.ExampleMod;
import com.example.examplemod.filter.FilterManager;

import java.util.ArrayList;
import java.util.List;

public class OldCustomContainer extends AbstractContainerMenu {

    private double totalWeight;
    private ItemStack carriedStack = ItemStack.EMPTY;

    // Constructor used by the MenuType registration (from the client)
    public OldCustomContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory);
    }

    private final Inventory playerInventory;

    // Constructor used by the MenuType registration (from the server)
    public OldCustomContainer(int windowId, Inventory playerInventory) {
        super(ExampleMod.CUSTOM_CONTAINER.get(), windowId);
        this.playerInventory = playerInventory;
        List<Slot> filledSlots = new ArrayList<>();
        int mode = FilterManager.getFilterMode();
        switch (mode) {
            case 1:
                for (int i = 9; i < 29; i++) {
                    if (!playerInventory.getItem(i).isEmpty() && playerInventory.getItem(i).isEdible()) {
                        filledSlots.add(new Slot(playerInventory, i, 0, 0));
                    }
                }
                break;
            case 2:
                for (int i = 9; i < 29; i++) {
                    if (!playerInventory.getItem(i).isEmpty() && playerInventory.getItem(i).getItem() instanceof ArmorItem) {
                        filledSlots.add(new Slot(playerInventory, i, 0, 0));
                    }
                }
                break;
            default:
                for (int i = 9; i < 29; i++) {
                    if (!playerInventory.getItem(i).isEmpty()) {
                        filledSlots.add(new Slot(playerInventory, i, 0, 0));
                    }
                }
                break;
        }

        filledSlots.add(new Slot(playerInventory, getNextEmptySlotIndex(playerInventory), 0, 0));

        int numRows = 4;
        int numCols = 5;
        int slotIndex = 0;

        for (int row = 0; row < numRows; ++row) {
            for (int col = 0; col < numCols; ++col) {
                if (slotIndex < filledSlots.size()) {
                    int x = 20 + col * 36;
                    int y = 96 + row * 36;
                    Slot slot = filledSlots.get(slotIndex);
                    this.addSlot(new Slot(playerInventory, slot.getContainerSlot(), x, y));
                    slotIndex++;
                }
            }
        }

        for (int col = 0; col < 5; ++col) {
            int x = 20 + col * 36;
            int y = 260;
            this.addSlot(new Slot(playerInventory, col, x, y));
        }

        this.addArmorSlots(playerInventory);
        calculateTotalWeight();
    }

    private int getNextEmptySlotIndex(Inventory playerInventory) {
        for (int i = 9; i < 29; i++) {
            if (playerInventory.getItem(i).isEmpty()) {
                return i;
            }
        }
        return 36;
    }

    private void calculateTotalWeight() {
        totalWeight = 0;
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack stack = playerInventory.getItem(i);
            if (!stack.isEmpty()) {
                totalWeight += ExampleMod.getItemWeight(stack.getItem()) * stack.getCount();
            }
        }
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    private void addArmorSlots(Inventory playerInventory) {
        this.addSlot(new Slot(playerInventory, 39, 270, 96) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.canEquip(net.minecraft.world.entity.EquipmentSlot.HEAD, playerInventory.player);
            }
        });

        this.addSlot(new Slot(playerInventory, 38, 270, 132) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.canEquip(net.minecraft.world.entity.EquipmentSlot.CHEST, playerInventory.player);
            }
        });

        this.addSlot(new Slot(playerInventory, 37, 270, 168) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.canEquip(net.minecraft.world.entity.EquipmentSlot.LEGS, playerInventory.player);
            }
        });

        this.addSlot(new Slot(playerInventory, 36, 270, 204) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.canEquip(net.minecraft.world.entity.EquipmentSlot.FEET, playerInventory.player);
            }
        });
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

            if (index < this.playerInventory.getContainerSize()) {
                if (!this.moveItemStackTo(newStack, this.playerInventory.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(newStack, 0, this.playerInventory.getContainerSize(), false)) {
                return ItemStack.EMPTY;
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

    // New Methods: Handling drag-and-drop functionality server-side

    public void pickUpItem(int slotIndex) {
        Slot slot = this.getSlot(slotIndex);
        if (slot != null && slot.hasItem()) {
            this.carriedStack = slot.getItem().copy(); // Save the item to be carried
            slot.set(ItemStack.EMPTY); // Remove the item from the slot
            slot.setChanged();
        }
    }

    public void placeItem(int slotIndex) {
        Slot slot = this.getSlot(slotIndex);
        if (slot != null) {
            if (!this.carriedStack.isEmpty()) {
                ItemStack currentStack = slot.getItem();

                if (currentStack.isEmpty()) {
                    slot.set(this.carriedStack.copy());
                    this.carriedStack = ItemStack.EMPTY;
                } else if (ItemStack.isSameItemSameTags(currentStack, this.carriedStack)) {
                    int newCount = currentStack.getCount() + this.carriedStack.getCount();
                    int maxStackSize = Math.min(currentStack.getMaxStackSize(), slot.getMaxStackSize());
                    if (newCount <= maxStackSize) {
                        currentStack.setCount(newCount);
                        this.carriedStack = ItemStack.EMPTY;
                    } else {
                        currentStack.setCount(maxStackSize);
                        this.carriedStack.setCount(newCount - maxStackSize);
                    }
                }

                slot.setChanged();
                this.broadcastChanges();
            }
        }
    }

    public void swapItems(int slotIndex1, int slotIndex2) {
        Slot slot1 = this.getSlot(slotIndex1);
        Slot slot2 = this.getSlot(slotIndex2);
        if (slot1 != null && slot2 != null) {
            ItemStack stack1 = slot1.getItem().copy();
            ItemStack stack2 = slot2.getItem().copy();

            slot1.set(stack2);
            slot2.set(stack1);

            slot1.setChanged();
            slot2.setChanged();
            this.broadcastChanges();
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
    }
}

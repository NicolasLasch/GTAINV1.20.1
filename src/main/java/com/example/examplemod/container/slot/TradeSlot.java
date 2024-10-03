package com.example.examplemod.container.slot;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TradeSlot extends Slot {

    public TradeSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // Customize which items can be placed in the trade slot, if needed
        return true;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        // This can be used for additional logic when an item is taken from the slot
    }

    @Override
    public boolean mayPickup(Player player) {
        return true; // Allow player to pick the item back up
    }
}

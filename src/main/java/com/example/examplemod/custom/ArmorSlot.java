package com.example.examplemod.custom;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ArmorSlot extends Slot {
    private final EquipmentSlot equipmentSlot;

    public ArmorSlot(Inventory inventory, int index, int x, int y, EquipmentSlot equipmentSlot) {
        super(inventory, index, x, y);
        this.equipmentSlot = equipmentSlot;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        switch (this.equipmentSlot) {
            case HEAD:
                return stack.getItem() == Items.LEATHER_HELMET ||
                        stack.getItem() == Items.IRON_HELMET ||
                        stack.getItem() == Items.GOLDEN_HELMET ||
                        stack.getItem() == Items.DIAMOND_HELMET ||
                        stack.getItem() == Items.NETHERITE_HELMET ||
                        stack.getItem() == Items.TURTLE_HELMET;

            case CHEST:
                return stack.getItem() == Items.LEATHER_CHESTPLATE ||
                        stack.getItem() == Items.IRON_CHESTPLATE ||
                        stack.getItem() == Items.GOLDEN_CHESTPLATE ||
                        stack.getItem() == Items.DIAMOND_CHESTPLATE ||
                        stack.getItem() == Items.NETHERITE_CHESTPLATE;

            case LEGS:
                return stack.getItem() == Items.LEATHER_LEGGINGS ||
                        stack.getItem() == Items.IRON_LEGGINGS ||
                        stack.getItem() == Items.GOLDEN_LEGGINGS ||
                        stack.getItem() == Items.DIAMOND_LEGGINGS ||
                        stack.getItem() == Items.NETHERITE_LEGGINGS;

            case FEET:
                return stack.getItem() == Items.LEATHER_BOOTS ||
                        stack.getItem() == Items.IRON_BOOTS ||
                        stack.getItem() == Items.GOLDEN_BOOTS ||
                        stack.getItem() == Items.DIAMOND_BOOTS ||
                        stack.getItem() == Items.NETHERITE_BOOTS;

            default:
                return false;
        }
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1; // Armor items can only be placed one at a time
    }
}

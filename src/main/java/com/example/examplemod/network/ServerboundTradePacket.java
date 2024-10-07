package com.example.examplemod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ServerboundTradePacket {
    private final UUID recipientId;
    private final ItemStack item;
    private final int amount;

    public ServerboundTradePacket(UUID recipientId, ItemStack item, int amount) {
        this.recipientId = recipientId;
        this.item = item.copy();
        this.amount = amount;
    }

    public ServerboundTradePacket(FriendlyByteBuf buf) {
        this.recipientId = buf.readUUID();
        this.item = buf.readItem();
        this.amount = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(recipientId);
        buf.writeItem(item);
        buf.writeInt(amount);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Player player = context.getSender();
            Player recipient = player.level().getPlayerByUUID(recipientId);

            if (recipient != null && amount > 0 && item.getCount() >= amount) {
                ItemStack tradeItem = new ItemStack(item.getItem(), amount);

                boolean hasSpace = false;
                for (int i = 0; i < 36; i++) {
                    ItemStack slotItem = recipient.getInventory().getItem(i);
                    if (slotItem.isEmpty() || (slotItem.getItem() == tradeItem.getItem() && slotItem.getCount() + tradeItem.getCount() <= tradeItem.getMaxStackSize())) {
                        hasSpace = true;
                        break;
                    }
                }
                if (hasSpace) {
                    ItemStack senderItem = player.getInventory().getItem(27);
                    System.out.println("Test item : " + tradeItem.getItem());
                    System.out.println(tradeItem.getItem());
                    System.out.println(senderItem.getItem());
                    if (senderItem.getItem() == tradeItem.getItem()) {
                        if (senderItem.getCount() >= amount) {
                            senderItem.shrink(amount);
                            recipient.addItem(tradeItem);
                        } else {
                            player.sendSystemMessage(Component.literal("You do not have enough items to trade."));
                        }
                    } else {
                        player.sendSystemMessage(Component.literal("You are not holding the correct item to trade."));
                    }
                } else {
                    player.sendSystemMessage(Component.literal("Recipient does not have enough space for the item."));
                }
            }
        });
        context.setPacketHandled(true);
    }
}

package io.github.sefiraat.networks.network.stackcaches;

import javax.annotation.Nonnull;

import org.bukkit.inventory.ItemStack;

import lombok.Getter;

@Getter
public class ItemRequest extends ItemStackCache {

    private int amount;

    public ItemRequest(@Nonnull ItemStack itemStack, int amount) {
        super(itemStack);
        this.amount = amount;
    }


    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void receiveAmount(int amount) {
        this.amount = this.amount - amount;
    }

    public String toString() {
        return "ItemRequest{" +
                "itemStack=" + getItemStack() +
                ", amount=" + amount +
                '}';
    }
}

package com.balugaq.netex.api.data;


import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;
import io.github.sefiraat.networks.utils.StackUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ytdd9527.networksexpansion.utils.itemstacks.ItemStackUtil;

import io.github.sefiraat.networks.utils.StackUtils;

/**
 * @author Final_ROOT
 * @since 2.0
 */
@Setter
@Getter
public class ItemAmountWrapper extends ItemWrapper {
    private int amount;

    public ItemAmountWrapper() {
        super();
        this.amount = 0;
    }

    public ItemAmountWrapper(@Nonnull ItemStack itemStack) {
        super(itemStack);
        this.amount = itemStack.getAmount();
    }

    public ItemAmountWrapper(@Nonnull ItemStack itemStack, int amount) {
        super(itemStack);
        this.amount = amount;
    }

    public ItemAmountWrapper(@Nonnull ItemStack itemStack, @Nullable ItemMeta itemMeta) {
        super(itemStack, itemMeta);
        this.amount = itemStack.getAmount();
    }

    public ItemAmountWrapper(@Nonnull ItemStack itemStack, @Nullable ItemMeta itemMeta, int amount) {
        super(itemStack, itemMeta);
        this.amount = amount;
    }

    public ItemAmountWrapper(@Nonnull ItemWrapper itemWrapper) {
        super(itemWrapper.getItemStack(), itemWrapper.getItemMeta());
        this.amount = itemWrapper.getItemStack().getAmount();
    }

    public ItemAmountWrapper(@Nonnull ItemWrapper itemWrapper, int amount) {
        super(itemWrapper.getItemStack(), itemWrapper.getItemMeta());
        this.amount = amount;
    }

    public static void addToList(@Nonnull List<ItemAmountWrapper> list, @Nonnull ItemAmountWrapper item) {
        for (ItemAmountWrapper existedItem : list) {
            if (StackUtils.itemsMatch(existedItem.getItemStack(), item.getItemStack())) {
                existedItem.addAmount(item.amount);
                return;
            }
        }
        list.add(item.shallowClone());
    }

    public static void addToList(@Nonnull List<ItemAmountWrapper> list, @Nonnull ItemAmountWrapper item, int mul) {
        for (ItemAmountWrapper existedItem : list) {
            if (StackUtils.itemsMatch(existedItem.getItemStack(), item.getItemStack())) {
                existedItem.addAmount(item.amount * mul);
                return;
            }
        }
        list.add(new ItemAmountWrapper(item.getItemStack(), item.getItemMeta(), item.amount * mul));
    }

    public void addAmount(int amount) {
        this.amount += amount;
    }

    public void subAmount(int amount) {
        this.amount -= amount;
    }

    @Override
    public void newWrap(@Nonnull ItemStack itemStack) {
        super.newWrap(itemStack);
        this.amount = itemStack.getAmount();
    }

    public void newWrap(@Nonnull ItemStack itemStack, int amount) {
        super.newWrap(itemStack);
        this.amount = amount;
    }

    @Override
    public void newWrap(@Nonnull ItemStack itemStack, @Nullable ItemMeta itemMeta) {
        super.newWrap(itemStack, itemMeta);
        this.amount = itemStack.getAmount();
    }

    public void newWrap(@Nonnull ItemStack itemStack, @Nullable ItemMeta itemMeta, int amount) {
        super.newWrap(itemStack, itemMeta);
        this.amount = amount;
    }

    @Nonnull
    @Override
    public ItemAmountWrapper shallowClone() {
        return new ItemAmountWrapper(this.getItemStack(), this.getItemMeta(), this.amount);
    }

    @Nonnull
    @Override
    public ItemAmountWrapper deepClone() {
        ItemStack itemStack = ItemStackUtil.cloneItem(this.getItemStack());
        itemStack.setAmount(this.getItemStack().getAmount());
        return new ItemAmountWrapper(ItemStackUtil.cloneItem(this.getItemStack()), this.amount);
    }

    @Override
    public int hashCode() {
        int hash = 31 + this.getItemStack().getType().hashCode();
        hash = hash * 31 + this.amount;
        hash = hash * 31 + (this.getItemStack().getDurability() & 0xffff);
        hash = hash * 31 + (this.getItemMeta() != null ? (this.getItemMeta().hashCode()) : 0);
        return hash;
    }
}

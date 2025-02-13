package io.github.sefiraat.networks.slimefun.groups;

import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;

public class DummyItemGroup extends ItemGroup {

    @ParametersAreNonnullByDefault
    public DummyItemGroup(NamespacedKey key, ItemStack item, int tier) {
        super(key, item, tier);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean isHidden(Player p) {
        return true;
    }

}

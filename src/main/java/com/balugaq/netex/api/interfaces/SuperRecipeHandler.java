package com.balugaq.netex.api.interfaces;

import org.bukkit.entity.Player;

import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;

public interface SuperRecipeHandler {
    boolean handle(Player player, BlockMenu blockMenu);
}

package com.balugaq.netex.api.interfaces;

import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.entity.Player;

public interface SuperRecipeHandler {
    boolean handle(Player player, BlockMenu blockMenu);
}

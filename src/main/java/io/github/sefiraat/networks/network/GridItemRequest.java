package io.github.sefiraat.networks.network;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.sefiraat.networks.network.stackcaches.ItemRequest;

public class GridItemRequest extends ItemRequest {

    private final Player player;

    public GridItemRequest(ItemStack itemStack, int amount, Player player) {
        super(itemStack, amount);
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}

package io.github.sefiraat.networks.slimefun.tools;

import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.utils.StackUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public interface CanCooldown {

    /**
     * The duration, in seconds, this item will go on cooldown for
     *
     * @return The cooldown duration in seconds
     */
    int cooldownDuration();

    @ParametersAreNonnullByDefault
    default boolean canBeUsed(ItemStack itemStack) {
        return canBeUsed(null, itemStack);
    }

    @ParametersAreNonnullByDefault
    default boolean canBeUsed(@Nullable Player player, ItemStack itemStack) {
        if (StackUtils.isOnCooldown(itemStack)) {
            if (player != null) {
                player.sendMessage(Networks.getLocalizationService().getString("messages.unsupported-operation.can_cooldown"));
            }
            return false;
        } else {
            return true;
        }
    }

    @ParametersAreNonnullByDefault
    default void putOnCooldown(ItemStack itemStack) {
        StackUtils.putOnCooldown(itemStack, this.cooldownDuration());
    }
}

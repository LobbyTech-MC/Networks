package io.github.sefiraat.networks.slimefun.network;

import java.util.List;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.helpers.Icon;
import com.balugaq.netex.utils.NetworksVersionedParticle;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;

import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.slimefun.NetworkSlimefunItems;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSetting;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.settings.IntRangeSetting;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;

public class NetworkPurger extends NetworkObject {

    private static final int[] BACKGROUND_SLOTS = {0, 1, 2, 6, 7, 8, 9, 10, 11, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
    private static final int TEST_ITEM_SLOT = 13;
    private static final int[] TEST_ITEM_BACKDROP = {3, 4, 5, 12, 14, 21, 22, 23};

    private final ItemSetting<Integer> tickRate;

    public NetworkPurger(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe, NodeType.PURGER);
        this.tickRate = new IntRangeSetting(this, "tick_rate", 1, 1, 10);
        addItemSetting(this.tickRate);

        this.getSlotsToDrop().add(TEST_ITEM_SLOT);

        addItemHandler(
                new BlockTicker() {

                    private int tick = 1;

                    @Override
                    public boolean isSynchronized() {
                        return false;
                    }

                    @Override
                    public void tick(Block block, SlimefunItem item, SlimefunBlockData data) {
                        if (tick <= 1) {
                            addToRegistry(block);
                            tryKillItem(data.getBlockMenu());
                        }
                    }

                    @Override
                    public void uniqueTick() {
                        tick = tick <= 1 ? tickRate.getValue() : tick - 1;
                    }
                },
                new BlockBreakHandler(true, true) {
                    @Override
                    public void onPlayerBreak(@Nonnull BlockBreakEvent e, @Nonnull ItemStack item, @Nonnull List<ItemStack> drops) {
                        BlockMenu blockMenu = StorageCacheUtils.getMenu(e.getBlock().getLocation());
                        blockMenu.dropItems(blockMenu.getLocation(), TEST_ITEM_SLOT);
                    }
                }
        );
    }

    private void tryKillItem(@Nonnull BlockMenu blockMenu) {
        final NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());

        if (definition == null || definition.getNode() == null) {
            sendFeedback(blockMenu.getLocation(), FeedbackType.NO_NETWORK_FOUND);
            return;
        }

        ItemStack testItem = blockMenu.getItemInSlot(TEST_ITEM_SLOT);

        if (testItem == null || testItem.getType() == Material.AIR) {
            sendFeedback(blockMenu.getLocation(), FeedbackType.NO_ITEM_FOUND);
            return;
        }
        ItemStack clone = StackUtils.getAsQuantity(testItem, 1);

        ItemRequest itemRequest = new ItemRequest(clone, clone.getMaxStackSize());
        ItemStack retrieved = definition.getNode().getRoot().getItemStack0(blockMenu.getLocation(), itemRequest);
        if (retrieved != null) {
            retrieved.setAmount(0);
            sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
            Location location = blockMenu.getLocation().clone().add(0.5, 1.2, 0.5);
            if (definition.getNode().getRoot().isDisplayParticles()) {
                location.getWorld().spawnParticle(NetworksVersionedParticle.SMOKE, location, 0, 0, 0.05, 0);
            }
        }
    }

    @Override
    public void postRegister() {
        new BlockMenuPreset(this.getId(), this.getItemName()) {

            @Override
            public void init() {
                drawBackground(BACKGROUND_SLOTS);
                drawBackground(Icon.PURGER_TEMPLATE_BACKGROUND_STACK, TEST_ITEM_BACKDROP);
            }

            @Override
            public boolean canOpen(@Nonnull Block block, @Nonnull Player player) {
                return player.hasPermission("slimefun.inventory.bypass") || (NetworkSlimefunItems.NETWORK_PURGER.canUse(player, false)
                        && Slimefun.getProtectionManager().hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK));
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                return new int[0];
            }
        };
    }
}

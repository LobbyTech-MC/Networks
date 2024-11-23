package com.balugaq.netex.utils;

import com.balugaq.netex.api.enums.TransportMode;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import lombok.experimental.UtilityClass;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

@UtilityClass
public class LineOperationUtil {
    public static void doOperation(Location startLocation, BlockFace direction, int limit, Consumer<BlockMenu> consumer) {
        doOperation(startLocation, direction, limit, false, true, consumer);
    }

    public static void doOperation(Location startLocation, BlockFace direction, int limit, boolean skipNoMenu, Consumer<BlockMenu> consumer) {
        doOperation(startLocation, direction, limit, skipNoMenu, true, consumer);
    }

    public static void doOperation(Location startLocation, BlockFace direction, int limit, boolean skipNoMenu, boolean optimizeExperience, Consumer<BlockMenu> consumer) {
        Location location = startLocation.clone();
        int finalLimit = limit;
        if (optimizeExperience) {
            finalLimit += 1;
        }
        for (int i = 0; i < finalLimit; i++) {
            switch (direction) {
                case NORTH -> location.setZ(location.getZ() - 1);
                case SOUTH -> location.setZ(location.getZ() + 1);
                case EAST -> location.setX(location.getX() + 1);
                case WEST -> location.setX(location.getX() - 1);
                case UP -> location.setY(location.getY() + 1);
                case DOWN -> location.setY(location.getY() - 1);
            }
            final BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
            if (blockMenu == null) {
                if (skipNoMenu) {
                    continue;
                } else {
                    return;
                }
            }
            consumer.accept(blockMenu);
        }
    }

    public static void doEnergyOperation(Location startLocation, BlockFace direction, int limit, Consumer<BlockMenu> consumer) {
        doEnergyOperation(startLocation, direction, limit, true, true, consumer);
    }

    public static void doEnergyOperation(Location startLocation, BlockFace direction, int limit, boolean allowNoMenu, Consumer<BlockMenu> consumer) {
        doEnergyOperation(startLocation, direction, limit, allowNoMenu, true, consumer);
    }

    public static void doEnergyOperation(Location startLocation, BlockFace direction, int limit, boolean allowNoMenu, boolean optimizeExperience, Consumer<BlockMenu> consumer) {
        Location location = startLocation.clone();
        int finalLimit = limit;
        if (optimizeExperience) {
            finalLimit += 1;
        }
        for (int i = 0; i < finalLimit; i++) {
            switch (direction) {
                case NORTH -> location.setZ(location.getZ() - 1);
                case SOUTH -> location.setZ(location.getZ() + 1);
                case EAST -> location.setX(location.getX() + 1);
                case WEST -> location.setX(location.getX() - 1);
                case UP -> location.setY(location.getY() + 1);
                case DOWN -> location.setY(location.getY() - 1);
            }
            final BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
            if (blockMenu == null) {
                if (!allowNoMenu) {
                    return;
                }
            }
            consumer.accept(blockMenu);
        }
    }

    public static void grabItem(
            @Nonnull NetworkRoot root,
            @Nonnull BlockMenu blockMenu,
            @Nonnull TransportMode transportMode,
            int limitQuantity
    ) {
        final int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(blockMenu, ItemTransportFlow.WITHDRAW, null);

        int limit = limitQuantity;
        switch (transportMode) {
            case NONE, NONNULL_ONLY -> {
                /*
                 * Grab all the items.
                 */
                for (int slot : slots) {
                    final ItemStack item = blockMenu.getItemInSlot(slot);
                    if (item != null && item.getType() != Material.AIR) {
                        final int exceptedReceive = Math.min(item.getAmount(), limit);
                        final ItemStack clone = StackUtils.getAsQuantity(item, exceptedReceive);
                        root.addItemStack(clone);
                        item.setAmount(item.getAmount() - (exceptedReceive - clone.getAmount()));
                        limit -= exceptedReceive - clone.getAmount();
                        if (limit <= 0) {
                            break;
                        }
                    }
                }
            }
            case NULL_ONLY -> {
                /*
                 * Nothing to do.
                 */
            }
            case FIRST_ONLY -> {
                /*
                 * Grab the first item only.
                 */
                if (slots.length > 0) {
                    final ItemStack item = blockMenu.getItemInSlot(slots[0]);
                    if (item != null && item.getType() != Material.AIR) {
                        final int exceptedReceive = Math.min(item.getAmount(), limit);
                        final ItemStack clone = StackUtils.getAsQuantity(item, exceptedReceive);
                        root.addItemStack(clone);
                        item.setAmount(item.getAmount() - (exceptedReceive - clone.getAmount()));
                        limit -= exceptedReceive - clone.getAmount();
                        if (limit <= 0) {
                            break;
                        }
                    }
                }
            }
            case LAST_ONLY -> {
                /*
                 * Grab the last item only.
                 */
                if (slots.length > 0) {
                    final ItemStack item = blockMenu.getItemInSlot(slots[slots.length - 1]);
                    if (item != null && item.getType() != Material.AIR) {
                        final int exceptedReceive = Math.min(item.getAmount(), limit);
                        final ItemStack clone = StackUtils.getAsQuantity(item, exceptedReceive);
                        root.addItemStack(clone);
                        item.setAmount(item.getAmount() - (exceptedReceive - clone.getAmount()));
                        limit -= exceptedReceive - clone.getAmount();
                        if (limit <= 0) {
                            break;
                        }
                    }
                }
            }
            case FIRST_STOP -> {
                /*
                 * Grab the first non-null item only.
                 */
                for (int slot : slots) {
                    final ItemStack item = blockMenu.getItemInSlot(slot);
                    if (item != null && item.getType() != Material.AIR) {
                        final int exceptedReceive = Math.min(item.getAmount(), limit);
                        final ItemStack clone = StackUtils.getAsQuantity(item, exceptedReceive);
                        root.addItemStack(clone);
                        item.setAmount(item.getAmount() - (exceptedReceive - clone.getAmount()));
                        limit -= exceptedReceive - clone.getAmount();
                        break;
                    }
                }
            }
            case LAZY -> {
                /*
                 * When it's first item is non-null, we will grab all the items.
                 */
                if (slots.length > 0) {
                    final ItemStack delta = blockMenu.getItemInSlot(slots[0]);
                    if (delta != null && delta.getType() != Material.AIR) {
                        for (int slot : slots) {
                            ItemStack item = blockMenu.getItemInSlot(slot);
                            if (item != null && item.getType() != Material.AIR) {
                                final int exceptedReceive = Math.min(item.getAmount(), limit);
                                final ItemStack clone = StackUtils.getAsQuantity(item, exceptedReceive);
                                root.addItemStack(clone);
                                item.setAmount(item.getAmount() - (exceptedReceive - clone.getAmount()));
                                limit -= exceptedReceive - clone.getAmount();
                                if (limit <= 0) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return;
    }

    public static void pushItem(
            @Nonnull NetworkRoot root,
            @Nonnull BlockMenu blockMenu,
            @Nonnull List<ItemStack> clones,
            @Nonnull TransportMode transportMode,
            int limitQuantity
    ) {
        for (ItemStack clone : clones) {
            pushItem(root, blockMenu, clone, transportMode, limitQuantity);
        }
    }

    public static void pushItem(
            @Nonnull NetworkRoot root,
            @Nonnull BlockMenu blockMenu,
            @Nonnull ItemStack clone,
            @Nonnull TransportMode transportMode,
            int limitQuantity
    ) {
        final ItemRequest itemRequest = new ItemRequest(clone, clone.getMaxStackSize());

        final int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(blockMenu, ItemTransportFlow.INSERT, clone);
        switch (transportMode) {
            case NONE -> {
                int freeSpace = 0;
                for (int slot : slots) {
                    final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                    if (itemStack == null || itemStack.getType() == Material.AIR) {
                        freeSpace += clone.getMaxStackSize();
                    } else {
                        if (itemStack.getAmount() >= clone.getMaxStackSize()) {
                            continue;
                        }
                        if (StackUtils.itemsMatch(itemRequest, itemStack)) {
                            final int availableSpace = itemStack.getMaxStackSize() - itemStack.getAmount();
                            if (availableSpace > 0) {
                                freeSpace += availableSpace;
                            }
                        }
                    }
                }
                if (freeSpace <= 0) {
                    return;
                }
                itemRequest.setAmount(Math.min(freeSpace, limitQuantity));

                final ItemStack retrieved = root.getItemStack(itemRequest);
                if (retrieved != null && retrieved.getType() != Material.AIR) {
                    BlockMenuUtil.pushItem(blockMenu, retrieved, slots);
                }
            }

            case NULL_ONLY -> {
                int free = limitQuantity;
                for (int slot : slots) {
                    final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                    if (itemStack == null || itemStack.getType() == Material.AIR) {
                        itemRequest.setAmount(clone.getMaxStackSize());
                    } else {
                        continue;
                    }
                    itemRequest.setAmount(Math.min(itemRequest.getAmount(), free));

                    final ItemStack retrieved = root.getItemStack(itemRequest);
                    if (retrieved != null && retrieved.getType() != Material.AIR) {
                        free -= retrieved.getAmount();
                        blockMenu.pushItem(retrieved, slot);
                        if (free <= 0) {
                            break;
                        }
                    }
                }
            }

            case NONNULL_ONLY -> {
                int free = limitQuantity;
                for (int slot : slots) {
                    final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                    if (itemStack == null || itemStack.getType() == Material.AIR) {
                        continue;
                    }
                    if (itemStack.getAmount() >= clone.getMaxStackSize()) {
                        continue;
                    }
                    if (StackUtils.itemsMatch(itemRequest, itemStack)) {
                        final int space = itemStack.getMaxStackSize() - itemStack.getAmount();
                        if (space > 0) {
                            itemRequest.setAmount(space);
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                    itemRequest.setAmount(Math.min(itemRequest.getAmount(), free));

                    final ItemStack retrieved = root.getItemStack(itemRequest);
                    if (retrieved != null && retrieved.getType() != Material.AIR) {
                        free -= retrieved.getAmount();
                        blockMenu.pushItem(retrieved, slot);
                        if (free <= 0) {
                            break;
                        }
                    }
                }
            }
            case FIRST_ONLY -> {
                int free = limitQuantity;
                if (slots.length == 0) {
                    break;
                }
                final int slot = slots[0];
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    itemRequest.setAmount(clone.getMaxStackSize());
                } else {
                    if (itemStack.getAmount() >= clone.getMaxStackSize()) {
                        return;
                    }
                    if (StackUtils.itemsMatch(itemRequest, itemStack)) {
                        final int space = itemStack.getMaxStackSize() - itemStack.getAmount();
                        if (space > 0) {
                            itemRequest.setAmount(space);
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                }
                itemRequest.setAmount(Math.min(itemRequest.getAmount(), free));

                final ItemStack retrieved = root.getItemStack(itemRequest);
                if (retrieved != null && retrieved.getType() != Material.AIR) {
                    free -= retrieved.getAmount();
                    blockMenu.pushItem(retrieved, slot);
                    if (free <= 0) {
                        break;
                    }
                }
            }
            case LAST_ONLY -> {
                int free = limitQuantity;
                if (slots.length == 0) {
                    break;
                }
                final int slot = slots[slots.length - 1];
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    itemRequest.setAmount(clone.getMaxStackSize());
                } else {
                    if (itemStack.getAmount() >= clone.getMaxStackSize()) {
                        return;
                    }
                    if (StackUtils.itemsMatch(itemRequest, itemStack)) {
                        final int space = itemStack.getMaxStackSize() - itemStack.getAmount();
                        if (space > 0) {
                            itemRequest.setAmount(space);
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                }
                itemRequest.setAmount(Math.min(itemRequest.getAmount(), free));

                final ItemStack retrieved = root.getItemStack(itemRequest);
                if (retrieved != null && retrieved.getType() != Material.AIR) {
                    free -= retrieved.getAmount();
                    blockMenu.pushItem(retrieved, slot);
                    if (free <= 0) {
                        break;
                    }
                }
            }
            case FIRST_STOP -> {
                int freeSpace = 0;
                for (int slot : slots) {
                    final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                    if (itemStack == null || itemStack.getType() == Material.AIR) {
                        freeSpace += clone.getMaxStackSize();
                        break;
                    } else {
                        if (itemStack.getAmount() >= clone.getMaxStackSize()) {
                            continue;
                        }
                        if (StackUtils.itemsMatch(itemRequest, itemStack)) {
                            final int availableSpace = itemStack.getMaxStackSize() - itemStack.getAmount();
                            if (availableSpace > 0) {
                                freeSpace += availableSpace;
                            }
                        }
                        break;
                    }
                }
                if (freeSpace <= 0) {
                    return;
                }
                itemRequest.setAmount(Math.min(freeSpace, limitQuantity));

                final ItemStack retrieved = root.getItemStack(itemRequest);
                if (retrieved != null && retrieved.getType() != Material.AIR) {
                    BlockMenuUtil.pushItem(blockMenu, retrieved, slots);
                }
            }
            case LAZY -> {
                if (slots.length > 0) {
                    final ItemStack delta = blockMenu.getItemInSlot(slots[0]);
                    if (delta == null || delta.getType() == Material.AIR) {
                        int freeSpace = 0;
                        for (int slot : slots) {
                            final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                            if (itemStack == null || itemStack.getType() == Material.AIR) {
                                freeSpace += clone.getMaxStackSize();
                            } else {
                                if (itemStack.getAmount() >= clone.getMaxStackSize()) {
                                    continue;
                                }
                                if (StackUtils.itemsMatch(itemRequest, itemStack)) {
                                    final int availableSpace = itemStack.getMaxStackSize() - itemStack.getAmount();
                                    if (availableSpace > 0) {
                                        freeSpace += availableSpace;
                                    }
                                }
                            }
                        }
                        if (freeSpace <= 0) {
                            return;
                        }
                        itemRequest.setAmount(Math.min(freeSpace, limitQuantity));

                        final ItemStack retrieved = root.getItemStack(itemRequest);
                        if (retrieved != null && retrieved.getType() != Material.AIR) {
                            BlockMenuUtil.pushItem(blockMenu, retrieved, slots);
                        }
                    }
                }
            }
        }
    }

    public static void outPower(@Nonnull Location location, @Nonnull NetworkRoot root, int rate) {
        final SlimefunBlockData blockData = StorageCacheUtils.getBlock(location);
        if (blockData == null) {
            return;
        }

        if (!blockData.isDataLoaded()) {
            StorageCacheUtils.requestLoad(blockData);
            return;
        }

        final SlimefunItem slimefunItem = SlimefunItem.getById(blockData.getSfId());
        if (!(slimefunItem instanceof EnergyNetComponent component) || slimefunItem instanceof NetworkObject) {
            return;
        }

        int existingCharge = component.getCharge(location);

        final int capacity = component.getCapacity();
        final int space = capacity - existingCharge;

        if (space <= 0) {
            return;
        }

        final int possibleGeneration = Math.min(rate, space);
        final long power = root.getRootPower();

        if (power <= 0) {
            return;
        }

        final int gen = power < possibleGeneration ? (int) power : possibleGeneration;

        component.addCharge(location, gen);
        root.removeRootPower(gen);
    }
}

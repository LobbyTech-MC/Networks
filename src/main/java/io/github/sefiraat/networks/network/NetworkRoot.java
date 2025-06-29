package io.github.sefiraat.networks.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.balugaq.netex.api.data.ItemContainer;
import com.balugaq.netex.api.data.ItemFlowRecord;
import com.balugaq.netex.api.data.StorageUnitData;
import com.balugaq.netex.api.enums.StorageType;
import com.balugaq.netex.api.events.NetworkRootLocateStorageEvent;
import com.balugaq.netex.utils.BlockMenuUtil;
import com.balugaq.netex.utils.NetworksVersionedParticle;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import com.ytdd9527.networksexpansion.implementation.machines.networks.advanced.AdvancedGreedyBlock;
import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;

import io.github.mooy1.infinityexpansion.items.storage.StorageCache;
import io.github.mooy1.infinityexpansion.items.storage.StorageUnit;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.barrel.FluffyBarrel;
import io.github.sefiraat.networks.network.barrel.InfinityBarrel;
import io.github.sefiraat.networks.network.barrel.NetworkStorage;
import io.github.sefiraat.networks.network.stackcaches.BarrelIdentity;
import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.slimefun.network.NetworkCell;
import io.github.sefiraat.networks.slimefun.network.NetworkDirectional;
import io.github.sefiraat.networks.slimefun.network.NetworkGreedyBlock;
import io.github.sefiraat.networks.slimefun.network.NetworkPowerNode;
import io.github.sefiraat.networks.slimefun.network.NetworkQuantumStorage;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.ncbpfluffybear.fluffymachines.items.Barrel;
import lombok.Getter;
import lombok.Setter;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Warning;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class NetworkRoot extends NetworkNode {
    public static final int persistentThreshold = Networks.getConfigManager().getPersistentThreshold();
    public static final int cacheMissThreshold = Networks.getConfigManager().getCacheMissThreshold();
    public static final int reduceMs = Networks.getConfigManager().getReduceMs();
    public static final int transportMissThreshold = Networks.getConfigManager().getTransportMissThreshold();
    public static final Map<Location, Map<Location, Integer /* Access times */>> observingAccessHistory = new ConcurrentHashMap<>();
    public static final Map<Location, Map<Location, Integer /* Cache miss times */>> persistentAccessHistory = new ConcurrentHashMap<>();
    public static final Map<Location, Integer /* Transport miss times */> transportMissInputHistory = new ConcurrentHashMap<>();
    public static final Map<Location, Integer /* Transport miss times */> transportMissOutputHistory = new ConcurrentHashMap<>();
    public static final Map<Location, Long> reducedAccessInputHistory = new ConcurrentHashMap<>();
    public static final Map<Location, Long> reducedAccessOutputHistory = new ConcurrentHashMap<>();
    @Getter
    private final long CREATED_TIME = System.currentTimeMillis();
    @Getter
    private final Set<Location> nodeLocations = new HashSet<>();
    private final int[] CELL_AVAILABLE_SLOTS = NetworkCell.SLOTS.stream().mapToInt(i -> i).toArray();
    private final int[] GREEDY_BLOCK_AVAILABLE_SLOTS = new int[]{NetworkGreedyBlock.INPUT_SLOT};
    private final int[] ADVANCED_GREEDY_BLOCK_AVAILABLE_SLOTS = AdvancedGreedyBlock.INPUT_SLOTS;
    @Getter
    private final Set<Location> bridges = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> monitors = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> importers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> exporters = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> grids = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> cells = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> grabbers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> pushers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> purgers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> crafters = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> powerNodes = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> powerDisplays = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> encoders = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> greedyBlocks = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> cutters = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> pasters = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> vacuums = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> wirelessTransmitters = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> wirelessReceivers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> powerOutlets = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> transferPushers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> transferGrabbers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> transfers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> advancedImporters = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> advancedExporters = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> advancedGreedyBlocks = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> advancedPurgers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> advancedVacuums = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> lineTransferVanillaPushers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> lineTransferVanillaGrabbers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> inputOnlyMonitors = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> outputOnlyMonitors = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> linePowerOutlets = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> decoders = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> quantumManagers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> drawerManagers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> crafterManagers = ConcurrentHashMap.newKeySet();
    @Getter
    private final Set<Location> itemFlowViewers = ConcurrentHashMap.newKeySet();
    @Deprecated
    private final boolean progressing = false;
    @Getter
    private final int maxNodes;
    @Getter
    private final boolean recordFlow;
    @Getter
    private final @Nullable ItemFlowRecord itemFlowRecord;
    @Getter
    private Location controller = null;
    @Getter
    private boolean isOverburdened = false;
    @Deprecated
    private Set<BarrelIdentity> barrels = null;
    private Set<BarrelIdentity> inputAbleBarrels = null;
    private Set<BarrelIdentity> outputAbleBarrels = null;
    @Deprecated
    private Map<StorageUnitData, Location> cargoStorageUnitDatas = null;
    private Map<StorageUnitData, Location> inputAbleCargoStorageUnitDatas = null;
    private Map<StorageUnitData, Location> outputAbleCargoStorageUnitDatas = null;
    private Map<Location, BarrelIdentity> mapInputAbleBarrels = null;
    private Map<Location, BarrelIdentity> mapOutputAbleBarrels = null;
    private Map<Location, StorageUnitData> mapInputAbleCargoStorageUnits = null;
    private Map<Location, StorageUnitData> mapOutputAbleCargoStorageUnits = null;
    @Setter
    @Getter
    private long rootPower = 0;
    @Setter
    @Getter
    private boolean displayParticles = false;

    public NetworkRoot(@Nonnull Location location, @Nonnull NodeType type, int maxNodes) {
        this(location, type, maxNodes, false, null);
    }

    public NetworkRoot(@Nonnull Location location, @Nonnull NodeType type, int maxNodes, boolean recordFlow, @Nullable ItemFlowRecord itemFlowRecord) {
        super(location, type);
        this.maxNodes = maxNodes;
        this.root = this;
        this.recordFlow = recordFlow;
        this.itemFlowRecord = itemFlowRecord;

        registerNode(location, type);
    }

    public static void addPersistentAccessHistory(Location location, Location accessLocation) {
        Map<Location, Integer> locations = persistentAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
        locations.put(accessLocation, 0);
        persistentAccessHistory.put(location, locations);
    }

    public static void addCacheMiss(Location location, Location accessLocation) {
        Map<Location, Integer> locations = persistentAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
        int value = locations.getOrDefault(accessLocation, 0) + 1;
        if (value > cacheMissThreshold) {
            removePersistentAccessHistory(location, accessLocation);
            return;
        }
        locations.put(accessLocation, value);
        persistentAccessHistory.put(location, locations);
    }

    public static void minusCacheMiss(Location location, Location accessLocation) {
        Map<Location, Integer> locations = persistentAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
        int value = Math.max(locations.getOrDefault(accessLocation, 0) - 1, 0);
        locations.put(accessLocation, value);
    }

    public static Map<Location, Integer> getPersistentAccessHistory(Location location) {
        return persistentAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
    }

    public static void removePersistentAccessHistory(Location location) {
        persistentAccessHistory.remove(location);
    }

    public static void removePersistentAccessHistory(Location location, Location accessLocation) {
        Map<Location, Integer> locations = persistentAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
        locations.remove(accessLocation);
        persistentAccessHistory.put(location, locations);
    }

    public static void addCountObservingAccessHistory(Location location, Location accessLocation) {
        Map<Location, Integer> locations = observingAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
        Integer count = locations.getOrDefault(accessLocation, 0);
        if (count >= persistentThreshold) {
            removeCountObservingAccessHistory(location, accessLocation);
            addPersistentAccessHistory(location, accessLocation);
            return;
        }
        locations.put(accessLocation, count + 1);
        observingAccessHistory.put(location, locations);
    }

    public static Map<Location, Integer> getCountObservingAccessHistory(Location location) {
        return observingAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
    }

    public static void removeCountObservingAccessHistory(Location location) {
        observingAccessHistory.remove(location);
    }

    public static void removeCountObservingAccessHistory(Location location, Location accessLocation) {
        Map<Location, Integer> locations = observingAccessHistory.getOrDefault(location, new ConcurrentHashMap<>());
        locations.remove(accessLocation);
        observingAccessHistory.put(location, locations);
    }

    @Nullable
    public static InfinityBarrel getInfinityBarrel(@Nonnull BlockMenu blockMenu, @Nonnull StorageUnit storageUnit) {
        return getInfinityBarrel(blockMenu, storageUnit, false);
    }

    @Nullable
    public static InfinityBarrel getInfinityBarrel(@Nonnull BlockMenu blockMenu, @Nonnull StorageUnit storageUnit, boolean includeEmpty) {
        final ItemStack itemStack = blockMenu.getItemInSlot(16);
        final var data = StorageCacheUtils.getBlock(blockMenu.getLocation());
        if (data == null) {
            return null;
        }
        final String storedString = data.getData("stored");

        if (storedString == null) {
            return null;
        }

        final int storedInt = Integer.parseInt(storedString);

        if (!includeEmpty && (itemStack == null || itemStack.getType() == Material.AIR)) {
            return null;
        }


        final StorageCache cache = storageUnit.getCache(blockMenu.getLocation());

        if (cache == null) {
            return null;
        }

        final ItemStack clone;
        if (itemStack == null) {
            clone = null;
        } else {
            clone = itemStack.clone();
            clone.setAmount(1);
        }

        return new InfinityBarrel(
                blockMenu.getLocation(),
                clone,
                storedInt + itemStack.getAmount(),
                cache
        );
    }

    @Nullable

    public static FluffyBarrel getFluffyBarrel(@Nonnull BlockMenu blockMenu, @Nonnull Barrel barrel) {
        return getFluffyBarrel(blockMenu, barrel, false);
    }

    @Nullable
    public static FluffyBarrel getFluffyBarrel(@Nonnull BlockMenu blockMenu, @Nonnull Barrel barrel, boolean includeEmpty) {
        Block block = blockMenu.getBlock();
        ItemStack itemStack;
        try {
            itemStack = barrel.getStoredItem(block);
        } catch (NullPointerException ignored) {
            return null;
        }

        if (!includeEmpty && (itemStack == null || itemStack.getType() == Material.AIR)) {
            return null;
        }

        final ItemStack clone;
        if (itemStack == null) {
            clone = null;
        } else {
            clone = itemStack.clone();
            clone.setAmount(1);
        }

        int stored = barrel.getStored(block);

        if (stored <= 0) {
            return null;
        }
        int limit = barrel.getCapacity(block);
        boolean voidExcess = Boolean.parseBoolean(StorageCacheUtils.getData(blockMenu.getLocation(), "trash"));

        return new FluffyBarrel(
                blockMenu.getLocation(),
                clone,
                stored,
                limit,
                voidExcess
        );
    }

    @Nullable
    public static NetworkStorage getNetworkStorage(@Nonnull BlockMenu blockMenu) {
        return getNetworkStorage(blockMenu, false);
    }

    @Nullable
    public static NetworkStorage getNetworkStorage(@Nonnull BlockMenu blockMenu, boolean includeEmpty) {

        final QuantumCache cache = NetworkQuantumStorage.getCaches().get(blockMenu.getLocation());

        if (cache == null) {
            return null;
        }

        final ItemStack itemStack = cache.getItemStack();
        if ((itemStack == null || itemStack.getType() == Material.AIR) && !includeEmpty) {
            return null;
        }

        final ItemStack output = blockMenu.getItemInSlot(NetworkQuantumStorage.OUTPUT_SLOT);
        long storedInt = cache.getAmount();
        if (output != null && output.getType() != Material.AIR && StackUtils.itemsMatch(cache, output)) {
            storedInt = storedInt + output.getAmount();
        }

        final ItemStack clone;

        if (itemStack != null) {
            clone = itemStack.clone();
            clone.setAmount(1);
        } else {
            clone = null;
        }

        return new NetworkStorage(
                blockMenu.getLocation(),
                clone,
                storedInt
        );
    }

    @Nullable
    public static BarrelIdentity getBarrel(@Nonnull Location barrelLocation) {
        return getBarrel(barrelLocation, false);
    }

    @Nullable
    public static BarrelIdentity getBarrel(@Nonnull Location barrelLocation, boolean includeEmpty) {
        SlimefunItem item = StorageCacheUtils.getSfItem(barrelLocation);
        BlockMenu menu = StorageCacheUtils.getMenu(barrelLocation);
        if (menu == null) {
            return null;
        }

        if (item instanceof NetworkQuantumStorage) {
            return getNetworkStorage(menu, includeEmpty);
        } else if (item instanceof Barrel barrel) {
            return getFluffyBarrel(menu, barrel, includeEmpty);
        } else if (item instanceof StorageUnit storageUnit) {
            return getInfinityBarrel(menu, storageUnit, includeEmpty);
        } else {
            return null;
        }
    }

    @Nullable
    public static StorageUnitData getCargoStorageUnitData(@Nonnull BlockMenu blockMenu) {
        return NetworksDrawer.getStorageData(blockMenu.getLocation());
    }

    @Nullable
    public static StorageUnitData getCargoStorageUnitData(@Nonnull Location location) {
        return NetworksDrawer.getStorageData(location);
    }

    public void registerNode(@Nonnull Location location, @Nonnull NodeType type) {
        nodeLocations.add(location);
        switch (type) {
            case CONTROLLER -> this.controller = location;
            case BRIDGE -> bridges.add(location);
            case STORAGE_MONITOR -> monitors.add(location);
            case IMPORT -> importers.add(location);
            case EXPORT -> exporters.add(location);
            case GRID -> grids.add(location);
            case CELL -> {
                /*
                 * Fix https://github.com/Sefiraat/Networks/issues/211
                 */
                BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
                if (blockMenu == null) {
                    return;
                }
                if (Arrays.equals(blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW), CELL_AVAILABLE_SLOTS)) {
                    cells.add(location);
                }
            }
            case GRABBER -> grabbers.add(location);
            case PUSHER -> pushers.add(location);
            case PURGER -> purgers.add(location);
            case CRAFTER -> crafters.add(location);
            case POWER_NODE -> powerNodes.add(location);
            case POWER_DISPLAY -> powerDisplays.add(location);
            case ENCODER -> encoders.add(location);
            case GREEDY_BLOCK -> {
                /*
                 * Fix https://github.com/Sefiraat/Networks/issues/211
                 */
                BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
                if (blockMenu == null) {
                    return;
                }
                if (Arrays.equals(blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW), GREEDY_BLOCK_AVAILABLE_SLOTS)) {
                    greedyBlocks.add(location);
                }
            }
            case CUTTER -> cutters.add(location);
            case PASTER -> pasters.add(location);
            case VACUUM -> vacuums.add(location);
            case WIRELESS_TRANSMITTER -> wirelessTransmitters.add(location);
            case WIRELESS_RECEIVER -> wirelessReceivers.add(location);
            case POWER_OUTLET -> powerOutlets.add(location);
            // from networks expansion
            case ADVANCED_IMPORT -> advancedImporters.add(location);
            case ADVANCED_EXPORT -> advancedExporters.add(location);
            case ADVANCED_GREEDY_BLOCK -> {
                /*
                 * Fix https://github.com/Sefiraat/Networks/issues/211
                 */
                BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
                if (blockMenu == null) {
                    return;
                }
                if (Arrays.equals(blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW), ADVANCED_GREEDY_BLOCK_AVAILABLE_SLOTS)) {
                    advancedGreedyBlocks.add(location);
                }
            }
            case ADVANCED_PURGER -> advancedPurgers.add(location);
            case ADVANCED_VACUUM -> advancedVacuums.add(location);
            case TRANSFER -> transfers.add(location);
            case TRANSFER_PUSHER -> transferPushers.add(location);
            case TRANSFER_GRABBER -> transferGrabbers.add(location);
            case LINE_TRANSFER_VANILLA_GRABBER -> lineTransferVanillaGrabbers.add(location);
            case LINE_TRANSFER_VANILLA_PUSHER -> lineTransferVanillaPushers.add(location);
            case INPUT_ONLY_MONITOR -> inputOnlyMonitors.add(location);
            case OUTPUT_ONLY_MONITOR -> outputOnlyMonitors.add(location);
            case LINE_POWER_OUTLET -> linePowerOutlets.add(location);
            case DECODER -> decoders.add(location);
            case QUANTUM_MANAGER -> quantumManagers.add(location);
            case DRAWER_MANAGER -> drawerManagers.add(location);
            case CRAFTER_MANAGER -> crafterManagers.add(location);
            case FLOW_VIEWER -> itemFlowViewers.add(location);
        }
    }

    public int getNodeCount() {
        return this.nodeLocations.size();
    }

    public void setOverburdened(boolean overburdened) {
        if (overburdened && !isOverburdened) {
            final Location loc = this.nodePosition.clone();
            for (int x = 0; x <= 1; x++) {
                for (int y = 0; y <= 1; y++) {
                    for (int z = 0; z <= 1; z++) {
                        loc.getWorld().spawnParticle(NetworksVersionedParticle.EXPLOSION, loc.clone().add(x, y, z), 0);
                    }
                }
            }
        }
        this.isOverburdened = overburdened;
    }

    @Nonnull
    public Map<ItemStack, Long> getAllNetworkItemsLongType() {
        final Map<ItemStack, Long> itemStacks = new HashMap<>();

        // Barrels
        for (BarrelIdentity barrelIdentity : getOutputAbleBarrels()) {
            final Long currentAmount = itemStacks.get(barrelIdentity.getItemStack());
            final long newAmount;
            if (currentAmount == null) {
                newAmount = barrelIdentity.getAmount();
            } else {
                long newLong = currentAmount + barrelIdentity.getAmount();
                if (newLong < 0) {
                    newAmount = 0;
                } else {
                    newAmount = currentAmount + barrelIdentity.getAmount();
                }
            }
            itemStacks.put(barrelIdentity.getItemStack(), newAmount);
        }

        // Cargo storage units
        Map<StorageUnitData, Location> cacheMap = getOutputAbleCargoStorageUnitDatas();
        for (StorageUnitData cache : cacheMap.keySet()) {
            for (ItemContainer itemContainer : cache.getStoredItems()) {
                final Long currentAmount = itemStacks.get(itemContainer.getSample());
                long newAmount;
                if (currentAmount == null) {
                    newAmount = itemContainer.getAmount();
                } else {
                    long newLong = currentAmount + (long) itemContainer.getAmount();
                    if (newLong < 0) {
                        newAmount = 0;
                    } else {
                        newAmount = currentAmount + itemContainer.getAmount();
                    }
                }
                itemStacks.put(itemContainer.getSample(), newAmount);
            }
        }

        for (BlockMenu blockMenu : getAdvancedGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    continue;
                }
                final ItemStack clone = StackUtils.getAsQuantity(itemStack, 1);
                final Long currentAmount = itemStacks.get(clone);
                final long newAmount;
                if (currentAmount == null) {
                    newAmount = itemStack.getAmount();
                } else {
                    long newLong = currentAmount + (long) itemStack.getAmount();
                    if (newLong < 0) {
                        newAmount = 0;
                    } else {
                        newAmount = currentAmount + itemStack.getAmount();
                    }
                }
                itemStacks.put(clone, newAmount);
            }
        }

        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            final ItemStack itemStack = blockMenu.getItemInSlot(slots[0]);
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            final ItemStack clone = StackUtils.getAsQuantity(itemStack, 1);
            final Long currentAmount = itemStacks.get(clone);
            final long newAmount;
            if (currentAmount == null) {
                newAmount = itemStack.getAmount();
            } else {
                long newLong = currentAmount + (long) itemStack.getAmount();
                if (newLong < 0) {
                    newAmount = 0;
                } else {
                    newAmount = currentAmount + itemStack.getAmount();
                }
            }
            itemStacks.put(clone, newAmount);
        }

        for (BlockMenu blockMenu : getCrafterOutputs()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    continue;
                }
                final ItemStack clone = StackUtils.getAsQuantity(itemStack, 1);
                final Long currentAmount = itemStacks.get(clone);
                final long newAmount;
                if (currentAmount == null) {
                    newAmount = itemStack.getAmount();
                } else {
                    long newLong = currentAmount + (long) itemStack.getAmount();
                    if (newLong < 0) {
                        newAmount = 0;
                    } else {
                        newAmount = currentAmount + itemStack.getAmount();
                    }
                }
                itemStacks.put(clone, newAmount);
            }
        }

        for (BlockMenu blockMenu : getCellMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack != null && itemStack.getType() != Material.AIR) {
                    final ItemStack clone = itemStack.clone();

                    clone.setAmount(1);

                    final Long currentAmount = itemStacks.get(clone);
                    long newAmount;

                    if (currentAmount == null) {
                        newAmount = itemStack.getAmount();
                    } else {
                        long newLong = currentAmount + (long) itemStack.getAmount();
                        if (newLong < 0) {
                            newAmount = 0;
                        } else {
                            newAmount = currentAmount + itemStack.getAmount();
                        }
                    }

                    itemStacks.put(clone, newAmount);
                }
            }
        }
        return itemStacks;
    }

    public Map<ItemStack, Integer> getAllNetworkItems() {
        final Map<ItemStack, Integer> itemStacks = new HashMap<>();

        // Barrels
        for (BarrelIdentity barrelIdentity : getOutputAbleBarrels()) {
            final Integer currentAmount = itemStacks.get(barrelIdentity.getItemStack());
            final long newAmount;
            if (currentAmount == null) {
                newAmount = barrelIdentity.getAmount();
            } else {
                long newLong = (long) currentAmount + barrelIdentity.getAmount();
                if (newLong > Integer.MAX_VALUE) {
                    newAmount = Integer.MAX_VALUE;
                } else {
                    newAmount = currentAmount + barrelIdentity.getAmount();
                }
            }
            itemStacks.put(barrelIdentity.getItemStack(), (int) newAmount);
        }

        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            final ItemStack itemStack = blockMenu.getItemInSlot(slots[0]);
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            final ItemStack clone = StackUtils.getAsQuantity(itemStack, 1);
            final Integer currentAmount = itemStacks.get(clone);
            final int newAmount;
            if (currentAmount == null) {
                newAmount = itemStack.getAmount();
            } else {
                long newLong = (long) currentAmount + (long) itemStack.getAmount();
                if (newLong > Integer.MAX_VALUE) {
                    newAmount = Integer.MAX_VALUE;
                } else {
                    newAmount = currentAmount + itemStack.getAmount();
                }
            }
            itemStacks.put(clone, newAmount);
        }

        for (BlockMenu blockMenu : getCrafterOutputs()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    continue;
                }
                final ItemStack clone = StackUtils.getAsQuantity(itemStack, 1);
                final Integer currentAmount = itemStacks.get(clone);
                final int newAmount;
                if (currentAmount == null) {
                    newAmount = itemStack.getAmount();
                } else {
                    long newLong = (long) currentAmount + (long) itemStack.getAmount();
                    if (newLong > Integer.MAX_VALUE) {
                        newAmount = Integer.MAX_VALUE;
                    } else {
                        newAmount = currentAmount + itemStack.getAmount();
                    }
                }
                itemStacks.put(clone, newAmount);
            }
        }

        for (BlockMenu blockMenu : getCellMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack != null && itemStack.getType() != Material.AIR) {
                    final ItemStack clone = itemStack.clone();

                    clone.setAmount(1);

                    final Integer currentAmount = itemStacks.get(clone);
                    int newAmount;

                    if (currentAmount == null) {
                        newAmount = itemStack.getAmount();
                    } else {
                        long newLong = (long) currentAmount + (long) itemStack.getAmount();
                        if (newLong > Integer.MAX_VALUE) {
                            newAmount = Integer.MAX_VALUE;
                        } else {
                            newAmount = currentAmount + itemStack.getAmount();
                        }
                    }

                    itemStacks.put(clone, newAmount);
                }
            }
        }

        for (BlockMenu blockMenu : getAdvancedGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    continue;
                }
                final ItemStack clone = StackUtils.getAsQuantity(itemStack, 1);
                final Integer currentAmount = itemStacks.get(clone);
                final int newAmount;
                if (currentAmount == null) {
                    newAmount = itemStack.getAmount();
                } else {
                    long newLong = (long) currentAmount + (long) itemStack.getAmount();
                    if (newLong > Integer.MAX_VALUE) {
                        newAmount = Integer.MAX_VALUE;
                    } else {
                        newAmount = currentAmount + itemStack.getAmount();
                    }
                }
                itemStacks.put(clone, newAmount);
            }
        }

        Map<StorageUnitData, Location> cacheMap = getOutputAbleCargoStorageUnitDatas();
        for (StorageUnitData cache : cacheMap.keySet()) {
            for (ItemContainer itemContainer : cache.getStoredItems()) {
                final Integer currentAmount = itemStacks.get(itemContainer.getSample());
                int newAmount;
                if (currentAmount == null) {
                    newAmount = itemContainer.getAmount();
                } else {
                    long newLong = (long) currentAmount + (long) itemContainer.getAmount();
                    if (newLong > Integer.MAX_VALUE) {
                        newAmount = Integer.MAX_VALUE;
                    } else {
                        newAmount = currentAmount + itemContainer.getAmount();
                    }
                }
                itemStacks.put(itemContainer.getSample(), newAmount);
            }
        }
        return itemStacks;
    }

    @Deprecated
    @Nonnull
    public Set<BarrelIdentity> getBarrels() {

        if (this.barrels != null) {
            return this.barrels;
        }

        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Set<BarrelIdentity> barrelSet = ConcurrentHashMap.newKeySet();

        for (Location cellLocation : this.monitors) {
            final BlockFace face = NetworkDirectional.getSelectedFace(cellLocation);

            if (face == null) {
                continue;
            }

            final Location testLocation = cellLocation.clone().add(face.getDirection());

            if (addedLocations.contains(testLocation)) {
                continue;
            } else {
                addedLocations.add(testLocation);
            }

            final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(testLocation);

            if (Networks.getSupportedPluginManager()
                    .isInfinityExpansion() && slimefunItem instanceof StorageUnit unit) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final InfinityBarrel infinityBarrel = getInfinityBarrel(menu, unit);
                if (infinityBarrel != null) {
                    barrelSet.add(infinityBarrel);
                }
            } else if (Networks.getSupportedPluginManager().isFluffyMachines() && slimefunItem instanceof Barrel barrel) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final FluffyBarrel fluffyBarrel = getFluffyBarrel(menu, barrel);
                if (fluffyBarrel != null) {
                    barrelSet.add(fluffyBarrel);
                }
            } else if (slimefunItem instanceof NetworkQuantumStorage) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final NetworkStorage storage = getNetworkStorage(menu);
                if (storage != null) {
                    barrelSet.add(storage);
                }
            }
        }

        this.barrels = barrelSet;
        NetworkRootLocateStorageEvent event = new NetworkRootLocateStorageEvent(this, StorageType.BARREL, true, true, Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return barrelSet;
    }

    @Deprecated
    @Nonnull
    public Map<StorageUnitData, Location> getCargoStorageUnitDatas() {
        if (this.cargoStorageUnitDatas != null) {
            return this.cargoStorageUnitDatas;
        }

        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Map<StorageUnitData, Location> dataSet = new HashMap<>();

        for (Location cellLocation : this.monitors) {
            final BlockFace face = NetworkDirectional.getSelectedFace(cellLocation);

            if (face == null) {
                continue;
            }

            final Location testLocation = cellLocation.clone().add(face.getDirection());

            if (addedLocations.contains(testLocation)) {
                continue;
            } else {
                addedLocations.add(testLocation);
            }

            final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(testLocation);

            if (slimefunItem instanceof NetworksDrawer) {
                final StorageUnitData data = getCargoStorageUnitData(testLocation);
                if (data != null) {
                    dataSet.put(data, testLocation);
                }
            }
        }

        this.cargoStorageUnitDatas = dataSet;
        NetworkRootLocateStorageEvent event = new NetworkRootLocateStorageEvent(this, StorageType.DRAWER, true, true, Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return dataSet;
    }

    @Nonnull
    public Set<BlockMenu> getCellMenus() {
        final Set<BlockMenu> menus = new HashSet<>();
        for (Location cellLocation : this.cells) {
            BlockMenu menu = StorageCacheUtils.getMenu(cellLocation);
            if (menu != null) {
                menus.add(menu);
            }
        }
        return menus;
    }

    @Nonnull
    public Set<BlockMenu> getCrafterOutputs() {
        final Set<BlockMenu> menus = new HashSet<>();
        for (Location location : this.crafters) {
            BlockMenu menu = StorageCacheUtils.getMenu(location);
            if (menu != null) {
                menus.add(menu);
            }
        }
        return menus;
    }

    @Nonnull
    public Set<BlockMenu> getGreedyBlockMenus() {
        final Set<BlockMenu> menus = new HashSet<>();
        for (Location location : this.greedyBlocks) {
            BlockMenu menu = StorageCacheUtils.getMenu(location);
            if (menu != null) {
                menus.add(menu);
            }
        }
        return menus;
    }

    @Nonnull
    public Set<BlockMenu> getAdvancedGreedyBlockMenus() {
        final Set<BlockMenu> menus = new HashSet<>();
        for (Location location : this.advancedGreedyBlocks) {
            BlockMenu menu = StorageCacheUtils.getMenu(location);
            if (menu != null) {
                menus.add(menu);
            }
        }
        return menus;
    }

    @Warning(reason = "This method is deprecated and will be removed in the future. Use getItemStack0(Location, ItemRequest) instead.")
    @Deprecated(forRemoval = true)
    @Nullable
    public ItemStack getItemStack(@Nonnull ItemRequest request) {
        ItemStack stackToReturn = null;

        if (request.getAmount() <= 0) {
            return null;
        }

        // Barrels first
        for (BarrelIdentity barrelIdentity : getOutputAbleBarrels()) {

            final ItemStack itemStack = barrelIdentity.getItemStack();

            if (itemStack == null || !StackUtils.itemsMatch(request, itemStack)) {
                continue;
            }

            boolean infinity = barrelIdentity instanceof InfinityBarrel;
            final ItemStack fetched = barrelIdentity.requestItem(request);
            if (fetched == null || fetched.getType() == Material.AIR || (infinity && fetched.getAmount() == 1)) {
                continue;
            }

            // Stack is null, so we can fill it here
            if (stackToReturn == null) {
                stackToReturn = fetched.clone();
                stackToReturn.setAmount(0);
            }

            final int preserveAmount = infinity ? fetched.getAmount() - 1 : fetched.getAmount();

            if (request.getAmount() <= preserveAmount) {
                stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                fetched.setAmount(fetched.getAmount() - request.getAmount());
                return stackToReturn;
            } else {
                stackToReturn.setAmount(stackToReturn.getAmount() + preserveAmount);
                request.receiveAmount(preserveAmount);
                fetched.setAmount(fetched.getAmount() - preserveAmount);
            }
        }

        // Units
        for (StorageUnitData cache : getOutputAbleCargoStorageUnitDatas().keySet()) {
            ItemStack take = cache.requestItem(request);
            if (take != null) {
                if (stackToReturn == null) {
                    stackToReturn = take.clone();
                } else {
                    stackToReturn.setAmount(stackToReturn.getAmount() + take.getAmount());
                }
                request.receiveAmount(take.getAmount());

                if (request.getAmount() <= 0) {
                    return stackToReturn;
                }
            }
        }

        // Cells
        for (BlockMenu blockMenu : getCellMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null
                        || itemStack.getType() == Material.AIR
                        || !StackUtils.itemsMatch(request, itemStack)
                ) {
                    continue;
                }

                // Mark the Cell as dirty otherwise the changes will not save on shutdown
                blockMenu.markDirty();

                // If the return stack is null, we need to set it up
                if (stackToReturn == null) {
                    stackToReturn = itemStack.clone();
                    stackToReturn.setAmount(0);
                }

                if (request.getAmount() <= itemStack.getAmount()) {
                    // We can't take more than this stack. Level to request amount, remove items and then return
                    stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                    itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                    return stackToReturn;
                } else {
                    // We can take more than what is here, consume before trying to take more
                    stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                    request.receiveAmount(itemStack.getAmount());
                    itemStack.setAmount(0);
                }
            }
        }

        // Crafters
        for (BlockMenu blockMenu : getCrafterOutputs()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null || itemStack.getType() == Material.AIR || !StackUtils.itemsMatch(
                        request,
                        itemStack
                )) {
                    continue;
                }

                // Stack is null, so we can fill it here
                if (stackToReturn == null) {
                    stackToReturn = itemStack.clone();
                    stackToReturn.setAmount(0);
                }

                if (request.getAmount() <= itemStack.getAmount()) {
                    stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                    itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                    return stackToReturn;
                } else {
                    stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                    request.receiveAmount(itemStack.getAmount());
                    itemStack.setAmount(0);
                }
            }
        }

        for (BlockMenu blockMenu : getAdvancedGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null || itemStack.getType() == Material.AIR || !StackUtils.itemsMatch(
                        request,
                        itemStack
                )) {
                    continue;
                }

                // Stack is null, so we can fill it here
                if (stackToReturn == null) {
                    stackToReturn = itemStack.clone();
                    stackToReturn.setAmount(0);
                }

                if (request.getAmount() <= itemStack.getAmount()) {
                    stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                    itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                    return stackToReturn;
                } else {
                    stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                    request.receiveAmount(itemStack.getAmount());
                    itemStack.setAmount(0);
                }
            }
        }

        // Greedy Blocks
        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            final ItemStack itemStack = blockMenu.getItemInSlot(slots[0]);
            if (itemStack == null
                    || itemStack.getType() == Material.AIR
                    || !StackUtils.itemsMatch(request, itemStack)
            ) {
                continue;
            }

            // Mark the Cell as dirty otherwise the changes will not save on shutdown
            blockMenu.markDirty();

            // If the return stack is null, we need to set it up
            if (stackToReturn == null) {
                stackToReturn = itemStack.clone();
                stackToReturn.setAmount(0);
            }

            if (request.getAmount() <= itemStack.getAmount()) {
                // We can't take more than this stack. Level to request amount, remove items and then return
                stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                return stackToReturn;
            } else {
                // We can take more than what is here, consume before trying to take more
                stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                request.receiveAmount(itemStack.getAmount());
                itemStack.setAmount(0);
            }
        }

        if (stackToReturn == null || stackToReturn.getAmount() == 0) {
            return null;
        }

        return stackToReturn;
    }

    public boolean contains(@Nonnull ItemStack itemStack) {
        return contains(new ItemRequest(itemStack, 1));
    }

    public boolean contains(@Nonnull ItemRequest request) {

        long found = 0;

        // Barrels
        for (BarrelIdentity barrelIdentity : getOutputAbleBarrels()) {
            final ItemStack itemStack = barrelIdentity.getItemStack();

            if (itemStack == null || !StackUtils.itemsMatch(request, itemStack)) {
                continue;
            }

            if (barrelIdentity instanceof InfinityBarrel) {
                if (barrelIdentity.getItemStack().getMaxStackSize() > 1) {
                    found += barrelIdentity.getAmount() - 2;
                }
            } else {
                found += barrelIdentity.getAmount();
            }

            // Escape if found all we need
            if (found >= request.getAmount()) {
                return true;
            }
        }

        // Crafters
        for (BlockMenu blockMenu : getCrafterOutputs()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null
                        || itemStack.getType() == Material.AIR
                        || !StackUtils.itemsMatch(request, itemStack)
                ) {
                    continue;
                }

                found += itemStack.getAmount();

                // Escape if found all we need
                if (found >= request.getAmount()) {
                    return true;
                }
            }
        }

        Map<StorageUnitData, Location> cacheMap = getOutputAbleCargoStorageUnitDatas();
        for (StorageUnitData cache : cacheMap.keySet()) {
            final List<ItemContainer> storedItems = cache.getStoredItems();
            for (ItemContainer itemContainer : storedItems) {
                final ItemStack itemStack = itemContainer.getSampleDirectly();
                if (itemStack == null
                        || itemStack.getType() == Material.AIR
                        || !StackUtils.itemsMatch(request, itemStack)
                ) {
                    continue;
                }

                int amount = itemContainer.getAmount();
                found += amount;


                // Escape if found all we need
                if (found >= request.getAmount()) {
                    return true;
                }
            }
        }

        for (BlockMenu blockMenu : getAdvancedGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null
                        || itemStack.getType() == Material.AIR
                        || !StackUtils.itemsMatch(request, itemStack)
                ) {
                    continue;
                }

                found += itemStack.getAmount();

                // Escape if found all we need
                if (found >= request.getAmount()) {
                    return true;
                }
            }
        }

        // Greedy Blocks
        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            final ItemStack itemStack = blockMenu.getItemInSlot(slots[0]);
            if (itemStack == null
                    || itemStack.getType() == Material.AIR
                    || !StackUtils.itemsMatch(request, itemStack)
            ) {
                continue;
            }

            found += itemStack.getAmount();

            // Escape if found all we need
            if (found >= request.getAmount()) {
                return true;
            }
        }

        // Cells
        for (BlockMenu blockMenu : getCellMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null
                        || itemStack.getType() == Material.AIR
                        || !StackUtils.itemsMatch(request, itemStack)
                ) {
                    continue;
                }

                found += itemStack.getAmount();

                // Escape if found all we need
                if (found >= request.getAmount()) {
                    return true;
                }
            }
        }

        return false;
    }

    public int getAmount(@Nonnull ItemStack itemStack) {
        long totalAmount = 0;
        for (BlockMenu blockMenu : getAdvancedGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack inputSlotItem = blockMenu.getItemInSlot(slot);
                if (inputSlotItem != null && StackUtils.itemsMatch(inputSlotItem, itemStack)) {
                    totalAmount += inputSlotItem.getAmount();
                }
            }
        }

        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            ItemStack inputSlotItem = blockMenu.getItemInSlot(slots[0]);
            if (inputSlotItem != null && StackUtils.itemsMatch(inputSlotItem, itemStack)) {
                totalAmount += inputSlotItem.getAmount();
            }
        }

        for (BarrelIdentity barrelIdentity : getOutputAbleBarrels()) {
            if (StackUtils.itemsMatch(barrelIdentity.getItemStack(), itemStack)) {
                totalAmount += barrelIdentity.getAmount();
                if (barrelIdentity instanceof InfinityBarrel) {
                    totalAmount -= 2;
                }
            }
        }
        Map<StorageUnitData, Location> cacheMap = getOutputAbleCargoStorageUnitDatas();
        for (StorageUnitData cache : cacheMap.keySet()) {
            final List<ItemContainer> storedItems = cache.getStoredItems();
            for (ItemContainer itemContainer : storedItems) {
                if (StackUtils.itemsMatch(itemContainer.getSampleDirectly(), itemStack)) {
                    totalAmount += itemContainer.getAmount();
                }
            }
        }

        for (BlockMenu blockMenu : getCellMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack cellItem = blockMenu.getItemInSlot(slot);
                if (cellItem != null && StackUtils.itemsMatch(cellItem, itemStack)) {
                    totalAmount += cellItem.getAmount();
                }
            }
        }
        if (totalAmount > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return (int) totalAmount;
        }
    }

    public HashMap<ItemStack, Long> getAmount(@Nonnull Set<ItemStack> itemStacks) {
        HashMap<ItemStack, Long> totalAmounts = new HashMap<>();
        for (BlockMenu menu : getAdvancedGreedyBlockMenus()) {
            int[] slots = menu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack inputSlotItem = menu.getItemInSlot(slot);
                if (inputSlotItem != null) {
                    for (ItemStack itemStack : itemStacks) {
                        if (StackUtils.itemsMatch(inputSlotItem, itemStack)) {
                            totalAmounts.put(itemStack, totalAmounts.getOrDefault(itemStack, 0L) + inputSlotItem.getAmount());
                        }
                    }
                }
            }
        }

        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            ItemStack inputSlotItem = blockMenu.getItemInSlot(slots[0]);
            if (inputSlotItem != null) {
                for (ItemStack itemStack : itemStacks) {
                    if (StackUtils.itemsMatch(inputSlotItem, itemStack)) {
                        totalAmounts.put(itemStack, totalAmounts.getOrDefault(itemStack, 0L) + inputSlotItem.getAmount());
                    }
                }
            }
        }

        for (BarrelIdentity barrelIdentity : getOutputAbleBarrels()) {
            for (ItemStack itemStack : itemStacks) {
                if (StackUtils.itemsMatch(barrelIdentity.getItemStack(), itemStack)) {
                    long totalAmount = barrelIdentity.getAmount();
                    if (barrelIdentity instanceof InfinityBarrel) {
                        totalAmount -= 2;
                    }
                    totalAmounts.put(itemStack, totalAmounts.getOrDefault(itemStack, 0L) + totalAmount);
                }
            }
        }
        Map<StorageUnitData, Location> cacheMap = getOutputAbleCargoStorageUnitDatas();
        for (StorageUnitData cache : cacheMap.keySet()) {
            final List<ItemContainer> storedItems = cache.getStoredItems();
            for (ItemContainer itemContainer : storedItems) {
                for (ItemStack itemStack : itemStacks) {
                    if (StackUtils.itemsMatch(itemContainer.getSample(), itemStack)) {
                        long totalAmount = itemContainer.getAmount();
                        totalAmounts.put(itemStack, totalAmounts.getOrDefault(itemStack, 0L) + totalAmount);
                    }
                }
            }
        }

        for (BlockMenu blockMenu : getCellMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack cellItem = blockMenu.getItemInSlot(slot);
                if (cellItem != null) {
                    for (ItemStack itemStack : itemStacks) {
                        if (StackUtils.itemsMatch(cellItem, itemStack)) {
                            totalAmounts.put(itemStack, totalAmounts.getOrDefault(itemStack, 0L) + cellItem.getAmount());
                        }
                    }
                }
            }
        }

        return totalAmounts;
    }

    @Warning(reason = "This method is deprecated and will be removed in the future. Use addItemStack0(Location, ItemStack) instead.")
    @Deprecated(forRemoval = true)
    public void addItemStack(@Nonnull ItemStack incoming) {
        for (BlockMenu blockMenu : getAdvancedGreedyBlockMenus()) {
            final ItemStack template = blockMenu.getItemInSlot(AdvancedGreedyBlock.TEMPLATE_SLOT);

            if (template == null || template.getType() == Material.AIR || !StackUtils.itemsMatch(incoming, template)) {
                continue;
            }

            blockMenu.markDirty();
            BlockMenuUtil.pushItem(blockMenu, incoming, ADVANCED_GREEDY_BLOCK_AVAILABLE_SLOTS);
            // Given we have found a match, it doesn't matter if the item moved or not, we will not bring it in
            return;
        }

        // Run for matching greedy blocks
        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            final ItemStack template = blockMenu.getItemInSlot(NetworkGreedyBlock.TEMPLATE_SLOT);

            if (template == null || template.getType() == Material.AIR || !StackUtils.itemsMatch(incoming, template)) {
                continue;
            }

            blockMenu.markDirty();
            BlockMenuUtil.pushItem(blockMenu, incoming, GREEDY_BLOCK_AVAILABLE_SLOTS[0]);
            // Given we have found a match, it doesn't matter if the item moved or not, we will not bring it in
            return;
        }


        // Run for matching barrels
        for (BarrelIdentity barrelIdentity : getInputAbleBarrels()) {
            if (StackUtils.itemsMatch(barrelIdentity, incoming)) {
                barrelIdentity.depositItemStack(incoming);

                // All distributed, can escape
                if (incoming.getAmount() == 0) {
                    return;
                }
            }
        }

        for (StorageUnitData cache : getInputAbleCargoStorageUnitDatas().keySet()) {
            cache.depositItemStack(incoming, true);

            if (incoming.getAmount() == 0) {
                return;
            }
        }

        for (BlockMenu blockMenu : getCellMenus()) {
            blockMenu.markDirty();
            BlockMenuUtil.pushItem(blockMenu, incoming, CELL_AVAILABLE_SLOTS);
            if (incoming.getAmount() == 0) {
                return;
            }
        }
    }

    @Override
    public long retrieveBlockCharge() {
        return 0;
    }

    public void addRootPower(long power) {
        this.rootPower += power;
    }

    public void removeRootPower(long power) {
        int removed = 0;
        for (Location node : powerNodes) {
            final SlimefunItem item = StorageCacheUtils.getSfItem(node);
            if (item instanceof NetworkPowerNode powerNode) {
                final int charge = powerNode.getCharge(node);
                if (charge <= 0) {
                    continue;
                }
                final int toRemove = (int) Math.min(power - removed, charge);
                powerNode.removeCharge(node, toRemove);
                this.rootPower -= power;
                removed = removed + toRemove;
            }
            if (removed >= power) {
                return;
            }
        }
    }

    @Warning(reason = "This method is deprecated and will be removed in the future. Use getItemStacks0(Location, List<ItemRequest>) instead.")
    @Deprecated(forRemoval = true)
    @Nonnull
    public List<ItemStack> getItemStacks(@Nonnull List<ItemRequest> itemRequests) {
        List<ItemStack> retrievedItems = new ArrayList<>();

        for (ItemRequest request : itemRequests) {
            ItemStack retrieved = getItemStack(request);
            if (retrieved != null) {
                retrievedItems.add(retrieved);
            }
        }
        return retrievedItems;
    }

    @Nonnull
    public List<ItemStack> getItemStacks0(@Nonnull Location location, @Nonnull List<ItemRequest> itemRequests) {
        List<ItemStack> retrievedItems = new ArrayList<>();
        for (ItemRequest request : itemRequests) {
            ItemStack retrieved = getItemStack0(location, request);
            if (retrieved != null) {
                retrievedItems.add(retrieved);
            }
        }
        return retrievedItems;
    }

    @Nonnull
    public List<BarrelIdentity> getBarrels(Predicate<BarrelIdentity> filter, NetworkRootLocateStorageEvent.Strategy strategy, boolean includeEmpty) {
        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final List<BarrelIdentity> barrelSet = new ArrayList<>();

        final Set<Location> monitor = new HashSet<>();
        monitor.addAll(this.inputOnlyMonitors);
        monitor.addAll(this.outputOnlyMonitors);
        monitor.addAll(this.monitors);
        for (Location cellLocation : monitor) {
            final BlockFace face = NetworkDirectional.getSelectedFace(cellLocation);

            if (face == null) {
                continue;
            }

            final Location testLocation = cellLocation.clone().add(face.getDirection());

            if (addedLocations.contains(testLocation)) {
                continue;
            } else {
                addedLocations.add(testLocation);
            }

            final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(testLocation);

            if (Networks.getSupportedPluginManager().isInfinityExpansion() && slimefunItem instanceof StorageUnit unit) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final InfinityBarrel infinityBarrel = getInfinityBarrel(menu, unit, includeEmpty);
                if (infinityBarrel != null) {
                    if (filter.test(infinityBarrel)) {
                        barrelSet.add(infinityBarrel);
                    }
                }
                continue;
            }
            if (Networks.getSupportedPluginManager().isFluffyMachines() && slimefunItem instanceof Barrel barrel) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final FluffyBarrel fluffyBarrel = getFluffyBarrel(menu, barrel, includeEmpty);
                if (fluffyBarrel != null) {
                    if (filter.test(fluffyBarrel)) {
                        barrelSet.add(fluffyBarrel);
                    }
                }
                continue;
            }
            if (slimefunItem instanceof NetworkQuantumStorage) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final NetworkStorage storage = getNetworkStorage(menu, includeEmpty);
                if (storage != null) {
                    if (filter.test(storage)) {
                        barrelSet.add(storage);
                    }
                }
            }
        }

        NetworkRootLocateStorageEvent event = new NetworkRootLocateStorageEvent(this, StorageType.BARREL, strategy, Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return barrelSet;
    }

    @Nonnull
    public Map<StorageUnitData, Location> getCargoStorageUnitDatas(NetworkRootLocateStorageEvent.Strategy strategy, boolean includeEmpty) {
        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Map<StorageUnitData, Location> dataSet = new HashMap<>();

        final Set<Location> monitor = new HashSet<>();
        monitor.addAll(this.inputOnlyMonitors);
        monitor.addAll(this.outputOnlyMonitors);
        monitor.addAll(this.monitors);
        for (Location cellLocation : monitor) {
            final BlockFace face = NetworkDirectional.getSelectedFace(cellLocation);

            if (face == null) {
                continue;
            }

            final Location testLocation = cellLocation.clone().add(face.getDirection());

            if (addedLocations.contains(testLocation)) {
                continue;
            } else {
                addedLocations.add(testLocation);
            }

            final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(testLocation);

            if (slimefunItem instanceof NetworksDrawer) {
                final StorageUnitData data = getCargoStorageUnitData(testLocation);
                if (data != null) {
                    dataSet.put(data, testLocation);
                }
            }
        }

        NetworkRootLocateStorageEvent event = new NetworkRootLocateStorageEvent(this, StorageType.DRAWER, strategy, Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return dataSet;
    }

    @Nonnull
    public Set<BarrelIdentity> getInputAbleBarrels() {
        if (this.inputAbleBarrels != null) {
            return this.inputAbleBarrels;
        }

        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Set<BarrelIdentity> barrelSet = ConcurrentHashMap.newKeySet();

        final Set<Location> monitor = new HashSet<>();
        monitor.addAll(this.inputOnlyMonitors);
        monitor.addAll(this.monitors);
        for (Location cellLocation : monitor) {
            final BlockFace face = NetworkDirectional.getSelectedFace(cellLocation);

            if (face == null) {
                continue;
            }

            final Location testLocation = cellLocation.clone().add(face.getDirection());

            if (addedLocations.contains(testLocation)) {
                continue;
            } else {
                addedLocations.add(testLocation);
            }

            final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(testLocation);

            if (Networks.getSupportedPluginManager().isInfinityExpansion() && slimefunItem instanceof StorageUnit unit) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final InfinityBarrel infinityBarrel = getInfinityBarrel(menu, unit);
                if (infinityBarrel != null) {
                    barrelSet.add(infinityBarrel);
                }
                continue;
            }
            if (Networks.getSupportedPluginManager().isFluffyMachines() && slimefunItem instanceof Barrel barrel) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final FluffyBarrel fluffyBarrel = getFluffyBarrel(menu, barrel);
                if (fluffyBarrel != null) {
                    barrelSet.add(fluffyBarrel);
                }
                continue;
            }
            if (slimefunItem instanceof NetworkQuantumStorage) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final NetworkStorage storage = getNetworkStorage(menu);
                if (storage != null) {
                    barrelSet.add(storage);
                }
            }
        }

        this.inputAbleBarrels = barrelSet;
        this.mapInputAbleBarrels = new HashMap<>();
        for (BarrelIdentity storage : barrelSet) {
            this.mapInputAbleBarrels.put(storage.getLocation(), storage);
        }
        NetworkRootLocateStorageEvent event = new NetworkRootLocateStorageEvent(this, StorageType.BARREL, true, false, Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return barrelSet;
    }

    @Nonnull
    public Set<BarrelIdentity> getOutputAbleBarrels() {

        if (this.outputAbleBarrels != null) {
            return this.outputAbleBarrels;
        }

        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Set<BarrelIdentity> barrelSet = ConcurrentHashMap.newKeySet();

        final Set<Location> monitor = new HashSet<>();
        monitor.addAll(this.outputOnlyMonitors);
        monitor.addAll(this.monitors);
        for (Location cellLocation : monitor) {
            final BlockFace face = NetworkDirectional.getSelectedFace(cellLocation);

            if (face == null) {
                continue;
            }

            final Location testLocation = cellLocation.clone().add(face.getDirection());

            if (addedLocations.contains(testLocation)) {
                continue;
            } else {
                addedLocations.add(testLocation);
            }

            final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(testLocation);

            if (Networks.getSupportedPluginManager().isInfinityExpansion() && slimefunItem instanceof StorageUnit unit) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final InfinityBarrel infinityBarrel = getInfinityBarrel(menu, unit);
                if (infinityBarrel != null) {
                    barrelSet.add(infinityBarrel);
                }
                continue;
            }
            if (Networks.getSupportedPluginManager().isFluffyMachines() && slimefunItem instanceof Barrel barrel) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final FluffyBarrel fluffyBarrel = getFluffyBarrel(menu, barrel);
                if (fluffyBarrel != null) {
                    barrelSet.add(fluffyBarrel);
                }
                continue;
            }
            if (slimefunItem instanceof NetworkQuantumStorage) {
                final BlockMenu menu = StorageCacheUtils.getMenu(testLocation);
                if (menu == null) {
                    continue;
                }
                final NetworkStorage storage = getNetworkStorage(menu);
                if (storage != null) {
                    barrelSet.add(storage);
                }
            }
        }

        this.outputAbleBarrels = barrelSet;
        this.mapOutputAbleBarrels = new HashMap<>();
        for (BarrelIdentity storage : barrelSet) {
            this.mapOutputAbleBarrels.put(storage.getLocation(), storage);
        }
        NetworkRootLocateStorageEvent event = new NetworkRootLocateStorageEvent(this, StorageType.BARREL, false, true, Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return barrelSet;
    }

    @Nonnull
    public Map<StorageUnitData, Location> getInputAbleCargoStorageUnitDatas() {
        if (this.inputAbleCargoStorageUnitDatas != null) {
            return this.inputAbleCargoStorageUnitDatas;
        }

        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Map<StorageUnitData, Location> dataSet = new HashMap<>();

        final Set<Location> monitor = new HashSet<>();
        monitor.addAll(this.inputOnlyMonitors);
        monitor.addAll(this.monitors);
        for (Location cellLocation : monitor) {
            final BlockFace face = NetworkDirectional.getSelectedFace(cellLocation);

            if (face == null) {
                continue;
            }

            final Location testLocation = cellLocation.clone().add(face.getDirection());

            if (addedLocations.contains(testLocation)) {
                continue;
            } else {
                addedLocations.add(testLocation);
            }

            final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(testLocation);

            if (slimefunItem instanceof NetworksDrawer) {
                final StorageUnitData data = getCargoStorageUnitData(testLocation);
                if (data != null) {
                    dataSet.put(data, testLocation);
                }
            }
        }

        this.inputAbleCargoStorageUnitDatas = dataSet;
        this.mapInputAbleCargoStorageUnits = new HashMap<>();
        for (Map.Entry<StorageUnitData, Location> entry : dataSet.entrySet()) {
            mapInputAbleCargoStorageUnits.put(entry.getValue(), entry.getKey());
        }
        NetworkRootLocateStorageEvent event = new NetworkRootLocateStorageEvent(this, StorageType.DRAWER, true, false, Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return dataSet;
    }

    @Nonnull
    public Map<StorageUnitData, Location> getOutputAbleCargoStorageUnitDatas() {
        if (this.outputAbleCargoStorageUnitDatas != null) {
            return this.outputAbleCargoStorageUnitDatas;
        }

        final Set<Location> addedLocations = ConcurrentHashMap.newKeySet();
        final Map<StorageUnitData, Location> dataSet = new HashMap<>();

        final Set<Location> monitor = new HashSet<>();
        monitor.addAll(this.outputOnlyMonitors);
        monitor.addAll(this.monitors);
        for (Location cellLocation : monitor) {
            final BlockFace face = NetworkDirectional.getSelectedFace(cellLocation);

            if (face == null) {
                continue;
            }

            final Location testLocation = cellLocation.clone().add(face.getDirection());

            if (addedLocations.contains(testLocation)) {
                continue;
            } else {
                addedLocations.add(testLocation);
            }

            final SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(testLocation);

            if (slimefunItem instanceof NetworksDrawer) {
                final StorageUnitData data = getCargoStorageUnitData(testLocation);
                if (data != null) {
                    dataSet.put(data, testLocation);
                }
            }
        }

        this.outputAbleCargoStorageUnitDatas = dataSet;
        this.mapOutputAbleCargoStorageUnits = new HashMap<>();
        for (Map.Entry<StorageUnitData, Location> entry : dataSet.entrySet()) {
            mapOutputAbleCargoStorageUnits.put(entry.getValue(), entry.getKey());
        }
        NetworkRootLocateStorageEvent event = new NetworkRootLocateStorageEvent(this, StorageType.DRAWER, false, true, Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return dataSet;
    }

    public boolean refreshRootItems() {
        this.barrels = null;
        this.cargoStorageUnitDatas = null;
        this.inputAbleBarrels = null;
        this.outputAbleBarrels = null;
        this.inputAbleCargoStorageUnitDatas = null;
        this.outputAbleCargoStorageUnitDatas = null;

        getBarrels();
        getCargoStorageUnitDatas();
        getInputAbleBarrels();
        getOutputAbleBarrels();
        getInputAbleCargoStorageUnitDatas();
        getOutputAbleCargoStorageUnitDatas();
        return true;
    }

    @Nullable
    public BarrelIdentity accessInputAbleBarrel(Location barrelLocation) {
        return getMapInputAbleBarrels().get(barrelLocation);
    }

    @Nullable
    public BarrelIdentity accessOutputAbleBarrel(Location barrelLocation) {
        return getMapOutputAbleBarrels().get(barrelLocation);
    }

    @Nullable
    public StorageUnitData accessInputAbleDrawerData(Location drawerLocation) {
        return accessInputAbleCargoStorageUnitData(drawerLocation);
    }

    @Nullable
    public StorageUnitData accessOutputAbleDrawerData(Location drawerLocation) {
        return accessOutputAbleCargoStorageUnitData(drawerLocation);
    }

    @Nullable
    public StorageUnitData accessInputAbleCargoStorageUnitData(Location storageUnitLocation) {
        return getMapInputAbleCargoStorageUnits().get(storageUnitLocation);
    }

    @Nullable
    public StorageUnitData accessOutputAbleCargoStorageUnitData(Location storageUnitLocation) {
        return getMapOutputAbleCargoStorageUnits().get(storageUnitLocation);
    }

    @Nullable
    public ItemStack requestItem(@Nonnull Location accessor, @Nonnull ItemRequest request) {
        return getItemStack0(accessor, request);
    }

    @Nullable
    public ItemStack requestItem(@Nonnull Location accessor, @Nonnull ItemStack itemStack) {
        return requestItem(accessor, new ItemRequest(itemStack, itemStack.getAmount()));
    }

    public void tryRecord(@Nonnull Location accessor, @Nonnull ItemRequest request) {
        if (recordFlow && itemFlowRecord != null) {
            itemFlowRecord.addAction(accessor, request);
        }
    }

    @Nullable
    public ItemStack getItemStack0(@Nonnull Location accessor, @Nonnull ItemRequest request) {
        ItemStack stackToReturn = null;

        if (request.getAmount() <= 0) {
            return null;
        }

        if (!allowAccessOutput(accessor)) {
            return null;
        }

        var m = getPersistentAccessHistory(accessor);
        if (m != null) {
            for (var entry : m.entrySet()) {
                // try cache first
                BarrelIdentity barrelIdentity = accessOutputAbleBarrel(entry.getKey());
                if (barrelIdentity != null) {
                    //<editor-fold desc="do barrel">
                    final ItemStack itemStack = barrelIdentity.getItemStack();

                    if (itemStack == null || !StackUtils.itemsMatch(request, itemStack)) {
                        // Netex - Cache start
                        addCacheMiss(accessor, entry.getKey());
                        // Netex - Cache end
                        continue;
                    }

                    // Netex - Cache start
                    minusCacheMiss(accessor, entry.getKey());
                    // Netex - Cache end

                    boolean infinity = barrelIdentity instanceof InfinityBarrel;
                    final ItemStack fetched = barrelIdentity.requestItem(request);
                    if (fetched == null || fetched.getType() == Material.AIR || (infinity && fetched.getAmount() == 1)) {
                        continue;
                    }

                    // Stack is null, so we can fill it here
                    if (stackToReturn == null) {
                        stackToReturn = fetched.clone();
                        stackToReturn.setAmount(0);
                    }

                    final int preserveAmount = infinity ? fetched.getAmount() - 1 : fetched.getAmount();

                    if (request.getAmount() <= preserveAmount) {
                        // Netex - Reduce start
                        unreduceAccessOutput(accessor);
                        // Netex - Reduce end
                        stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                        fetched.setAmount(fetched.getAmount() - request.getAmount());
                        // Netex - Record start
                        tryRecord(accessor, request);
                        // Netex - Record end
                        return stackToReturn;
                    } else {
                        stackToReturn.setAmount(stackToReturn.getAmount() + preserveAmount);
                        request.receiveAmount(preserveAmount);
                        fetched.setAmount(fetched.getAmount() - preserveAmount);
                    }
                    //</editor-fold>
                } else {
                    StorageUnitData data = accessOutputAbleCargoStorageUnitData(entry.getKey());
                    if (data != null) {
                        //<editor-fold desc="do drawer">
                        ItemStack take = data.requestItem0(accessor, request);
                        if (take != null) {
                            // Netex - Cache start
                            minusCacheMiss(accessor, entry.getKey());
                            // Netex - Cache end

                            if (stackToReturn == null) {
                                stackToReturn = take.clone();
                            } else {
                                stackToReturn.setAmount(stackToReturn.getAmount() + take.getAmount());
                            }
                            request.receiveAmount(take.getAmount());

                            if (request.getAmount() <= 0) {
                                // Netex - Reduce start
                                unreduceAccessOutput(accessor);
                                // Netex - Reduce end
                                // Netex - Record start
                                tryRecord(accessor, request);
                                // Netex - Record end
                                return stackToReturn;
                            }
                        } else {
                            // Netex - Cache start
                            addCacheMiss(accessor, entry.getKey());
                            // Netex - Cache end
                        }
                        //</editor-fold>
                    } else {
                        // Netex - Cache start
                        addCacheMiss(accessor, entry.getKey());
                        // Netex - Cache end
                    }
                }
            }
        }

        // Barrels first
        for (BarrelIdentity barrelIdentity : getOutputAbleBarrels()) {
            //<editor-fold desc="do barrel">
            final ItemStack itemStack = barrelIdentity.getItemStack();

            if (itemStack == null || !StackUtils.itemsMatch(request, itemStack)) {
                continue;
            }

            // Netex - Cache start
            addCountObservingAccessHistory(accessor, barrelIdentity.getLocation());
            // Netex - Cache end

            boolean infinity = barrelIdentity instanceof InfinityBarrel;
            final ItemStack fetched = barrelIdentity.requestItem(request);
            if (fetched == null || fetched.getType() == Material.AIR || (infinity && fetched.getAmount() == 1)) {
                continue;
            }

            // Stack is null, so we can fill it here
            if (stackToReturn == null) {
                stackToReturn = fetched.clone();
                stackToReturn.setAmount(0);
            }

            final int preserveAmount = infinity ? fetched.getAmount() - 1 : fetched.getAmount();

            if (request.getAmount() <= preserveAmount) {
                // Netex - Reduce start
                unreduceAccessOutput(accessor);
                // Netex - Reduce end
                stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                fetched.setAmount(fetched.getAmount() - request.getAmount());
                // Netex - Record start
                tryRecord(accessor, request);
                // Netex - Record end
                return stackToReturn;
            } else {
                stackToReturn.setAmount(stackToReturn.getAmount() + preserveAmount);
                request.receiveAmount(preserveAmount);
                fetched.setAmount(fetched.getAmount() - preserveAmount);
            }
            //</editor-fold>
        }

        // Units
        for (StorageUnitData cache : getOutputAbleCargoStorageUnitDatas().keySet()) {
            //<editor-fold desc="do drawer">
            ItemStack take = cache.requestItem0(accessor, request);
            if (take != null) {
                // Netex - Cache start
                addCountObservingAccessHistory(accessor, cache.getLastLocation());
                // Netex - Cache end
                if (stackToReturn == null) {
                    stackToReturn = take.clone();
                } else {
                    stackToReturn.setAmount(stackToReturn.getAmount() + take.getAmount());
                }
                request.receiveAmount(take.getAmount());

                if (request.getAmount() <= 0) {
                    // Netex - Reduce start
                    unreduceAccessOutput(accessor);
                    // Netex - Reduce end
                    // Netex - Record start
                    tryRecord(accessor, request);
                    // Netex - Record end
                    return stackToReturn;
                }
            }
            //</editor-fold>
        }

        // Cells
        for (BlockMenu blockMenu : getCellMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null
                        || itemStack.getType() == Material.AIR
                        || !StackUtils.itemsMatch(request, itemStack)
                ) {
                    continue;
                }

                // Mark the Cell as dirty otherwise the changes will not save on shutdown
                blockMenu.markDirty();

                // If the return stack is null, we need to set it up
                if (stackToReturn == null) {
                    stackToReturn = itemStack.clone();
                    stackToReturn.setAmount(0);
                }

                if (request.getAmount() <= itemStack.getAmount()) {
                    // Netex - Reduce start
                    unreduceAccessOutput(accessor);
                    // Netex - Reduce end
                    // We can't take more than this stack. Level to request amount, remove items and then return
                    stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                    itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                    // Netex - Record start
                    tryRecord(accessor, request);
                    // Netex - Record end
                    return stackToReturn;
                } else {
                    // We can take more than what is here, consume before trying to take more
                    stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                    request.receiveAmount(itemStack.getAmount());
                    itemStack.setAmount(0);
                }
            }
        }

        // Crafters
        for (BlockMenu blockMenu : getCrafterOutputs()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null || itemStack.getType() == Material.AIR || !StackUtils.itemsMatch(
                        request,
                        itemStack
                )) {
                    continue;
                }

                // Stack is null, so we can fill it here
                if (stackToReturn == null) {
                    stackToReturn = itemStack.clone();
                    stackToReturn.setAmount(0);
                }

                if (request.getAmount() <= itemStack.getAmount()) {
                    // Netex - Reduce start
                    unreduceAccessOutput(accessor);
                    // Netex - Reduce end
                    stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                    itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                    // Netex - Record start
                    tryRecord(accessor, request);
                    // Netex - Record end
                    return stackToReturn;
                } else {
                    stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                    request.receiveAmount(itemStack.getAmount());
                    itemStack.setAmount(0);
                }
            }
        }

        for (BlockMenu blockMenu : getAdvancedGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            for (int slot : slots) {
                final ItemStack itemStack = blockMenu.getItemInSlot(slot);
                if (itemStack == null || itemStack.getType() == Material.AIR || !StackUtils.itemsMatch(
                        request,
                        itemStack
                )) {
                    continue;
                }

                // Stack is null, so we can fill it here
                if (stackToReturn == null) {
                    stackToReturn = itemStack.clone();
                    stackToReturn.setAmount(0);
                }

                if (request.getAmount() <= itemStack.getAmount()) {
                    // Netex - Reduce start
                    unreduceAccessOutput(accessor);
                    // Netex - Reduce end
                    stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                    itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                    // Netex - Record start
                    tryRecord(accessor, request);
                    // Netex - Record end
                    return stackToReturn;
                } else {
                    stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                    request.receiveAmount(itemStack.getAmount());
                    itemStack.setAmount(0);
                }
            }
        }

        // Greedy Blocks
        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            int[] slots = blockMenu.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
            final ItemStack itemStack = blockMenu.getItemInSlot(slots[0]);
            if (itemStack == null
                    || itemStack.getType() == Material.AIR
                    || !StackUtils.itemsMatch(request, itemStack)
            ) {
                continue;
            }

            // Mark the Cell as dirty otherwise the changes will not save on shutdown
            blockMenu.markDirty();

            // If the return stack is null, we need to set it up
            if (stackToReturn == null) {
                stackToReturn = itemStack.clone();
                stackToReturn.setAmount(0);
            }

            if (request.getAmount() <= itemStack.getAmount()) {
                // Netex - Reduce start
                unreduceAccessOutput(accessor);
                // Netex - Reduce end
                // We can't take more than this stack. Level to request amount, remove items and then return
                stackToReturn.setAmount(stackToReturn.getAmount() + request.getAmount());
                itemStack.setAmount(itemStack.getAmount() - request.getAmount());
                // Netex - Record start
                tryRecord(accessor, request);
                // Netex - Record end
                return stackToReturn;
            } else {
                // We can take more than what is here, consume before trying to take more
                stackToReturn.setAmount(stackToReturn.getAmount() + itemStack.getAmount());
                request.receiveAmount(itemStack.getAmount());
                itemStack.setAmount(0);
            }
        }

        if (stackToReturn == null || stackToReturn.getAmount() == 0) {
            addTransportOutputMiss(accessor);
            return null;
        }

        // Netex - Reduce start
        unreduceAccessOutput(accessor);
        // Netex - Reduce end
        // Netex - Record start
        tryRecord(accessor, request);
        // Netex - Record end

        return stackToReturn;
    }

    public void addItem(@Nonnull Location accessor, @Nonnull ItemStack incoming) {
        addItemStack0(accessor, incoming);
    }

    public void tryRecord(@Nonnull Location accessor, @Nullable ItemStack before, int after) {
        if (recordFlow && itemFlowRecord != null && before != null) {
            itemFlowRecord.addAction(accessor, before, after);
        }
    }

    public void addItemStack0(@Nonnull Location accessor, @Nonnull ItemStack incoming) {
        if (!allowAccessInput(accessor)) {
            return;
        }

        ItemStack beforeItemStack = null;
        if (recordFlow && itemFlowRecord != null) {
            beforeItemStack = incoming.clone();
        }

        int before = incoming.getAmount();

        var m = getPersistentAccessHistory(accessor);
        if (m != null) {
            for (var entry : m.entrySet()) {
                BarrelIdentity barrelIdentity = accessInputAbleBarrel(entry.getKey());
                if (barrelIdentity != null) {
                    //<editor-fold desc="do barrel">
                    if (StackUtils.itemsMatch(barrelIdentity, incoming)) {
                        // Netex - Cache start
                        minusCacheMiss(accessor, entry.getKey());
                        // Netex - Cache end

                        barrelIdentity.depositItemStack(incoming);

                        // All distributed, can escape
                        if (incoming.getAmount() == 0) {
                            // Netex - Reduce start
                            unreduceAccessInput(accessor);
                            // Netex - Reduce end
                            // Netex - Record start
                            tryRecord(accessor, beforeItemStack, 0);
                            // Netex - Record end
                            return;
                        }
                    } else {
                        // Netex - Cache start
                        addCacheMiss(accessor, barrelIdentity.getLocation());
                        // Netex - Cache end
                    }
                    //</editor-fold>
                } else {
                    StorageUnitData data = accessInputAbleCargoStorageUnitData(entry.getKey());
                    if (data != null) {
                        // Netex - Cache start
                        int before2 = incoming.getAmount();
                        // Netex - Cache end
                        data.depositItemStack0(accessor, incoming, true);

                        // Netex - Cache start
                        if (incoming.getAmount() == before2) {
                            addCacheMiss(accessor, entry.getKey());
                        } else {
                            minusCacheMiss(accessor, entry.getKey());
                        }
                        // Netex - Cache end

                        if (incoming.getAmount() == 0) {
                            // Netex - Reduce start
                            unreduceAccessInput(accessor);
                            // Netex - Reduce end
                            // Netex - Record start
                            tryRecord(accessor, beforeItemStack, 0);
                            // Netex - Record end
                            return;
                        }
                    } else {
                        // Netex - Cache start
                        addCacheMiss(accessor, entry.getKey());
                        // Netex - Cache end
                    }
                }
            }
        }

        for (BlockMenu blockMenu : getAdvancedGreedyBlockMenus()) {
            final ItemStack template = blockMenu.getItemInSlot(AdvancedGreedyBlock.TEMPLATE_SLOT);

            if (template == null || template.getType() == Material.AIR || !StackUtils.itemsMatch(incoming, template)) {
                continue;
            }

            blockMenu.markDirty();
            BlockMenuUtil.pushItem(blockMenu, incoming, ADVANCED_GREEDY_BLOCK_AVAILABLE_SLOTS);
            // Netex - Reduce start
            unreduceAccessInput(accessor);
            // Netex - Reduce end
            // Netex - Record start
            tryRecord(accessor, beforeItemStack, incoming.getAmount());
            // Netex - Record end
            // Given we have found a match, it doesn't matter if the item moved or not, we will not bring it in
            return;
        }

        // Run for matching greedy blocks
        for (BlockMenu blockMenu : getGreedyBlockMenus()) {
            final ItemStack template = blockMenu.getItemInSlot(NetworkGreedyBlock.TEMPLATE_SLOT);

            if (template == null || template.getType() == Material.AIR || !StackUtils.itemsMatch(incoming, template)) {
                continue;
            }

            blockMenu.markDirty();
            BlockMenuUtil.pushItem(blockMenu, incoming, GREEDY_BLOCK_AVAILABLE_SLOTS[0]);
            // Netex - Reduce start
            unreduceAccessInput(accessor);
            // Netex - Reduce end
            // Netex - Record start
            tryRecord(accessor, beforeItemStack, incoming.getAmount());
            // Netex - Record end
            // Given we have found a match, it doesn't matter if the item moved or not, we will not bring it in
            return;
        }


        // Run for matching barrels
        for (BarrelIdentity barrelIdentity : getInputAbleBarrels()) {
            //<editor-fold desc="do barrel">
            if (StackUtils.itemsMatch(barrelIdentity, incoming)) {
                // Netex - Cache start
                addCountObservingAccessHistory(accessor, barrelIdentity.getLocation());
                // Netex - Cache end

                barrelIdentity.depositItemStack(incoming);

                // All distributed, can escape
                if (incoming.getAmount() == 0) {
                    // Netex - Reduce start
                    unreduceAccessInput(accessor);
                    // Netex - Reduce end
                    // Netex - Record start
                    tryRecord(accessor, beforeItemStack, 0);
                    // Netex - Record end
                    return;
                }
            }
            //</editor-fold>
        }

        for (StorageUnitData cache : getInputAbleCargoStorageUnitDatas().keySet()) {
            // Netex - Cache start
            int before2 = incoming.getAmount();
            // Netex - Cache end

            cache.depositItemStack0(accessor, incoming, true);

            // Netex - Cache start
            if (incoming.getAmount() != before2) {
                // Netex - Reduce start
                unreduceAccessInput(accessor);
                // Netex - Reduce end
                addCountObservingAccessHistory(accessor, cache.getLastLocation());
            }
            // Netex - Cache end

            if (incoming.getAmount() == 0) {
                // Netex - Reduce start
                unreduceAccessInput(accessor);
                // Netex - Reduce end
                // Netex - Record start
                tryRecord(accessor, beforeItemStack, 0);
                // Netex - Record end
                return;
            }
        }

        for (BlockMenu blockMenu : getCellMenus()) {
            blockMenu.markDirty();
            BlockMenuUtil.pushItem(blockMenu, incoming, CELL_AVAILABLE_SLOTS);
            if (incoming.getAmount() == 0) {
                // Netex - Reduce start
                unreduceAccessInput(accessor);
                // Netex - Reduce end
                // Netex - Record start
                tryRecord(accessor, beforeItemStack, 0);
                // Netex - Record end
                return;
            }
        }

        // Netex - Reduce start
        if (before == incoming.getAmount()) {
            // No item moved, limit the accessor
            addTransportInputMiss(accessor);
        } else {
            unreduceAccessInput(accessor);
        }
        // Netex - Reduce end
        // Netex - Record start
        tryRecord(accessor, beforeItemStack, incoming.getAmount());
        // Netex - Record end
    }

    public Map<Location, BarrelIdentity> getMapInputAbleBarrels() {
        if (this.mapInputAbleBarrels != null) {
            return this.mapInputAbleBarrels;
        }

        this.mapInputAbleBarrels = new HashMap<>();
        for (var barrel : getInputAbleBarrels()) {
            this.mapInputAbleBarrels.put(barrel.getLocation(), barrel);
        }
        return this.mapInputAbleBarrels;
    }

    public Map<Location, BarrelIdentity> getMapOutputAbleBarrels() {
        if (this.mapOutputAbleBarrels != null) {
            return this.mapOutputAbleBarrels;
        }

        this.mapOutputAbleBarrels = new HashMap<>();
        for (var barrel : getOutputAbleBarrels()) {
            this.mapOutputAbleBarrels.put(barrel.getLocation(), barrel);
        }
        return this.mapOutputAbleBarrels;
    }

    public Map<Location, StorageUnitData> getMapInputAbleCargoStorageUnits() {
        if (this.mapInputAbleCargoStorageUnits != null) {
            return this.mapInputAbleCargoStorageUnits;
        }

        for (var entry : getInputAbleCargoStorageUnitDatas().entrySet()) {
            this.mapInputAbleCargoStorageUnits.put(entry.getValue(), entry.getKey());
        }

        return this.mapInputAbleCargoStorageUnits;
    }

    public Map<Location, StorageUnitData> getMapOutputAbleCargoStorageUnits() {
        if (this.mapOutputAbleCargoStorageUnits != null) {
            return this.mapOutputAbleCargoStorageUnits;
        }

        for (var entry : getOutputAbleCargoStorageUnitDatas().entrySet()) {
            this.mapOutputAbleCargoStorageUnits.put(entry.getValue(), entry.getKey());
        }

        return this.mapOutputAbleCargoStorageUnits;
    }

    public boolean allowAccessInput(@Nonnull Location accessor) {
        var lastTime = reducedAccessOutputHistory.get(accessor);
        if (lastTime == null) {
            return true;
        } else {
            return System.currentTimeMillis() - lastTime > reduceMs;
        }
    }

    public boolean allowAccessOutput(@Nonnull Location accessor) {
        var lastTime = reducedAccessInputHistory.get(accessor);
        if (lastTime == null) {
            return true;
        } else {
            return System.currentTimeMillis() - lastTime > reduceMs;
        }
    }

    public void addTransportInputMiss(@Nonnull Location location) {
        transportMissInputHistory.merge(location, 1, (a, b) -> {
            if (a + b > transportMissThreshold) {
                reduceAccessInput(location);
                return null;
            } else {
                return a + b;
            }
        });
    }

    public void addTransportOutputMiss(@Nonnull Location location) {
        transportMissOutputHistory.merge(location, 1, (a, b) -> {
            if (a + b > transportMissThreshold) {
                reduceAccessOutput(location);
                return null;
            } else {
                return a + b;
            }
        });
    }

    public void reduceAccessInput(@Nonnull Location accessor) {
        reducedAccessInputHistory.put(accessor, System.currentTimeMillis());
    }

    public void reduceAccessOutput(@Nonnull Location accessor) {
        reducedAccessOutputHistory.put(accessor, System.currentTimeMillis());
    }

    public void unreduceAccessInput(@Nonnull Location accessor) {
        reducedAccessInputHistory.remove(accessor);
    }

    public void unreduceAccessOutput(@Nonnull Location accessor) {
        reducedAccessOutputHistory.remove(accessor);
    }
}
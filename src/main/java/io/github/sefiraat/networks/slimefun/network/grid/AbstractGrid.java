package io.github.sefiraat.networks.slimefun.network.grid;

import java.text.Collator;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.balugaq.netex.api.enums.FeedbackType;
import com.balugaq.netex.api.helpers.Icon;
import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum;
import com.github.houbb.pinyin.util.PinyinHelper;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;

import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.network.GridItemRequest;
import io.github.sefiraat.networks.network.NetworkRoot;
import io.github.sefiraat.networks.network.NodeDefinition;
import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.slimefun.network.NetworkObject;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.sefiraat.networks.utils.Theme;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSetting;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.settings.IntRangeSetting;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import net.guizhanss.guizhanlib.minecraft.helper.inventory.ItemStackHelper;

@SuppressWarnings("deprecation")
public abstract class AbstractGrid extends NetworkObject {

    public static final Comparator<Map.Entry<ItemStack, Long>> ALPHABETICAL_SORT = Comparator.comparing(
            itemStackIntegerEntry -> {
                ItemStack itemStack = itemStackIntegerEntry.getKey();
                SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
                if (slimefunItem != null) {
                    return ChatColor.stripColor(slimefunItem.getItemName());
                } else {
                    return ChatColor.stripColor(ItemStackHelper.getDisplayName(itemStack));
                }
            },
            Collator.getInstance(Locale.CHINA)::compare
    );
    private static final Comparator<Map.Entry<ItemStack, Long>> NUMERICAL_SORT = Map.Entry.comparingByValue();
    private static final Comparator<Map.Entry<ItemStack, Long>> ADDON_SORT = Comparator.comparing(
            itemStackIntegerEntry -> {
                ItemStack itemStack = itemStackIntegerEntry.getKey();
                SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
                if (slimefunItem != null) {
                    return ChatColor.stripColor(slimefunItem.getAddon().getName());
                } else {
                    return "Minecraft";
                }
            },
            Collator.getInstance(Locale.CHINA)::compare
    );
    private static final Map<GridCache.SortOrder, Comparator<? super Map.Entry<ItemStack, Long>>> SORT_MAP = new HashMap<>();

    static {
        SORT_MAP.put(GridCache.SortOrder.ALPHABETICAL, ALPHABETICAL_SORT);
        SORT_MAP.put(GridCache.SortOrder.NUMBER, NUMERICAL_SORT.reversed());
        SORT_MAP.put(GridCache.SortOrder.ADDON, ADDON_SORT);
    }

    private final ItemSetting<Integer> tickRate;

    protected AbstractGrid(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe, NodeType.GRID);

        this.getSlotsToDrop().add(getInputSlot());

        this.tickRate = new IntRangeSetting(this, "tick_rate", 1, 1, 10);
        addItemSetting(this.tickRate);

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
                            final BlockMenu blockMenu = data.getBlockMenu();
                            if (blockMenu == null) {
                                return;
                            }
                            addToRegistry(block);
                            tryAddItem(blockMenu);
                            updateDisplay(blockMenu);
                        }
                    }

                    @Override
                    public void uniqueTick() {
                        tick = tick <= 1 ? tickRate.getValue() : tick - 1;
                    }
                }
        );
    }

    @Nonnull
    private static List<String> getLoreAddition(long amount) {
        final MessageFormat format = new MessageFormat(Networks.getLocalizationService().getString("messages.normal-operation.grid.item_amount"), Locale.ROOT);
        return List.of(
                "",
                format.format(new Object[]{Theme.CLICK_INFO.getColor(), Theme.PASSIVE.getColor(), amount}, new StringBuffer(), null).toString()
        );
    }

    protected void tryAddItem(@Nonnull BlockMenu blockMenu) {
        final ItemStack itemStack = blockMenu.getItemInSlot(getInputSlot());

        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }

        final NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());
        if (definition == null || definition.getNode() == null) {
            return;
        }

        definition.getNode().getRoot().addItemStack(itemStack);
    }


    protected void updateDisplay(@Nonnull BlockMenu blockMenu) {
        // No viewer - lets not bother updating
        if (!blockMenu.hasViewer()) {
            sendFeedback(blockMenu.getLocation(), FeedbackType.AFK);
            return;
        }

        final NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());

        // No node located, weird
        if (definition == null || definition.getNode() == null) {
            clearDisplay(blockMenu);
            sendFeedback(blockMenu.getLocation(), FeedbackType.NO_NETWORK_FOUND);
            return;
        }

        // Update Screen
        final NetworkRoot root = definition.getNode().getRoot();

        final GridCache gridCache = getCacheMap().get(blockMenu.getLocation().clone());
        final List<Map.Entry<ItemStack, Long>> entries = getEntries(root, gridCache);
        final int pages = (int) Math.ceil(entries.size() / (double) getDisplaySlots().length) - 1;

        gridCache.setMaxPages(pages);

        // Set everything to blank and return if there are no pages (no items)
        if (pages < 0) {
            clearDisplay(blockMenu);
            return;
        }

        // Reset selected page if it no longer exists due to items being removed
        if (gridCache.getPage() > pages) {
            gridCache.setPage(0);
        }

        final int start = gridCache.getPage() * getDisplaySlots().length;
        final int end = Math.min(start + getDisplaySlots().length, entries.size());
        final List<Map.Entry<ItemStack, Long>> validEntries = entries.subList(start, end);

        getCacheMap().put(blockMenu.getLocation(), gridCache);

        for (int i = 0; i < getDisplaySlots().length; i++) {
            if (validEntries.size() > i) {
                final Map.Entry<ItemStack, Long> entry = validEntries.get(i);
                final ItemStack displayStack = entry.getKey().clone();
                final ItemMeta itemMeta = displayStack.getItemMeta();
                if (itemMeta == null) {
                    continue;
                }
                List<String> lore = itemMeta.getLore();

                if (lore == null) {
                    lore = getLoreAddition(entry.getValue());
                } else {
                    lore.addAll(getLoreAddition(entry.getValue()));
                }

                itemMeta.setLore(lore);
                displayStack.setItemMeta(itemMeta);
                blockMenu.replaceExistingItem(getDisplaySlots()[i], displayStack);
                blockMenu.addMenuClickHandler(getDisplaySlots()[i], (player, slot, item, action) -> {
                    retrieveItem(player, item, action, blockMenu);
                    return false;
                });
            } else {
                blockMenu.replaceExistingItem(getDisplaySlots()[i], Icon.BLANK_SLOT_STACK);
                blockMenu.addMenuClickHandler(getDisplaySlots()[i], (p, slot, item, action) ->{
                    receiveItem(p, action, blockMenu);
                    return false;
                });
            }
        }
        sendFeedback(blockMenu.getLocation(), FeedbackType.WORKING);
    }

    protected void clearDisplay(BlockMenu blockMenu) {
        for (int displaySlot : getDisplaySlots()) {
            blockMenu.replaceExistingItem(displaySlot, Icon.BLANK_SLOT_STACK);
            blockMenu.addMenuClickHandler(displaySlot, (p, slot, item, action) -> false);
        }
    }

    @Nonnull
    protected List<Map.Entry<ItemStack, Long>> getEntries(@Nonnull NetworkRoot networkRoot, @Nonnull GridCache cache) {
        return networkRoot.getAllNetworkItemsLongType().entrySet().stream()
                .filter(entry -> {
                    if (cache.getFilter() == null) {
                        return true;
                    }

                    final ItemStack itemStack = entry.getKey();
                    String name = ChatColor.stripColor(ItemStackHelper.getDisplayName(itemStack).toLowerCase(Locale.ROOT));
                    if (cache.getFilter().matches("^[a-zA-Z]+$")) {
                        final String pinyinName = PinyinHelper.toPinyin(name, PinyinStyleEnum.INPUT, "");
                        final String pinyinFirstLetter = PinyinHelper.toPinyin(name, PinyinStyleEnum.FIRST_LETTER, "");
                        return name.contains(cache.getFilter()) || pinyinName.contains(cache.getFilter()) || pinyinFirstLetter.contains(cache.getFilter());
                    } else {
                        return name.contains(cache.getFilter());
                    }
                })
                .sorted(SORT_MAP.get(cache.getSortOrder()))
                .toList();
    }

    protected void setFilter(@Nonnull Player player, @Nonnull BlockMenu blockMenu, @Nonnull GridCache gridCache, @Nonnull ClickAction action) {
        if (action.isRightClicked()) {
            gridCache.setFilter(null);
        } else {
            player.closeInventory();
            player.sendMessage(Networks.getLocalizationService().getString("messages.normal-operation.grid.waiting_for_filter"));
            ChatUtils.awaitInput(player, s -> {
                if (s.isBlank()) {
                    return;
                }
                gridCache.setFilter(s.toLowerCase(Locale.ROOT));
                getCacheMap().put(blockMenu.getLocation(), gridCache);
                player.sendMessage(Networks.getLocalizationService().getString("messages.completed-operation.grid.filter_set"));

                SlimefunBlockData data = StorageCacheUtils.getBlock(blockMenu.getLocation());
                if (data == null) {
                    return;
                }

                if (blockMenu.getPreset().getID().equals(data.getSfId())) {
                    BlockMenu actualMenu = data.getBlockMenu();
                    if (actualMenu != null) {
                        updateDisplay(actualMenu);
                        actualMenu.open(player);
                    }
                }
            });
        }
    }

    @ParametersAreNonnullByDefault
    protected synchronized void retrieveItem(Player player, @Nullable ItemStack itemStack, ClickAction action, BlockMenu blockMenu) {
        NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());
        if (definition == null || definition.getNode() == null) {
            clearDisplay(blockMenu);
            blockMenu.close();
            Networks.getInstance().getLogger().warning(String.format(Networks.getLocalizationService().getString("messages.unsupported-operation.grid.may_duping"), player.getName(), blockMenu.getLocation()));
            return;
        }

        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }

        final ItemStack clone = itemStack.clone();
        final ItemMeta cloneMeta = clone.getItemMeta();
        if (cloneMeta == null) {
            return;
        }

        final List<String> cloneLore = cloneMeta.getLore();
        if (cloneLore == null || cloneLore.size() < 2) {
            return;
        }

        cloneLore.remove(cloneLore.size() - 1);
        cloneLore.remove(cloneLore.size() - 1);
        cloneMeta.setLore(cloneLore);
        clone.setItemMeta(cloneMeta);

        NetworkRoot root = definition.getNode().getRoot();
        boolean success = root.refreshRootItems();
        if (!success) {
            return;
        }

        final ItemStack cursor = player.getItemOnCursor();
        if (cursor.getType() != Material.AIR && !StackUtils.itemsMatch(clone, StackUtils.getAsQuantity(player.getItemOnCursor(), 1))) {
            root.addItemStack(player.getItemOnCursor());
            return;
        }

        int amount = 1;

        if (action.isRightClicked()) {
            amount = clone.getMaxStackSize();
        }

        final GridItemRequest request = new GridItemRequest(clone, amount, player);

        if (action.isShiftClicked()) {
            addToInventory(player, definition, request, action);
        } else {
            addToCursor(player, definition, request, action);
        }

        updateDisplay(blockMenu);
    }

    @ParametersAreNonnullByDefault
    private void addToInventory(Player player, NodeDefinition definition, GridItemRequest request, ClickAction action) {
        ItemStack requestingStack = definition.getNode().getRoot().getItemStack(request);

        if (requestingStack == null) {
            return;
        }

        HashMap<Integer, ItemStack> remnant = player.getInventory().addItem(requestingStack);
        requestingStack = remnant.values().stream().findFirst().orElse(null);
        if (requestingStack != null) {
            definition.getNode().getRoot().addItemStack(requestingStack);
        }
    }

    @ParametersAreNonnullByDefault
    private void addToCursor(Player player, NodeDefinition definition, GridItemRequest request, ClickAction action) {
        final ItemStack cursor = player.getItemOnCursor();

        // Quickly check if the cursor has an item and if we can add more to it
        if (cursor.getType() != Material.AIR && !canAddMore(action, cursor, request)) {
            return;
        }

        ItemStack requestingStack = definition.getNode().getRoot().getItemStack(request);
        setCursor(player, cursor, requestingStack);
    }

    private void setCursor(Player player, ItemStack cursor, ItemStack requestingStack) {
        if (requestingStack != null) {
            if (cursor.getType() != Material.AIR) {
                requestingStack.setAmount(cursor.getAmount() + 1);
            }
            player.setItemOnCursor(requestingStack);
        }
    }

    private boolean canAddMore(@Nonnull ClickAction action, @Nonnull ItemStack cursor, @Nonnull GridItemRequest request) {
        return !action.isRightClicked()
                && request.getAmount() == 1
                && cursor.getAmount() < cursor.getMaxStackSize()
                && StackUtils.itemsMatch(request, cursor);
    }

    @Override
    public void postRegister() {
        getPreset();
    }

    @Nonnull
    protected abstract BlockMenuPreset getPreset();

    @Nonnull
    protected abstract Map<Location, GridCache> getCacheMap();

    protected abstract int[] getBackgroundSlots();

    protected abstract int[] getDisplaySlots();

    protected abstract int getInputSlot();

    protected abstract int getChangeSort();

    protected abstract int getPagePrevious();

    protected abstract int getPageNext();

    protected abstract int getFilterSlot();

    protected ItemStack getBlankSlotStack() {
        return Icon.BLANK_SLOT_STACK;
    }

    protected ItemStack getPagePreviousStack() {
        return Icon.PAGE_PREVIOUS_STACK;
    }

    protected ItemStack getPageNextStack() {
        return Icon.PAGE_NEXT_STACK;
    }

    protected ItemStack getChangeSortStack() {
        return Icon.CHANGE_SORT_STACK;
    }

    protected ItemStack getFilterStack() {
        return Icon.FILTER_STACK;
    }
    public void receiveItem(Player player, ClickAction action, BlockMenu blockMenu) {
        NodeDefinition definition = NetworkStorage.getNode(blockMenu.getLocation());
        if (definition == null || definition.getNode() == null) {
            clearDisplay(blockMenu);
            blockMenu.close();
            Networks.getInstance().getLogger().warning(String.format(Networks.getLocalizationService().getString("messages.unsupported-operation.grid.may_duping"), player.getName(), blockMenu.getLocation()));
            return;
        }

        ItemStack cursor = player.getItemOnCursor();
        if (cursor != null && cursor.getType() != Material.AIR) {
            definition.getNode().getRoot().addItemStack(cursor);
        }
    }
}

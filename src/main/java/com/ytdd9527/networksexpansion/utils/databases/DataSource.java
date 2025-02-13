package com.ytdd9527.networksexpansion.utils.databases;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.balugaq.netex.api.data.ItemContainer;
import com.balugaq.netex.api.data.StorageUnitData;
import com.balugaq.netex.api.enums.StorageUnitType;
import com.ytdd9527.networksexpansion.implementation.machines.unit.NetworksDrawer;

import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.utils.itemstack.ItemStackWrapper;

public class DataSource {

    private final String ITEM_ID_KEY = "NEXT_ITEM_ID";
    private final String CONTAINER_ID_KEY = "NEXT_CONTAINER_ID";
    private final Logger logger;
    private final Map<Integer, ItemStack> itemMap;
    private final Map<String, String> environment;
    private Connection conn;
    private int nextContainerId = 0;
    private int nextItemId = 0;

    public DataSource() throws ClassNotFoundException, SQLException {

        connect();
        createTable();
        logger = Networks.getInstance().getLogger();
        // Load item ids
        itemMap = new HashMap<>();
        loadItemMap();
        // Load environment variables
        environment = new HashMap<>();
        loadEnvironment();

        init();

    }

    void saveNewStorageData(StorageUnitData storageData) {
        String sql = "INSERT INTO " + DataTables.CONTAINER + " VALUES(" +
                storageData.getId() + ",'" +
                storageData.getOwner().getUniqueId() + "'," +
                storageData.getSizeType().ordinal() + "," +
                (storageData.isPlaced() ? 1 : 0) + ",'" +
                DataStorage.formatLocation(storageData.getLastLocation()) + "');";
        scheduleExecute(sql, Networks.getLocalizationService().getString("messages.data-saving.error-occurred-when-saving-new-data"));
    }

    int getNextContainerId() {
        int re = nextContainerId++;

        String sql = "UPDATE " + DataTables.ENVIRONMENT + " SET VarValue = " + nextContainerId + " WHERE VarName = '" + CONTAINER_ID_KEY + "';";
        if (!environment.containsKey(CONTAINER_ID_KEY)) {
            sql = "INSERT INTO " + DataTables.ENVIRONMENT + " VALUES ('" + CONTAINER_ID_KEY + "'," + nextContainerId + ");";
            environment.put(CONTAINER_ID_KEY, "" + nextContainerId);
        }
        scheduleExecute(sql, Networks.getLocalizationService().getString("messages.data-saving.error-occurred-when-updating-environment-var"));

        return re;
    }

    @Nullable
    public StorageUnitData getStorageData(int id) {
        StorageUnitData re = null;

        String sql = "SELECT * FROM " + DataTables.CONTAINER + " WHERE ContainerID = " + id + ";";
        try (Statement stat = conn.createStatement();
             ResultSet result = stat.executeQuery(sql)
        ) {
            if (result.next()) {
                String[] locStr = result.getString("LastLocation").split(";");
                Location l = null;

                if (locStr.length == 4) {
                    World w = Bukkit.getWorld(UUID.fromString(locStr[0]));
                    if (w != null) {
                        l = new Location(w, Integer.parseInt(locStr[1]), Integer.parseInt(locStr[2]), Integer.parseInt(locStr[3]));
                    }
                }

                re = new StorageUnitData(result.getInt("ContainerID"), result.getString("PlayerUUID"), StorageUnitType.values()[result.getInt("SizeType")], result.getBoolean("IsPlaced"), l, getStoredItem(id));
            }
        } catch (SQLException e) {
            logger.warning(Networks.getLocalizationService().getString("messages.data-saving.error-occurred-when-loading-data"));
            e.printStackTrace();
        }
        return re;
    }

    int getItemId(ItemStack item) {
        ItemStack clone = item.clone();
        ItemStackWrapper wrapper = ItemStackWrapper.wrap(item);
        for (Map.Entry<Integer, ItemStack> each : itemMap.entrySet()) {
            if (StackUtils.itemsMatch(each.getValue(), wrapper)) {
                return each.getKey();
            }
        }

        // Not found, return new one and
        int re = nextItemId++;

        Networks.getQueryQueue().scheduleUpdate(() -> {
            try (Statement stat = conn.createStatement()) {
                // Update environment data
                String sql = "UPDATE " + DataTables.ENVIRONMENT + " SET VarValue = " + nextItemId + " WHERE VarName = '" + ITEM_ID_KEY + "';";
                if (!environment.containsKey(ITEM_ID_KEY)) {
                    sql = "INSERT INTO " + DataTables.ENVIRONMENT + " VALUES ('" + ITEM_ID_KEY + "'," + nextItemId + ");";
                    environment.put(ITEM_ID_KEY, "" + nextItemId);
                }
                stat.execute(sql);

                // Save item map
                stat.execute("INSERT INTO " + DataTables.ITEM_STACK + " VALUES (" + re + ",'" + getBase64String(clone) + "');");
                return true;
            } catch (SQLException | IOException e) {
                logger.warning(Networks.getLocalizationService().getString("messages.data-saving.error-occurred-when-saving-itemstack"));
                e.printStackTrace();
                return false;
            }
        });

        return re;
    }

    void updateContainer(int id, String key, String value) {
        String sql = "UPDATE " + DataTables.CONTAINER + " SET " + key + " = '" + value + "' WHERE ContainerID = " + id + ";";
        scheduleExecute(sql, Networks.getLocalizationService().getString("messages.data-saving.error-occurred-when-updating-container-data"));
    }

    void addStoredItem(int containerId, int itemId, int amount) {
        if (amount <= 0) return;
        String sql = "INSERT INTO " + DataTables.ITEM_STORED + " VALUES(" + containerId + "," + itemId + "," + amount + ");";
        scheduleExecute(sql, Networks.getLocalizationService().getString("messages.data-saving.error-occurred-when-updating-storage"));
    }

    void updateItemAmount(int containerId, int itemId, int amount) {
        if (NetworksDrawer.isLocked(containerId)) {
            if (amount < 0) {
                deleteStoredItem(containerId, itemId);
                return;
            }
        } else if (amount <= 0) {
            deleteStoredItem(containerId, itemId);
            return;
        }
        String sql = "UPDATE " + DataTables.ITEM_STORED + " SET Amount = " + amount + " WHERE ContainerID = " + containerId + " AND ItemID = " + itemId + ";";
        scheduleExecute(sql, Networks.getLocalizationService().getString("messages.data-saving.error-occurred-when-updating-storage"));
    }

    void deleteStoredItem(int containerId, int itemId) {
        String sql = "DELETE FROM " + DataTables.ITEM_STORED + " WHERE ContainerID = " + containerId + " AND ItemID = " + itemId + ";";
        scheduleExecute(sql, Networks.getLocalizationService().getString("messages.data-saving.error-occurred-when-updating-storage"));
    }

    int getIdFromLocation(Location l) {
        String sql = "SELECT ContainerID FROM " + DataTables.CONTAINER + " WHERE IsPlaced = 1 AND LastLocation = '" + DataStorage.formatLocation(l) + "';";
        try (Statement stat = conn.createStatement(); ResultSet resultSet = stat.executeQuery(sql)) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            logger.warning(Networks.getLocalizationService().getString("messages.data-saving.error-occurred-when-fixing-data"));
            e.printStackTrace();
        }
        return -1;
    }

    private void connect() throws SQLException, ClassNotFoundException {
        File dataFolder = Networks.getInstance().getDataFolder();
        if (!dataFolder.exists() || !dataFolder.isDirectory()) {
            if (!dataFolder.mkdir()) {
                throw new IllegalStateException(Networks.getLocalizationService().getString("messages.data-saving.error-occurred-when-creating-data-folder"));
            }
        }
        Class.forName("org.sqlite.JDBC");
        this.conn = DriverManager.getConnection("jdbc:sqlite:" + dataFolder.getPath() + "/CargoStorageUnits.db");
    }

    private void createTable() throws SQLException {
        try (Statement stat = conn.createStatement()) {
            stat.execute(DataTables.CONTAINER_CREATION);
            stat.execute(DataTables.ITEM_STACK_CREATION);
            stat.execute(DataTables.ITEM_STORED_CREATION);
            stat.execute(DataTables.ENVIRONMENT_CREATION);
        }
    }

    private void loadItemMap() {

        String sql = "SELECT Item, ItemID FROM " + DataTables.ITEM_STACK + ";";
        executeQuery(sql, result -> {
            try {
                while (result.next()) {
                    itemMap.put(result.getInt("ItemID"), getItemStack(result.getString("Item")));
                }
            } catch (SQLException | IOException | ClassNotFoundException e) {
                logger.warning(Networks.getLocalizationService().getString("messages.data-saving.error-occurred-when-loading-itemstack"));
                e.printStackTrace();
            }
        });

    }

    private void loadEnvironment() {

        String sql = "SELECT VarName, VarValue FROM " + DataTables.ENVIRONMENT + ";";
        executeQuery(sql, result -> {
            try {
                while (result.next()) {
                    environment.put(result.getString(1), result.getString(2));
                }
            } catch (SQLException e) {
                logger.warning(Networks.getLocalizationService().getString("messages.data-saving.error-occurred-when-loading-environment-var"));
                e.printStackTrace();
            }
        });
    }

    private void init() {
        String temp = environment.get(ITEM_ID_KEY);
        if (temp != null) {
            this.nextItemId = Integer.parseInt(temp);
        }

        temp = environment.get(CONTAINER_ID_KEY);
        if (temp != null) {
            this.nextContainerId = Integer.parseInt(temp);
        }
    }

    @Nonnull
    private Map<Integer, ItemContainer> getStoredItem(int id) {
        Map<Integer, ItemContainer> re = new HashMap<>();

        // Schedule query
        Networks.getQueryQueue().scheduleQuery(new QueuedTask() {
            private boolean success = true;

            @Override
            public boolean execute() {
                String sql = "SELECT ItemID, Amount FROM " + DataTables.ITEM_STORED + " WHERE ContainerID = " + id + ";";
                executeQuery(sql, result -> {
                    try {
                        while (result.next()) {
                            int itemId = result.getInt("ItemID");
                            ItemStack item = itemMap.get(itemId);
                            if (item != null) {
                                re.put(itemId, new ItemContainer(itemId, item, result.getInt("Amount")));
                            }
                        }
                    } catch (SQLException e) {
                        success = false;
                        logger.warning(Networks.getLocalizationService().getString("messages.data-saving.error-occurred-when-loading-storage"));
                        e.printStackTrace();
                    }
                });
                return success;
            }

            @Override
            public boolean callback() {
                // Update container data state
                DataStorage.setContainerLoaded(id);
                return false;
            }
        });

        return re;
    }

    private void executeQuery(String sql, Consumer<ResultSet> usage) {

        try (Statement stat = conn.createStatement();
             ResultSet result = stat.executeQuery(sql)
        ) {
            usage.accept(result);
        } catch (SQLException e) {
            logger.warning(Networks.getLocalizationService().getString("messages.data-saving.error-occurred-when-executing-query"));
            e.printStackTrace();
        }
    }

    private void scheduleExecute(String sql, String errorMsg) {
        Networks.getQueryQueue().scheduleUpdate(() -> {
            try (Statement stat = conn.createStatement()) {
                stat.execute(sql);
                return true;
            } catch (SQLException e) {
                logger.warning(errorMsg);
                e.printStackTrace();
                return false;
            }
        });
    }

    private String getBase64String(ItemStack item) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BukkitObjectOutputStream bs = new BukkitObjectOutputStream(stream);
        bs.writeObject(item);

        bs.close();
        return Base64Coder.encodeLines(stream.toByteArray());
    }

    private ItemStack getItemStack(String base64Str) throws IOException, ClassNotFoundException {
        ByteArrayInputStream stream = new ByteArrayInputStream(Base64Coder.decodeLines(base64Str));
        BukkitObjectInputStream bs = new BukkitObjectInputStream(stream);
        ItemStack re = (ItemStack) bs.readObject();
        bs.close();
        return re;
    }

}

package io.github.sefiraat.networks.network;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;

import io.github.sefiraat.networks.NetworkStorage;
import io.github.sefiraat.networks.Networks;
import io.github.sefiraat.networks.slimefun.network.NetworkController;
import io.github.sefiraat.networks.slimefun.network.NetworkPowerNode;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import lombok.Getter;
import lombok.ToString;

@ToString
public class NetworkNode {

    protected static final Set<BlockFace> VALID_FACES = EnumSet.of(
            BlockFace.UP,
            BlockFace.DOWN,
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    );

    @Getter
    protected final Set<NetworkNode> childrenNodes = new HashSet<>();
    protected final Location nodePosition;
    protected final NodeType nodeType;
    @Getter
    protected final long power;
    @Getter
    protected NetworkNode parent = null;
    protected NetworkRoot root = null;

    public NetworkNode(Location location, NodeType type) {
        this.nodePosition = location;
        this.nodeType = type;
        this.power = retrieveBlockCharge();
    }

    public void addChild(@Nonnull NetworkNode child) {
        child.setParent(this);
        child.setRoot(this.getRoot());
        this.root.addRootPower(child.getPower());
        this.root.registerNode(child.nodePosition, child.nodeType);
        this.childrenNodes.add(child);
    }

    @Nonnull
    public Location getNodePosition() {
        return nodePosition;
    }

    @Nonnull
    public NodeType getNodeType() {
        return nodeType;
    }

    public boolean networkContains(@Nonnull NetworkNode networkNode) {
        return networkContains(networkNode.nodePosition);
    }

    public boolean networkContains(@Nonnull Location location) {
        return this.root.getNodeLocations().contains(location);
    }

    @Nonnull
    public NetworkRoot getRoot() {
        return this.root;
    }

    private void setRoot(NetworkRoot root) {
        this.root = root;
    }

    private void setParent(NetworkNode parent) {
        this.parent = parent;
    }

    public void addAllChildren() {
        // Loop through all possible locations
        for (BlockFace face : VALID_FACES) {
            final Location testLocation = this.nodePosition.clone().add(face.getDirection());
            final NodeDefinition testDefinition = NetworkStorage.getNode(testLocation);

            if (testDefinition == null) {
                continue;
            }

            final NodeType testType = testDefinition.getType();

            // Kill additional controllers if it isn't the root
            if (testType == NodeType.CONTROLLER && !testLocation.equals(getRoot().nodePosition)) {
                killAdditionalController(testLocation);
                continue;
            }

            // Check if it's in the network already and, if not, create a child node and propagate further.
            if (testType != NodeType.CONTROLLER && !this.networkContains(testLocation)) {
                if (this.getRoot().getNodeCount() >= root.getMaxNodes()) {
                    this.getRoot().setOverburdened(true);
                    return;
                }
                final NetworkNode networkNode = new NetworkNode(testLocation, testType);
                addChild(networkNode);
                networkNode.addAllChildren();
                testDefinition.setNode(networkNode);
                NetworkStorage.registerNode(testLocation, testDefinition);
            }
        }
    }

    private void killAdditionalController(@Nonnull Location location) {
        var sfItem = StorageCacheUtils.getSfItem(location);
        if (sfItem != null) {
            Slimefun.getDatabaseManager().getBlockDataController().removeBlock(location);
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    //fix #99
                    NetworkController.wipeNetwork(location);
                    location.getWorld().dropItemNaturally(location, sfItem.getItem());
                    location.getBlock().setType(Material.AIR);
                }
            };
            runnable.runTask(Networks.getInstance());
            NetworkController.wipeNetwork(location);
        }
    }

    protected long retrieveBlockCharge() {
        if (this.nodeType == NodeType.POWER_NODE) {
            int blockCharge = 0;
            final SlimefunItem item = StorageCacheUtils.getSfItem(this.nodePosition);
            if (item instanceof NetworkPowerNode powerNode) {
                blockCharge = powerNode.getCharge(this.nodePosition);
            }
            return blockCharge;
        }
        return 0;
    }
}

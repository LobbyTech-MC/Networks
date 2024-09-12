package io.github.sefiraat.networks.network;

public enum NodeType {
    CONTROLLER,
    BRIDGE,
    STORAGE_MONITOR,
    IMPORT,
    EXPORT,
    GRID,
    CELL,
    WIPER,
    GRABBER,
    PUSHER,
    CUTTER,
    PASTER,
    VACUUM,
    PURGER,
    CRAFTER,
    POWER_NODE,
    POWER_OUTLET,
    POWER_DISPLAY,
    ENCODER,
    GREEDY_BLOCK,
    WIRELESS_TRANSMITTER,
    WIRELESS_RECEIVER,

    // from networks expansion
    ADVANCED_GREEDY_BLOCK,
    ADVANCED_IMPORT,
    ADVANCED_EXPORT,
    ADVANCED_PURGER,
    LINE_TRANSFER,
    LINE_TRANSFER_PUSHER,
    LINE_TRANSFER_GRABBER,
    LINE_TRANSFER_VANILLA_PUSHER,
    LINE_TRANSFER_VANILLA_GRABBER,
    INPUT_ONLY_MONITOR,
    OUTPUT_ONLY_MONITOR
}

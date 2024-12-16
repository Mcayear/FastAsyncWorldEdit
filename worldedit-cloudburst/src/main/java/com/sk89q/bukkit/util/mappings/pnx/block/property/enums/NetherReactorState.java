package com.sk89q.bukkit.util.mappings.pnx.block.property.enums;

public enum NetherReactorState {
    READY,

    INITIALIZED,

    FINISHED;
    
    private static final NetherReactorState[] values = values();
    
    public static NetherReactorState getFromData(int data) {
        return values[data];
    }
}

package com.sk89q.bukkit.util.mappings.pnx.block.property.enums;

/**
 * Automatically generated by {@code org.allaymc.codegen.VanillaBlockPropertyTypeGen} <br>
 * Allay Project <p>
 *
 * @author daoge_cmd
 */
public enum CrackedState {
    NO_CRACKS,
    CRACKED,
    MAX_CRACKED;

    public CrackedState next() {
        return CrackedState.values()[this.ordinal() + 1];
    }
}
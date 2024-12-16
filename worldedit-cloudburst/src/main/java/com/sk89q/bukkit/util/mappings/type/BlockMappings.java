package com.sk89q.bukkit.util.mappings.type;


import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import com.fastasyncworldedit.bukkit.adapter.BlockStateAdapter;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BlockMappings {

    Object2ObjectOpenHashMap<String, BlockStateAdapter> mapping1;
    Object2ObjectOpenHashMap<BlockStateAdapter, com.sk89q.worldedit.world.block.BlockState> mapping2;

    public BlockStateAdapter getPNXBlock(String faweBlockState) {
        final BlockStateAdapter blockState = mapping1.get(faweBlockState);
        if (blockState == null) {
            return new BlockStateAdapter(Block.get(BlockID.AIR));
        } else {
            return blockState;
        }
    }

    public com.sk89q.worldedit.world.block.BlockState getFAWEBlock(BlockStateAdapter nkxBlockState) {
        final com.sk89q.worldedit.world.block.BlockState faweBlockState = mapping2.get(nkxBlockState);
        if (faweBlockState == null) {
            return com.sk89q.worldedit.world.block.BlockState.get("minecraft:air");
        } else {
            return faweBlockState;
        }
    }

}

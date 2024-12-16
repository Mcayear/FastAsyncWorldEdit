package com.sk89q.worldedit.bukkit;

import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.biome.EnumBiome;
import cn.nukkit.level.format.Chunk;
import com.fastasyncworldedit.bukkit.util.ItemUtil;
import com.fastasyncworldedit.core.extent.processor.heightmap.HeightMapType;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.queue.IBlocks;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.util.NbtUtils;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;

public class NukkitGetBlocks_Copy implements IChunkGet {

    private static final Logger LOGGER = LogManagerCompat.getLogger();

    private final Map<BlockVector3, FaweCompoundTag> tiles = new HashMap<>();
    private final Set<FaweCompoundTag> entities = new HashSet<>();
    private final char[][] blocks;
    private final int minHeight;
    private final int maxHeight;
    final Level serverLevel;
    final Chunk levelChunk;
    private byte[][] biomes;

    protected NukkitGetBlocks_Copy(Level serverLevel, Chunk levelChunk) {
        this.levelChunk = levelChunk;
        this.serverLevel = serverLevel;
        this.minHeight = serverLevel.getMinBlockY() + 1;
        this.maxHeight = serverLevel.getMaxBlockY();
        this.blocks = new char[getSectionCount()][];
        this.biomes = new byte[getSectionCount()][4096];
    }

    protected void storeTile(BlockEntity blockEntity) {
        tiles.put(
                BlockVector3.at(
                        blockEntity.getFloorX(),
                        blockEntity.getFloorY(),
                        blockEntity.getFloorZ()
                ),
                ItemUtil.toFaweNBT(blockEntity.namedTag)
        );
    }

    @Override
    public Map<BlockVector3, FaweCompoundTag> tiles() {
        return tiles;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public FaweCompoundTag tile(final int x, final int y, final int z) {
        return tiles.get(BlockVector3.at(x, y, z));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void storeEntity(Entity entity) {
        entities.add(ItemUtil.toFaweNBT(entity.namedTag));
    }

    @Override
    public Collection<FaweCompoundTag> entities() {
        return this.entities;
    }

    /**
     * {@return the compound tag describing the entity with the given UUID, if any}
     *
     * @param uuid the uuid of the entity
     */
    @Nullable
    @Override
    public FaweCompoundTag entity(final UUID uuid) {
        for (FaweCompoundTag tag : entities) {
            if (uuid.equals(NbtUtils.uuid(tag))) {
                return tag;
            }
        }
        return null;
    }

    @Override
    public boolean isCreateCopy() {
        return false;
    }

    @Override
    public void setCreateCopy(boolean createCopy) {
    }

    @Override
    public void setLightingToGet(char[][] lighting, int minSectionPosition, int maxSectionPosition) {
    }

    @Override
    public void setSkyLightingToGet(char[][] lighting, int minSectionPosition, int maxSectionPosition) {
    }

    @Override
    public void setHeightmapToGet(HeightMapType type, int[] data) {
    }

    @Override
    public int getMaxY() {
        return maxHeight;
    }

    @Override
    public int getMinY() {
        return minHeight;
    }

    @Override
    public int getMaxSectionPosition() {
        return maxHeight >> 4;
    }

    @Override
    public int getMinSectionPosition() {
        return minHeight >> 4;
    }

    @Override
    public BiomeType getBiomeType(int x, int y, int z) {
        return BukkitAdapter.adapt(EnumBiome.getBiome(serverLevel.getBiomeId(x, z)));
    }

    @Override
    public void removeSectionLighting(int layer, boolean sky) {
    }

    @Override
    public boolean trim(boolean aggressive, int layer) {
        return false;
    }

    @Override
    public IBlocks reset() {
        return null;
    }

    @Override
    public int getSectionCount() {
        if (serverLevel.isOverWorld()) {
            return 24;
        } else {
            return 16;
        }
    }

    protected void storeSection(int layer, char[] data) {
        blocks[layer] = data;
    }

    protected void storeBiomes(int layer, byte[] biomeData) {
        Preconditions.checkArgument(biomeData.length == 4096);
        System.arraycopy(biomeData, 0, this.biomes[layer], 0, 4096);
    }

    @Override
    public BaseBlock getFullBlock(int x, int y, int z) {
        BlockState state = BlockTypesCache.states[get(x, y, z)];
        return state.toBaseBlock(this, x, y, z);
    }

    @Override
    public boolean hasSection(int layer) {
        layer -= getMinSectionPosition();
        return blocks[layer] != null;
    }

    @Override
    public char[] load(int layer) {
        layer -= getMinSectionPosition();
        return blocks[layer];
    }

    @Override
    public char[] loadIfPresent(int layer) {
        layer -= getMinSectionPosition();
        return blocks[layer];
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        return BlockTypesCache.states[get(x, y, z)];
    }

    @Override
    public int getSkyLight(int x, int y, int z) {
        return 0;
    }

    @Override
    public int getEmittedLight(int x, int y, int z) {
        return 0;
    }

    @Override
    public int[] getHeightMap(HeightMapType type) {
        return new int[0];
    }

    @Override
    public <T extends Future<T>> T call(IChunkSet set, Runnable finalize) {
        return null;
    }

    public char get(int x, int y, int z) {
        final int layer = (y >> 4) - getMinSectionPosition();
        final int index = (y & 15) << 8 | z << 4 | x;
        return blocks[layer][index];
    }


    @Override
    public boolean trim(boolean aggressive) {
        return false;
    }

}
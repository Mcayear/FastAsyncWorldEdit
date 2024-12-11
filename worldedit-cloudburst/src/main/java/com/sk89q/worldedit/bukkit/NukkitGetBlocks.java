package com.sk89q.worldedit.bukkit;

import cn.nukkit.block.BlockAir;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.biome.EnumBiome;
import cn.nukkit.level.format.ChunkSection;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.format.anvil.palette.BiomePalette;
//import cn.nukkit.level.format.palette.Palette;
import com.fastasyncworldedit.bukkit.util.ItemUtil;
import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extent.processor.heightmap.HeightMapType;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.queue.implementation.QueueHandler;
import com.fastasyncworldedit.core.queue.implementation.blocks.CharGetBlocks;
import com.google.common.base.Preconditions;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import com.sk89q.worldedit.world.entity.EntityType;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class NukkitGetBlocks extends CharGetBlocks {

    private static final Logger LOGGER = LogManagerCompat.getLogger();
    private final ReadWriteLock sectionLock = new ReentrantReadWriteLock();
    private final Level serverLevel;
    private final int chunkX;
    private final int chunkZ;
    private final int minHeight;
    private final int maxHeight;
    private boolean createCopy = false;
    private NukkitGetBlocks_Copy copy = null;
    private boolean forceLoadSections = true;
    private boolean lightUpdate = false;
    private ChunkSection[] nukkitChunkSections;
    private BaseFullChunk nukkitChunk;

    public NukkitGetBlocks(Level serverLevel, int chunkX, int chunkZ) {
        super(serverLevel.getDimensionData().getMinSectionY(), serverLevel.getDimensionData().getMaxSectionY());
        this.serverLevel = serverLevel;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.minHeight = serverLevel.getMinBlockY();
        this.maxHeight = serverLevel.getMaxBlockY();

        BaseFullChunk chunk = serverLevel.getChunk(chunkX, chunkZ);
        if (chunk == null) {
            chunk = serverLevel.getChunk(chunkX, chunkZ, true);
        }

        this.nukkitChunk = chunk;
        this.nukkitChunkSections = this.nukkitChunk.getSections();
    }

    @Override
    public BaseBlock getFullBlock(final int x, final int y, final int z) {
        return this.getBlock(x, y, z).toBaseBlock();
    }

    @Override
    public BlockState getBlock(final int x, final int y, final int z) {
        return BukkitAdapter.adapt(this.nukkitChunk.getBlockState(x & 15, y, z & 15));
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public Level getServerLevel() {
        return serverLevel;
    }

    @Override
    public boolean isCreateCopy() {
        return createCopy;
    }

    @Override
    public void setCreateCopy(boolean createCopy) {
        this.createCopy = createCopy;
    }

    @Override
    public IChunkGet getCopy() {
        return copy;
    }

    @Override
    public void setLightingToGet(char[][] light, int minSectionPosition, int maxSectionPosition) {
        if (light != null) {
            lightUpdate = true;
            try {
                fillLightNibble(light, LightLayer.BLOCK, minSectionPosition, maxSectionPosition);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setSkyLightingToGet(char[][] light, int minSectionPosition, int maxSectionPosition) {
        if (light != null) {
            lightUpdate = true;
            try {
                fillLightNibble(light, LightLayer.SKY, minSectionPosition, maxSectionPosition);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    enum LightLayer {
        SKY,
        BLOCK
    }

    @Override
    public void setHeightmapToGet(HeightMapType type, int[] data) {
        Preconditions.checkArgument(data.length == 256);
        for (int i = 0; i < data.length; i++) {
            nukkitChunk.getHeightMapArray()[i] = (byte) data[i];
        }
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
    public BiomeType getBiomeType(int x, int y, int z) {
        return BukkitAdapter.adapt(EnumBiome.getBiome(nukkitChunk.getBiomeId(x & 15, z & 15)));
    }

    @Override
    public void removeSectionLighting(int layer, boolean sky) {
        layer -= getMinSectionPosition();
        if (this.nukkitChunkSections[layer] != null && !(this.nukkitChunkSections[layer].isEmpty())) {
            lightUpdate = true;
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            this.nukkitChunkSections[layer].setBlockLight(x, y, z, (byte) 0);
                        }
                    }
                }
            if (sky) {
                    for (int x = 0; x < 16; x++) {
                        for (int y = 0; y < 16; y++) {
                            for (int z = 0; z < 16; z++) {
                                this.nukkitChunkSections[layer].setBlockSkyLight(x, y, z, (byte) 0);
                            }
                        }
                    }
            }
        }
    }

    @Override
    public CompoundTag getTile(int x, int y, int z) {
        return ItemUtil.toJNBT(this.nukkitChunk.getTile(x & 15, y, z & 15).namedTag);
    }

    @Override
    public Map<BlockVector3, CompoundTag> getTiles() {
        var map = new HashMap<BlockVector3, CompoundTag>();
        this.nukkitChunk.getBlockEntities().values()
                .forEach(entity -> map.put(
                        BlockVector3.at(entity.getFloorX(), entity.getFloorY(), entity.getFloorZ()),
                        ItemUtil.toJNBT(entity.namedTag)
                ));
        return map;
    }

    @Override
    public int getSkyLight(int x, int y, int z) {
        return this.nukkitChunk.getBlockSkyLight(x & 15, y, z & 15);
    }

    @Override
    public int getEmittedLight(int x, int y, int z) {
        return this.nukkitChunk.getBlockLight(x & 15, y, z & 15);
    }

    @Override
    public int[] getHeightMap(HeightMapType type) {
        int[] array = new int[256];
        for (int i = 0; i < this.nukkitChunk.getHeightMapArray().length; i++) {
            array[i] = this.nukkitChunk.getHeightMapArray()[i];
        }
        return array;
    }

    @Override
    public CompoundTag getEntity(UUID uuid) {
        return this.nukkitChunk.getEntities().values().stream()
                .filter(entity -> entity.getUniqueId().equals(uuid))
                .map(entity -> ItemUtil.toJNBT(entity.namedTag))
                .toList().get(0);
    }

    @Override
    public Set<CompoundTag> getEntities() {
        return this.nukkitChunk.getEntities().values().stream()
                .map(entity -> ItemUtil.toJNBT(entity.namedTag))
                .collect(Collectors.toSet());
    }

    private void removeEntity(Entity entity) {
        entity.kill();
    }

    public BaseFullChunk ensureLoaded(Level nmsWorld, int chunkX, int chunkZ) {
        return nmsWorld.getChunkIfLoaded(chunkX, chunkZ);
    }

    private void setChunkBlocks(final IChunkSet set) {
        for (int x = 0; x < 16; x++) {
            for (int y = set.getMinSectionPosition() * 16; y < set.getMaxSectionPosition() * 16 + 16; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockState combined = set.getBlock(x, y, z);
                    if (combined.getBlockType() == BlockTypes.__RESERVED__) {
                        continue;
                    }
                    if (combined.getBlockType() == BlockTypes.AIR ||
                            combined.getBlockType() == BlockTypes.CAVE_AIR ||
                            combined.getBlockType() == BlockTypes.VOID_AIR) {
                        nukkitChunk.setBlockState(x, y, z, BlockAir.STATE);
                    } else {
                        nukkitChunk.setBlockState(x, y, z, BukkitAdapter.adapt(combined));
                    }
                }
            }
        }
    }

    private void setSectionBlocks(final IChunkSet set) {
        for (int x = 0; x < 16; x++) {
            for (int y = set.getMinSectionPosition() * 16; y < set.getMaxSectionPosition() * 16 + 16; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockState combined = set.getBlock(x, y, z);
                    if (combined.getBlockType() == BlockTypes.__RESERVED__) {
                        continue;
                    }
                    if (combined.getBlockType() == BlockTypes.AIR ||
                            combined.getBlockType() == BlockTypes.CAVE_AIR ||
                            combined.getBlockType() == BlockTypes.VOID_AIR) {
                        nukkitChunk.setBlockState(x, y, z, BlockAir.STATE);
                    } else {
                        nukkitChunk.setBlockState(x, y, z, BukkitAdapter.adapt(combined));
                    }
                }
            }
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public synchronized <T extends Future<T>> T call(IChunkSet set, Runnable finalizer) {
        forceLoadSections = false;
        copy = createCopy ? new PNXGetBlocks_Copy(serverLevel, nukkitChunk) : null;
        try {
            Level nmsWorld = serverLevel;
            BaseFullChunk nmsChunk = ensureLoaded(nmsWorld, chunkX, chunkZ);
            // Remove existing tiles. Create a copy so that we can remove blocks
            Map<Long, BlockEntity> chunkTiles = new HashMap<>(nmsChunk.getBlockEntities());
            if (!chunkTiles.isEmpty()) {
                for (Map.Entry<Long, BlockEntity> entry : chunkTiles.entrySet()) {
                    final cn.nukkit.math.BlockVector3 pos = entry.getValue().getLocation().asBlockVector3();
                    final int lx = pos.getX() & 15;
                    final int ly = pos.getY();
                    final int lz = pos.getZ() & 15;
                    final int layer = ly >> 4;
                    if (!set.hasSection(layer)) {
                        continue;
                    }

                    int ordinal = set.getBlock(lx, ly, lz).getOrdinal();
                    if (ordinal != 0) {
                        BlockEntity tile = entry.getValue();
                        nmsChunk.removeBlockEntity(tile);
                        if (createCopy) {
                            copy.storeTile(tile);
                        }
                    }
                }
            }
            final BiomeType[][] biomes = set.getBiomes();
            int bitMask = 0;
            synchronized (nmsChunk) {
                //set section biome
                for (int layerNo = getMinSectionPosition(); layerNo <= getMaxSectionPosition(); layerNo++) {
                    int getSectionIndex = layerNo - getMinSectionPosition();
                    int setSectionIndex = layerNo - set.getMinSectionPosition();
                    int sectionIndex = sectionCount - getMinSectionPosition();
                    //store section
                    copy.storeSection(getSectionIndex, loadPrivately(layerNo));
                    if (!set.hasSection(layerNo)) {
                        if (biomes == null) {
                            continue;
                        }
                        if (layerNo < set.getMinSectionPosition() || layerNo > set.getMaxSectionPosition()) {
                            continue;
                        }
                        final BiomeType[] biome = biomes[setSectionIndex];
                        if (biome != null) {
                            synchronized (super.sectionLocks[getSectionIndex]) {
                                var existingSection = nukkitChunkSections[getSectionIndex];
                                if (existingSection == null) {
                                    var newSection = new ChunkSection((byte) layerNo);
                                    setSectionBiomes(biome, newSection);
                                    updateGet(nukkitChunk, nukkitChunkSections, newSection, new char[4096], getSectionIndex);
                                } else {
                                    setSectionBiomes(biome, existingSection);
                                }
                                if (createCopy) {
                                    assert existingSection != null;
                                    copy.storeBiomes(
                                            getSectionIndex,
                                            get3DBiomeDataArray(existingSection.biomes())
                                    );
                                }
                            }
                        }
                        continue;
                    }
                    bitMask |= 1 << sectionIndex;

                    char[] tmp = set.load(layerNo);
                    char[] setArr = new char[4096];
                    System.arraycopy(tmp, 0, setArr, 0, 4096);

                    synchronized (super.sectionLocks[getSectionIndex]) {
                        var existingSection = nukkitChunkSections[getSectionIndex];

                        if (createCopy) {
                            copy.storeSection(getSectionIndex, loadPrivately(layerNo));
                            if (biomes != null && existingSection != null) {
                                copy.storeBiomes(getSectionIndex, get3DBiomeDataArray(existingSection.biomes()));
                            }
                        }

                        if (existingSection == null) {
                            BiomeType[] biomeData;
                            if (biomes == null) {
                                biomeData = new BiomeType[64];
                                Arrays.fill(biomeData, BiomeTypes.PLAINS);
                            } else {
                                biomeData = biomes[setSectionIndex];
                            }
                            var newSection = new ChunkSection((byte) layerNo);
                            if (biomeData != null) {
                                setSectionBiomes(biomeData, newSection);
                                for (int y = 0, index = 0; y < 16; y++) {
                                    for (int z = 0; z < 16; z++) {
                                        for (int x = 0; x < 16; x++, index++) {
                                            BlockState combined = BlockState.getFromOrdinal(setArr[index]);
                                            if (combined.getBlockType() == BlockTypes.__RESERVED__) {
                                                continue;
                                            }
                                            if (combined.getBlockType() == BlockTypes.AIR ||
                                                    combined.getBlockType() == BlockTypes.CAVE_AIR ||
                                                    combined.getBlockType() == BlockTypes.VOID_AIR) {
                                                nukkitChunk.setBlockState(x, y, z, BlockAir.STATE);
                                            } else {
                                                nukkitChunk.setBlockState(x, y, z, BukkitAdapter.adapt(combined));
                                            }
                                        }
                                    }
                                }
                            }
                            updateGet(nukkitChunk, nukkitChunkSections, newSection, setArr, getSectionIndex);
                        }
                        //同步
                        try {
                            sectionLock.writeLock().lock();
                            if (this.getChunk() != nmsChunk) {
                                this.nukkitChunk = nmsChunk;
                                this.nukkitChunkSections = null;
                                this.reset();
                            } else if (existingSection != getSections(false)[getSectionIndex]) {
                                this.nukkitChunkSections[getSectionIndex] = existingSection;
                                this.reset();
                            } else if (!Arrays.equals(update(getSectionIndex, new char[4096], true), loadPrivately(layerNo))) {
                                this.reset(layerNo);
                            }
                        } finally {
                            sectionLock.writeLock().unlock();
                        }
                    }
                }
                //set block and state
                setChunkBlocks(set);
                //set Height Map
                Map<HeightMapType, int[]> heightMaps = set.getHeightMaps();
                for (Map.Entry<HeightMapType, int[]> entry : heightMaps.entrySet()) {
                    this.setHeightmapToGet(entry.getKey(), entry.getValue());
                }
                //set Lighting
                this.setLightingToGet(
                        set.getLight(),
                        set.getMinSectionPosition(),
                        set.getMaxSectionPosition()
                );
                //set SkyLighting
                this.setSkyLightingToGet(
                        set.getSkyLight(),
                        set.getMinSectionPosition(),
                        set.getMaxSectionPosition()
                );

                Runnable[] syncTasks = new Runnable[3];
                int bx = chunkX << 4;
                int bz = chunkZ << 4;

                //Remove Entity
                Set<UUID> entityRemoves = set.getEntityRemoves();
                if (entityRemoves != null && !entityRemoves.isEmpty()) {
                    syncTasks[2] = () -> {
                        Set<UUID> entitiesRemoved = new HashSet<>();
                        final var entities = nmsChunk.getEntities().values().iterator();
                        while (entities.hasNext()) {
                            var entity = entities.next();
                            var uuid = entity.getUniqueId();
                            if (entityRemoves.contains(uuid)) {
                                if (createCopy) {
                                    copy.storeEntity(entity);
                                }
                                removeEntity(entity);
                                entitiesRemoved.add(uuid);
                                entityRemoves.remove(uuid);
                                entities.remove();
                            }
                        }
                        if (Settings.settings().EXPERIMENTAL.REMOVE_ENTITY_FROM_WORLD_ON_CHUNK_FAIL) {
                            for (UUID uuid : entityRemoves) {
                                Entity entity = Arrays.stream(nmsWorld.getEntities()).filter(entity1 -> entity1
                                        .getUniqueId()
                                        .equals(uuid)).toList().get(0);
                                if (entity != null) {
                                    entitiesRemoved.add(uuid);
                                    removeEntity(entity);
                                }
                            }
                        }
                        // Only save entities that were actually removed to history
                        set.getEntityRemoves().clear();
                        set.getEntityRemoves().addAll(entitiesRemoved);
                    };
                }

                //set Entity
                Set<CompoundTag> entities = set.getEntities();
                if (entities != null && !entities.isEmpty()) {
                    syncTasks[1] = () -> {
                        for (final CompoundTag nativeTag : entities) {
                            final Map<String, Tag> entityTagMap = nativeTag.getValue();
                            final StringTag idTag = (StringTag) entityTagMap.get("Id");
                            final ListTag posTag = (ListTag) entityTagMap.get("Pos");
                            final ListTag rotTag = (ListTag) entityTagMap.get("Rotation");
                            if (idTag == null || posTag == null || rotTag == null) {
                                LOGGER.error("Unknown entity tag: {}", nativeTag);
                                continue;
                            }
                            final double x = posTag.getDouble(0);
                            final double y = posTag.getDouble(1);
                            final double z = posTag.getDouble(2);
                            final float yaw = rotTag.getFloat(0);
                            final float pitch = rotTag.getFloat(1);
                            final String id = idTag.getValue();

                            final EntityType entityType = EntityType.REGISTRY.get(id);
                            if (entityType != null) {
                                Entity entity = BukkitAdapter.adaptEntityType(entityType);
                                if (entity != null) {
                                    entity.setPosition(new Location(x, y, z, yaw, pitch, nmsWorld));
                                    entity.spawnToAll();
                                }
                            }
                        }
                    };
                }

                // set tiles
                Map<BlockVector3, CompoundTag> tiles = set.getTiles();
                if (tiles != null && !tiles.isEmpty()) {
                    syncTasks[0] = () -> {
                        for (final Map.Entry<BlockVector3, CompoundTag> entry : tiles.entrySet()) {
                            final CompoundTag nativeTag = entry.getValue();
                            final BlockVector3 blockHash = entry.getKey();
                            final int x = blockHash.getX() + bx;
                            final int y = blockHash.getY();
                            final int z = blockHash.getZ() + bz;
                            final cn.nukkit.math.BlockVector3 pos = new cn.nukkit.math.BlockVector3(x, y, z);

                            synchronized (nmsWorld) {
                                BlockEntity tileEntity = nmsWorld.getBlockEntity(pos);
                                if (tileEntity == null || tileEntity.closed) {
                                    nmsWorld.removeBlockEntity(tileEntity);
                                    tileEntity = nmsWorld.getBlockEntity(pos);
                                }
                                if (tileEntity != null) {
                                    cn.nukkit.nbt.tag.CompoundTag tag = (cn.nukkit.nbt.tag.CompoundTag) NBTConverter.toNative(
                                            nativeTag.asBinaryTag());
                                    String tileId = tag.getString("id");
                                    Map<String, cn.nukkit.nbt.tag.Tag> map = new HashMap<>();
                                    map.put("x", new cn.nukkit.nbt.tag.IntTag(x));
                                    map.put("y", new cn.nukkit.nbt.tag.IntTag(y));
                                    map.put("z", new cn.nukkit.nbt.tag.IntTag(z));
                                    BlockEntity ent = BlockEntity.createBlockEntity(
                                            tileId,
                                            nmsChunk,
                                            new cn.nukkit.nbt.tag.CompoundTag(map)
                                    );
                                    if (ent != null) {
                                        nmsChunk.addBlockEntity(ent);
                                    }
                                }
                            }
                        }
                    };
                }

                Runnable callback;
                if (bitMask == 0 && set.getBiomes() == null && !lightUpdate) {
                    callback = null;
                } else {
                    callback = () -> {
                        // Set Modified
                        nmsChunk.setChanged(true);
                        if (finalizer != null) {
                            finalizer.run();
                        }
                    };
                }
                QueueHandler queueHandler = Fawe.instance().getQueueHandler();
                // Chain the sync tasks and the callback
                Callable<Future> chain = () -> {
                    try {
                        // Run the sync tasks
                        for (Runnable task : syncTasks) {
                            if (task != null) {
                                task.run();
                            }
                        }
                        if (callback == null) {
                            if (finalizer != null) {
                                finalizer.run();
                            }
                            return null;
                        } else {
                            return queueHandler.async(callback, null);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        throw e;
                    }
                };
                //noinspection unchecked - required at compile time
                return (T) (Future) queueHandler.sync(chain);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        } finally {
            forceLoadSections = true;
        }
    }

    private void setSectionBiomes(final BiomeType[] biomes, final ChunkSection newSection) {
        for (int y = 0, index = 0; y < 4; y++) {
            for (int z = 0; z < 4; z++) {
                for (int x = 0; x < 4; x++, index++) {
                    BiomeType biomeType = biomes[index];
                    if (biomeType == null) {
                        continue;
                    }
                    for (int i = 0; i < 4; i++) {
                        newSection.setBiomeId(
                                x * 4 + i,
                                y * 4 + i,
                                z * 4 + i,
                                (byte) BukkitAdapter.adapt(biomeType)
                        );
                    }
                }
            }
        }
    }

    private byte[] get3DBiomeDataArray(BiomePalette biomesPalette) {
        final byte[] array = new byte[4096];
        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    final int i = y << 8 | z << 4 | x;
                    final int d = biomesPalette.get(i);
                    array[d] = (byte) d;
                }
            }
        }
        return array;
    }

    private char[] loadPrivately(int layer) {
        layer -= getMinSectionPosition();
        if (super.sections[layer] != null) {
            synchronized (super.sectionLocks[layer]) {
                if (super.sections[layer].isFull() && super.blocks[layer] != null) {
                    char[] blocks = new char[4096];
                    System.arraycopy(super.blocks[layer], 0, blocks, 0, 4096);
                    return blocks;
                }
            }
        }
        return this.update(layer, null, true);
    }

    @Override
    public char[] update(int layer, char[] data, boolean aggressive) {
        ChunkSection section = getSections(aggressive)[layer];
        // Section is null, return empty array
        if (section == null) {
            data = new char[4096];
            Arrays.fill(data, (char) BlockTypesCache.ReservedIDs.AIR);
            return data;
        }
        if (data != null && data.length != 4096) {
            data = new char[4096];
            Arrays.fill(data, (char) BlockTypesCache.ReservedIDs.AIR);
        }
        if (data == null || data == FaweCache.INSTANCE.EMPTY_CHAR_4096) {
            data = new char[4096];
            Arrays.fill(data, (char) BlockTypesCache.ReservedIDs.AIR);
        }

        for (int y = 0, index = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++, index++) {
                    var state = section.getBlockState(x, y, z);
                    if (state == BlockAir.STATE) {
                        data[index] = 0;
                    } else {
                        data[index] = BukkitAdapter.adapt(section.getBlockState(x, y, z)).getOrdinalChar();
                    }
                }
            }
        }
        return data;
    }

    private void updateGet(
            BaseFullChunk nmsChunk,
            ChunkSection[] chunkSections,
            ChunkSection section,
            char[] arr,
            int layer
    ) {
        try {
            sectionLock.writeLock().lock();
            if (this.getChunk() != nmsChunk) {
                this.nukkitChunk = nmsChunk;
                this.nukkitChunkSections = new ChunkSection[chunkSections.length];
                System.arraycopy(chunkSections, 0, this.nukkitChunkSections, 0, chunkSections.length);
                this.reset();
            }
            if (this.nukkitChunkSections == null) {
                this.nukkitChunkSections = new ChunkSection[chunkSections.length];
                System.arraycopy(chunkSections, 0, this.nukkitChunkSections, 0, chunkSections.length);
            }
            if (this.nukkitChunkSections[layer] != section) {
                // Not sure why it's funky, but it's what I did in commit fda7d00747abe97d7891b80ed8bb88d97e1c70d1 and I don't want to touch it >dords
                this.nukkitChunkSections[layer] = new ChunkSection[]{section}.clone()[0];
            }
        } finally {
            sectionLock.writeLock().unlock();
        }
        this.blocks[layer] = arr;
    }

    public ChunkSection[] getSections(boolean force) {
        force &= forceLoadSections;
        sectionLock.readLock().lock();
        ChunkSection[] tmp = nukkitChunkSections;
        sectionLock.readLock().unlock();
        if (tmp == null || force) {
            try {
                sectionLock.writeLock().lock();
                tmp = nukkitChunkSections;
                if (tmp == null || force) {
                    ChunkSection[] chunkSections = getChunk().getSections();
                    tmp = new ChunkSection[chunkSections.length];
                    System.arraycopy(chunkSections, 0, tmp, 0, chunkSections.length);
                    nukkitChunkSections = tmp;
                }
            } finally {
                sectionLock.writeLock().unlock();
            }
        }
        return tmp;
    }

    public BaseFullChunk getChunk() {
        BaseFullChunk levelChunk = this.nukkitChunk;
        if (levelChunk == null) {
            synchronized (this) {
                levelChunk = this.nukkitChunk;
                if (levelChunk == null) {
                    this.nukkitChunk = levelChunk = ensureLoaded(this.serverLevel, chunkX, chunkZ);
                }
            }
        }
        return levelChunk;
    }

    private void fillLightNibble(char[][] light, LightLayer lightLayer, int minSectionPosition, int maxSectionPosition) {
        for (int Y = 0; Y <= maxSectionPosition - minSectionPosition; Y++) {
            if (light[Y] == null || this.nukkitChunkSections[Y] == null) {
                continue;
            }
            if (this.nukkitChunkSections[Y].isEmpty()) {
                continue;
            }
            if (lightLayer == LightLayer.BLOCK) {
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            int i = y << 8 | z << 4 | x;
                            this.nukkitChunkSections[Y].setBlockLight(x, y, z, (byte) (light[Y][i] & 15));
                        }
                    }
                }
            } else if (lightLayer == LightLayer.SKY) {
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            int i = y << 8 | z << 4 | x;
                            this.nukkitChunkSections[Y].setBlockSkyLight(x, y, z, (byte) (light[Y][i] & 15));
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean hasSection(int layer) {
        layer -= getMinSectionPosition();
        return getSections(false)[layer] != null;
    }

    @Override
    public synchronized boolean trim(boolean aggressive) {
        if (aggressive) {
            sectionLock.writeLock().lock();
            nukkitChunkSections = null;
            this.nukkitChunk = null;
            sectionLock.writeLock().unlock();
            return super.trim(true);
        } else if (nukkitChunkSections == null) {
            // don't bother trimming if there are no sections stored.
            return true;
        } else {
            for (int i = getMinSectionPosition(); i <= getMaxSectionPosition(); i++) {
                int layer = i - getMinSectionPosition();
                if (!hasSection(i) || !super.sections[layer].isFull()) {
                    continue;
                }
                ChunkSection existing = getSections(true)[layer];
                super.trim(false, i);
            }
            return true;
        }
    }

}

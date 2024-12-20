package com.fastasyncworldedit.bukkit.adapter;

import com.fastasyncworldedit.core.queue.IChunkCache;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.implementation.SingleThreadQueueExtent;
import com.fastasyncworldedit.core.util.TaskManager;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

/**
 * Represents an abstract regeneration handler.
 */
public abstract class Regenerator {

    protected final cn.nukkit.level.Level originalBukkitWorld;
    protected final Region region;
    protected final Extent target;
    protected final RegenOptions options;

    //runtime
    protected long seed;
    protected SingleThreadQueueExtent source;

    /**
     * Initializes an abstract regeneration handler.
     *
     * @param originalBukkitWorld the Bukkit world containing all the information on how to regenerate the {code Region}
     * @param region              the selection to regenerate
     * @param target              the target {@code Extent} to paste the regenerated blocks into
     * @param options             the options to used while regenerating and pasting into the target {@code Extent}
     */
    public Regenerator(cn.nukkit.level.Level originalBukkitWorld, Region region, Extent target, RegenOptions options) {
        this.originalBukkitWorld = originalBukkitWorld;
        this.region = region;
        this.target = target;
        this.options = options;
    }

    /**
     * Regenerates the selected {@code Region}.
     *
     * @return whether the regeneration process was successful
     * @throws Exception when something goes terribly wrong
     */
    public boolean regenerate() throws Exception {
        if (!prepare()) {
            return false;
        }

        try {
            if (!initNewWorld()) {
                cleanup0();
                return false;
            }
        } catch (Exception e) {
            cleanup0();
            throw e;
        }

        try {
            copyToWorld();
        } catch (Exception e) {
            cleanup0();
            throw e;
        }

        cleanup0();
        return true;
    }

    /**
     * Execute tasks on the main thread during regen.
     */
    protected abstract void runTasks(BooleanSupplier shouldKeepTicking);

    private void createSource() {

        source = new SingleThreadQueueExtent(
                originalBukkitWorld.getDimensionData().getMinHeight(),
                originalBukkitWorld.getDimensionData().getMaxHeight()
        );
        source.init(target, initSourceQueueCache(), null);
    }

    private void copyToWorld() {
        createSource();
        final long timeoutPerTick = TimeUnit.MILLISECONDS.toNanos(10);
        int taskId = TaskManager.taskManager().repeat(() -> {
            final long startTime = System.nanoTime();
            runTasks(() -> System.nanoTime() - startTime < timeoutPerTick);
        }, 1);
        //Setting Blocks
        boolean genbiomes = options.shouldRegenBiomes();
        boolean hasBiome = options.hasBiomeType();
        BiomeType biome = options.getBiomeType();
        if (!genbiomes && !hasBiome) {
            target.setBlocks(region, new PlacementPattern());
        }
        if (hasBiome) {
            target.setBlocks(region, new WithBiomePlacementPattern(ignored -> biome));
        } else if (genbiomes) {
            target.setBlocks(region, new WithBiomePlacementPattern(vec -> source.getBiome(vec)));
        }
        TaskManager.taskManager().cancel(taskId);
    }

    private class PlacementPattern implements Pattern {

        @Override
        public BaseBlock applyBlock(final BlockVector3 position) {
            return source.getFullBlock(position);
        }

        @Override
        public boolean apply(final Extent extent, final BlockVector3 get, final BlockVector3 set) throws WorldEditException {
            return extent.setBlock(set.x(), set.y(), set.z(), source.getFullBlock(get.x(), get.y(), get.z()));
        }

    }

    private class WithBiomePlacementPattern implements Pattern {

        private final Function<BlockVector3, BiomeType> biomeGetter;

        private WithBiomePlacementPattern(final Function<BlockVector3, BiomeType> biomeGetter) {
            this.biomeGetter = biomeGetter;
        }

        @Override
        public BaseBlock applyBlock(final BlockVector3 position) {
            return source.getFullBlock(position);
        }

        @Override
        public boolean apply(final Extent extent, final BlockVector3 get, final BlockVector3 set) throws WorldEditException {
            return extent.setBlock(set.x(), set.y(), set.z(), source.getFullBlock(get.x(), get.y(), get.z()))
                    && extent.setBiome(set.x(), set.y(), set.z(), biomeGetter.apply(get));
        }

    }

    //functions to be implemented by sub class
    private void cleanup0() {
        cleanup();
    }

    /**
     * <p>Implement the preparation process in here. DO NOT instanciate any variable here that require the cleanup function. This function is for gathering further information before initializing a new
     * world.</p>
     *
     * <p>Fields required to be initialized: chunkStati, seed</p>
     * <p>For chunkStati also see {code ChunkStatusWrapper}.</p>
     *
     * @return whether or not the preparation process was successful
     */
    protected abstract boolean prepare();

    /**
     * Implement the creation of the seperate world in here.
     * <p>
     * Fields required to be initialized: generateConcurrent
     *
     * @return true if everything went fine, otherwise false. When false is returned the Regenerator halts the regeneration process and calls the cleanup function.
     * @throws Exception When the implementation of this method throws and exception the Regenerator halts the regeneration process and calls the cleanup function.
     */
    protected abstract boolean initNewWorld() throws Exception;

    //functions to implement by sub class - regenate related

    /**
     * Implement the cleanup of all the mess that is created during the regeneration process (initNewWorld() and generate()).This function must not throw any exceptions.
     */
    protected abstract void cleanup();

    /**
     * Implement the initialization an {@code IChunkCache<IChunkGet>} here. Use will need the {@code getChunkAt} function
     *
     * @return an initialized {@code IChunkCache<IChunkGet>}
     */
    protected abstract IChunkCache<IChunkGet> initSourceQueueCache();

    protected BiomeProvider getBiomeProvider() {
        if (options.hasBiomeType()) {
            return new SingleBiomeProvider();
        }
        return originalBukkitWorld.getBiomeProvider();
    }

    //classes

    public enum Concurrency {
        FULL,
        RADIUS,
        NONE
    }

    public class SingleBiomeProvider extends BiomeProvider {

        private final org.bukkit.block.Biome biome = BukkitAdapter.adapt(options.getBiomeType());

        @Override
        public org.bukkit.block.Biome getBiome(final WorldInfo worldInfo, final int x, final int y, final int z) {
            return biome;
        }

        @Override
        public List<org.bukkit.block.Biome> getBiomes(final WorldInfo worldInfo) {
            return Collections.singletonList(biome);
        }

    }

}

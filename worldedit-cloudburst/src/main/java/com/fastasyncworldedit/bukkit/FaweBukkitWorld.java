package com.fastasyncworldedit.bukkit;

import com.fastasyncworldedit.bukkit.adapter.NMSAdapter;
import com.fastasyncworldedit.bukkit.util.WorldUnloadedException;
import com.fastasyncworldedit.core.math.IntPair;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import cn.nukkit.Server;
import cn.nukkit.level.Level;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class FaweBukkitWorld extends BukkitWorld {

    private static final Map<Level, FaweBukkitWorld> CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    private final ConcurrentHashMap<IntPair, NMSAdapter.ChunkSendLock> SENDING_CHUNKS = new ConcurrentHashMap<>();

    /**
     * Construct the object.
     *
     * @param world the world
     */
    private FaweBukkitWorld(final Level world) {
        super(world);
    }

    public static FaweBukkitWorld of(Level world) {
        return CACHE.compute(world, (__, val) -> {
            if (val == null) {
                return new FaweBukkitWorld(world);
            }
            val.updateReference();
            return val;
        });
    }

    public static FaweBukkitWorld of(String worldName) {
        Level world = Server.getInstance().getLevelByName(worldName);
        if (world == null) {
            throw new UnsupportedOperationException("Unable to find org.bukkit.World instance for " + worldName + ". Is it loaded?");
        }
        return of(world);
    }

    public static ConcurrentHashMap<IntPair, NMSAdapter.ChunkSendLock> getWorldSendingChunksMap(FaweBukkitWorld world) {
        return world.SENDING_CHUNKS;
    }

    public static ConcurrentHashMap<IntPair, NMSAdapter.ChunkSendLock> getWorldSendingChunksMap(String worldName) {
        return of(worldName).SENDING_CHUNKS;
    }

    private void updateReference() {
        Level world = getWorld();
        Level bukkitWorld = Server.getInstance().getLevelByName(worldNameRef);
        if (bukkitWorld == null) {
            throw new WorldUnloadedException(worldNameRef);
        } else if (bukkitWorld != world) {
            worldRef = new WeakReference<>(bukkitWorld);
        }
    }

}

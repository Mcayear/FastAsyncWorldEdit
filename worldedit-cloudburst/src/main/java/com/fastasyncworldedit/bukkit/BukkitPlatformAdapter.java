package com.fastasyncworldedit.bukkit;

import cn.nukkit.Player;
import cn.nukkit.level.Position;
import com.fastasyncworldedit.core.FAWEPlatformAdapterImpl;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.sk89q.worldedit.bukkit.NukkitGetBlocks;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import java.util.Collection;

public class BukkitPlatformAdapter implements FAWEPlatformAdapterImpl {

    public static final BukkitPlatformAdapter INSTANCE = new BukkitPlatformAdapter();

    private BukkitPlatformAdapter() {
    }

    @Override
    public void sendChunk(final IChunkGet chunk, final int mask, final boolean lighting) {
        if (chunk instanceof NukkitGetBlocks pnxGetBlocks) {
            Collection<Player> players = WorldEditPlugin.getInstance().getServer().getOnlinePlayers().values();
            int view = WorldEditPlugin.getInstance().getServer().getViewDistance();
            for (Player player : players) {
                Position pos = player.getPosition();
                int pcx = pos.getFloorX() >> 4;
                int pcz = pos.getFloorZ() >> 4;
                if (Math.abs(pcx - pnxGetBlocks.getChunkX()) > view || Math.abs(pcz - pnxGetBlocks.getChunkZ()) > view) {
                    continue;
                }
                pnxGetBlocks.getServerLevel().requestChunk(pnxGetBlocks.getChunkX(), pnxGetBlocks.getChunkZ(), player);
            }
        }
    }

}

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
        if (chunk instanceof NukkitGetBlocks nkxGetBlocks) {
            Collection<Player> players = WorldEditPlugin.getInstance().getServer().getOnlinePlayers().values();
            int view = WorldEditPlugin.getInstance().getServer().getViewDistance();
            for (Player player : players) {
                Position pos = player.getPosition();
                int pcx = pos.getFloorX() >> 4;
                int pcz = pos.getFloorZ() >> 4;
                if (Math.abs(pcx - nkxGetBlocks.getChunkX()) > view || Math.abs(pcz - nkxGetBlocks.getChunkZ()) > view) {
                    continue;
                }
                nkxGetBlocks.getServerLevel().requestChunk(nkxGetBlocks.getChunkX(), nkxGetBlocks.getChunkZ(), player);
            }
        }
    }

}

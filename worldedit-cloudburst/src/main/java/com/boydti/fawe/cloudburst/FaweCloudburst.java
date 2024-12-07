package com.boydti.fawe.cloudburst;

import com.fastasyncworldedit.core.FAWEPlatformAdapterImpl;
import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.IFawe;
import com.fastasyncworldedit.core.queue.implementation.preloader.Preloader;
import com.fastasyncworldedit.core.queue.implementation.QueueHandler;
import com.boydti.fawe.cloudburst.adapter.CloudburstQueueHandler;
import com.boydti.fawe.cloudburst.listener.BrushListener;
import com.boydti.fawe.cloudburst.listener.ChunkListener9;
import com.boydti.fawe.cloudburst.util.BukkitTaskMan;
import com.boydti.fawe.cloudburst.util.ItemUtil;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.regions.FaweMaskManager;
import com.fastasyncworldedit.core.util.TaskManager;
import com.fastasyncworldedit.core.util.image.ImageViewer;
import com.sk89q.worldedit.cloudburst.CloudburstAdapter;
import com.sk89q.worldedit.cloudburst.CloudburstPlayer;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.level.LevelLoadEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.level.Level;
import cn.nukkit.Player;
import cn.nukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;

public class FaweCloudburst implements IFawe, Listener {

    private static final Logger log = LoggerFactory.getLogger(FaweCloudburst.class);

    private final Plugin plugin;
    private ItemUtil itemUtil;

//    private CFIPacketListener packetListener;
    private final boolean chunksStretched;

    public FaweCloudburst(Plugin plugin) {
        this.plugin = plugin;
        try {
            Settings.settings().TICK_LIMITER.ENABLED = !plugin.getServer().hasWhitelist();
            Fawe.set(this);
            Fawe.setupInjector();
            try {
                new BrushListener(plugin);
            } catch (Throwable e) {
                log.debug("Brush Listener Failed", e);
            }
        } catch (final Throwable e) {
            e.printStackTrace();
            plugin.getServer().shutdown();
        }

        chunksStretched = false;
//                Integer.parseInt(Nukkit.VERSION.split("-")[0].split("\\.")[1]) >= 16;

        //PlotSquared support is limited to Spigot/Paper as of 02/20/2020

        // Registered delayed Event Listeners
        TaskManager.taskManager().task(() -> {
            // This class
            plugin.getServer().getPluginManager().registerEvents(this, this.plugin);

            // The tick limiter
            new ChunkListener9();
        });
    }

    @Override // Please don't delete this again, it's WIP
    public void registerPacketListener() {
//        PluginManager manager = Bukkit.getPluginManager();
//        if (packetListener == null && manager.getPlugin("ProtocolLib") != null) {
//            packetListener = new CFIPacketListener(plugin);
//        }
    }

    @Override
    public String getPlatform() {
        return "Cloudburst";
    }

    @Override
    public QueueHandler getQueueHandler() {
        return new CloudburstQueueHandler();
    }

    /**
     * Get the preloader instance and initialise if needed
     *
     * @param initialise if the preloader should be initialised if null
     * @return preloader instance
     */
    @Override
    public Preloader getPreloader(final boolean initialise) {
        return null;
    }

    @Override
    public synchronized ImageViewer getImageViewer(com.sk89q.worldedit.entity.Player player) {
//        if (listeningImages && imageListener == null) {
//            return null;
//        }

//        CloudburstImageViewer viewer = new CloudburstImageViewer(CloudburstAdapter.adapt(player));
//        if (imageListener == null) {
//            this.imageListener = new CloudburstImageListener(plugin);
//        }
//        return viewer;
        return null;
    }

    @Override
    public void debug(final String message) {
        log.debug(message);
    }

    @Override
    public File getDirectory() {
        return plugin.getDataFolder();
    }


    public ItemUtil getItemUtil() {
        ItemUtil tmp = itemUtil;
        if (tmp == null) {
            try {
                this.itemUtil = tmp = new ItemUtil();
            } catch (Throwable e) {
                Settings.settings().EXPERIMENTAL.PERSISTENT_BRUSHES = false;
                log.debug("Persistent Brushes Failed", e);
            }
        }
        return tmp;
    }

    @Override
    public String getDebugInfo() {
        StringBuilder msg = new StringBuilder();
        msg.append("Server Version: ").append(plugin.getServer().getNukkitVersion()).append("\n");
        msg.append("Plugins: \n");
        for (Plugin p : plugin.getServer().getPluginManager().getPlugins().values()) {
            msg.append(" - ").append(p.getName()).append(": ")
                    .append(p.getDescription().getVersion()).append("\n");
        }
        return msg.toString();
    }

    /**
     * The task manager handles sync/async tasks.
     */
    @Override
    public TaskManager getTaskManager() {
        return new BukkitTaskMan(plugin);
    }

    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * A mask manager handles region restrictions e.g., PlotSquared plots / WorldGuard regions
     */
    @Override
    public Collection<FaweMaskManager> getMaskManagers() {
        return Collections.emptyList();
    }

    private volatile boolean keepUnloaded;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(LevelLoadEvent event) {
        if (keepUnloaded) {
            Level world = event.getLevel();
//            world.setKeepSpawnInMemory(false);
        }
    }

    public synchronized <T> T createWorldUnloaded(Supplier<T> task) {
        keepUnloaded = true;
        try {
            return task.get();
        } finally {
            keepUnloaded = false;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CloudburstPlayer wePlayer = CloudburstAdapter.adapt(player);
        wePlayer.unregister();
    }

    @SuppressWarnings("deprecation")
    @Override
    public UUID getUUID(String name) {
        return plugin.getServer().getOfflinePlayer(name).getServerId();
    }

    @Override
    public String getName(UUID uuid) {
        return plugin.getServer().getOfflinePlayer(uuid).getName();
    }

    @Override
    public Preloader getPreloader() {
        return null;
    }

    @Override
    public boolean isChunksStretched() {
        return chunksStretched;
    }

    @Override
    public FAWEPlatformAdapterImpl getPlatformAdapter() {
        return null;
    }

}

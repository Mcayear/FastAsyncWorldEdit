package com.fastasyncworldedit.bukkit.util;

import com.fastasyncworldedit.core.util.TaskManager;
import cn.nukkit.Server;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.scheduler.AsyncTask;

import javax.annotation.Nonnull;

public class BukkitTaskManager extends TaskManager {

    private final Plugin plugin;

    public BukkitTaskManager(final Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public int repeat(@Nonnull final Runnable runnable, final int interval) {
        return this.plugin.getServer().getScheduler().scheduleRepeatingTask(this.plugin, runnable, interval, false).getTaskId();
    }

    @Override
    public int repeatAsync(@Nonnull final Runnable runnable, final int interval) {
        return this.plugin.getServer().getScheduler().scheduleRepeatingTask(this.plugin, runnable, interval, true).getTaskId();
    }

    @Override
    public void async(@Nonnull final Runnable runnable) {
        this.plugin.getServer().getScheduler().scheduleAsyncTask(this.plugin, new AsyncTask() {
            @Override
            public void onRun() {
                runnable.run();
            }
        }).getTaskId();
    }

    @Override
    public void task(@Nonnull final Runnable runnable) {
        this.plugin.getServer().getScheduler().scheduleTask(this.plugin, runnable).getTaskId();
    }

    @Override
    public void later(@Nonnull final Runnable runnable, final int delay) {
        this.plugin.getServer().getScheduler().scheduleDelayedTask(this.plugin, runnable, delay).getTaskId();
    }

    @Override
    public void laterAsync(@Nonnull final Runnable runnable, final int delay) {
        this.plugin.getServer().getScheduler().scheduleDelayedTask(this.plugin, runnable, delay, true);
    }

    @Override
    public void cancel(final int task) {
        if (task != -1) {
            Server.getInstance().getScheduler().cancelTask(task);
        }
    }

}

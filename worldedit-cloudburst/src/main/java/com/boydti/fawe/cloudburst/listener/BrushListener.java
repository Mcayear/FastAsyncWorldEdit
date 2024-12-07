package com.boydti.fawe.cloudburst.listener;

import com.fastasyncworldedit.core.command.tool.MovableTool;
import com.fastasyncworldedit.core.command.tool.ResettableTool;
import com.fastasyncworldedit.core.command.tool.scroll.ScrollTool;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.cloudburst.CloudburstAdapter;
import com.sk89q.worldedit.cloudburst.CloudburstPlayer;
import com.sk89q.worldedit.command.tool.Tool;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerItemHeldEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.level.Location;
import cn.nukkit.Player;
import cn.nukkit.plugin.Plugin;

public class BrushListener implements Listener {
    public BrushListener(Plugin plugin) {
        Server.getInstance().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerItemHoldEvent(final PlayerItemHeldEvent event) {
        final Player cloudPlayer = event.getPlayer();
        if (cloudPlayer.isSneaking()) {
            return;
        }
        CloudburstPlayer player = CloudburstAdapter.adapt(cloudPlayer);
        LocalSession session = player.getSession();
        Tool tool = session.getTool(player);
        if (tool instanceof ScrollTool) {
            final int slot = event.getSlot();
            final int oldSlot = event.getPlayer().getInventory().getHeldItemIndex();
            final int ri;
            if ((((slot - oldSlot) <= 4) && ((slot - oldSlot) > 0)) || ((slot - oldSlot) < -4)) {
                ri = 1;
            } else {
                ri = -1;
            }
            ScrollTool scrollable = (ScrollTool) tool;
            if (scrollable.increment(player, ri)) {
                cloudPlayer.getInventory().setHeldItemSlot(oldSlot);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if ((from.getYaw() != to.getYaw() && from.getPitch() != to.getPitch()) || from.getFloorX() != to.getFloorX() || from.getFloorZ() != to.getFloorZ() || from.getFloorY() != to.getFloorY()) {
            Player bukkitPlayer = event.getPlayer();
            com.sk89q.worldedit.entity.Player player = CloudburstAdapter.adapt(bukkitPlayer);
            LocalSession session = player.getSession();
            Tool tool = session.getTool(player);
            if (tool != null) {
                if (tool instanceof MovableTool) {
                    ((MovableTool) tool).move(player);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        Player bukkitPlayer = event.getPlayer();
        if (bukkitPlayer.isSneaking()) {
            if (event.getAction() == PlayerInteractEvent.Action.PHYSICAL) {
                return;
            }
            com.sk89q.worldedit.entity.Player player = CloudburstAdapter.adapt(bukkitPlayer);
            LocalSession session = player.getSession();
            Tool tool = session.getTool(player);
            if (tool instanceof ResettableTool) {
                if (((ResettableTool) tool).reset()) {
                    event.setCancelled(true);
                }
            }
        }
    }
}

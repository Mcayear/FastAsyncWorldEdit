package com.fastasyncworldedit.bukkit.util.image;

import com.fastasyncworldedit.core.util.TaskManager;
import com.fastasyncworldedit.core.util.image.Drawable;
import com.fastasyncworldedit.core.util.image.ImageUtil;
import com.fastasyncworldedit.core.util.image.ImageViewer;
import cn.nukkit.item.ItemEmptyMap;
import cn.nukkit.item.ItemMap;
import cn.nukkit.level.Location;
import cn.nukkit.level.generator.math.Rotation;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.entity.Entity;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.inventory.PlayerInventory;
//import org.inventivetalent.mapmanager.MapManagerPlugin;
//import org.inventivetalent.mapmanager.controller.MapController;
//import org.inventivetalent.mapmanager.controller.MultiMapController;
//import org.inventivetalent.mapmanager.manager.MapManager;
//import org.inventivetalent.mapmanager.wrapper.MapWrapper;

import javax.annotation.Nullable;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;

public class BukkitImageViewer implements ImageViewer {

//    private final MapManager mapManager;
    private final Player player;
    private BufferedImage last;
    private BlockEntityItemFrame[][] frames;
    private boolean reverse;

    public BukkitImageViewer(Player player) {
//        mapManager = ((MapManagerPlugin) Bukkit.getPluginManager().getPlugin("MapManager")).getMapManager();
        this.player = player;
    }

    public void selectFrame(BlockEntityItemFrame start) {
        Location pos1 = start.getLocation().clone();
        Location pos2 = start.getLocation().clone();

        BlockFace facing = start.createSpawnPacket().getBlockFace();
        int planeX = facing.getXOffset() == 0 ? 1 : 0;
        int planeY = facing.getYOffset() == 0 ? 1 : 0;
        int planeZ = facing.getZOffset() == 0 ? 1 : 0;

        BlockEntityItemFrame[][] res = find(pos1, pos2, facing);
        Location tmp;
        while (true) {
            if (res != null) {
                frames = res;
            }
            tmp = pos1.clone().subtract(planeX, planeY, planeZ);
            if ((res = find(tmp, pos2, facing)) != null) {
                pos1 = tmp;
                continue;
            }
            tmp = pos2.clone().add(planeX, planeY, planeZ);
            if ((res = find(pos1, tmp, facing)) != null) {
                pos2 = tmp;
                continue;
            }
            tmp = pos1.clone().subtract(planeX, 0, planeZ);
            if ((res = find(tmp, pos2, facing)) != null) {
                pos1 = tmp;
                continue;
            }
            tmp = pos2.clone().add(planeX, 0, planeZ);
            if ((res = find(pos1, tmp, facing)) != null) {
                pos2 = tmp;
                continue;
            }
            tmp = pos1.clone().subtract(0, 1, 0);
            if ((res = find(tmp, pos2, facing)) != null) {
                pos1 = tmp;
                continue;
            }
            tmp = pos2.clone().add(0, 1, 0);
            if ((res = find(pos1, tmp, facing)) != null) {
                pos2 = tmp;
                continue;
            }
            break;
        }
    }

    public BlockEntityItemFrame[][] getItemFrames() {
        return frames;
    }

    private BlockEntityItemFrame[][] find(Location pos1, Location pos2, BlockFace facing) {
        try {
            Location distance = pos2.clone().subtract(pos1).add(1, 1, 1);
            int width = Math.max(distance.getFloorX(), distance.getFloorZ());
            BlockEntityItemFrame[][] frames = new BlockEntityItemFrame[width][distance.getFloorY()];

            Level world = pos1.getLevel();

            this.reverse = facing == BlockFace.NORTH || facing == BlockFace.EAST;
            int v = 0;
            for (double y = pos1.getY(); y <= pos2.getY(); y++, v++) {
                int h = 0;
                for (double z = pos1.getZ(); z <= pos2.getZ(); z++) {
                    for (double x = pos1.getX(); x <= pos2.getX(); x++, h++) {
//                        Location pos = new Location(x, y, z, world);
//                        Entity[] entities = world.getNearbyEntities(player.boundingBox.grow(0.1, 0.1, 0.1), player);
                        boolean contains = false;
//                        for (Entity ent : entities) {
//                            if (ent instanceof BlockEntityItemFrame && ent.getFacing() == facing) {
//                                BlockEntityItemFrame itemFrame = (BlockEntityItemFrame) ent;
//                                itemFrame.setRotation(Rotation.NONE);
//                                contains = true;
//                                frames[reverse ? width - 1 - h : h][v] = (BlockEntityItemFrame) ent;
//                                break;
//                            }
//                        }
                        if (!contains) {
                            return null;
                        }
                    }
                }
            }
            return frames;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void view(Drawable drawable) {
        view(null, drawable);
    }

    private void view(@Nullable BufferedImage image, @Nullable Drawable drawable) {
        if (image == null && drawable == null) {
            throw new IllegalArgumentException("An image or drawable must be provided. Both cannot be null");
        }
        boolean initializing = last == null;

        if (this.frames != null) {
            if (image == null) {
                image = drawable.draw();
            }
            last = image;
            int width = frames.length;
            int height = frames[0].length;
            BufferedImage scaled = ImageUtil.getScaledInstance(
                    image,
                    128 * width,
                    128 * height,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR,
                    false
            );
//            MapWrapper mapWrapper = mapManager.wrapMultiImage(scaled, width, height);
//            MultiMapController controller = (MultiMapController) mapWrapper.getController();
//            controller.addViewer(player);
//            controller.sendContent(player);
//            controller.showInFrames(player, frames, true);
        } else {
            int slot = getMapSlot(player);
            TaskManager.taskManager().sync(() -> {
                if (slot == -1) {
                    if (initializing) {
                        player.getInventory().setItemInHand(Item.fromString("minecraft:map"));
                    } else {
                        return null;
                    }
                } else if (player.getInventory().getHeldItemSlot() != slot) {
                    player.getInventory().setHeldItemSlot(slot);
                }
                return null;
            });
            if (image == null && drawable != null) {
                image = drawable.draw();
            }
            last = image;
            BufferedImage scaled = ImageUtil.getScaledInstance(
                    image,
                    128,
                    128,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR,
                    false
            );
//            MapWrapper mapWrapper = mapManager.wrapImage(scaled);
//            MapController controller = mapWrapper.getController();
//            controller.addViewer(player);
//            controller.sendContent(player);
//            controller.showInHand(player, true);
        }
    }

    private int getMapSlot(Player player) {
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < 9; i++) {
            Item item = inventory.getItem(i);
            if (!item.isNull() && (item instanceof ItemEmptyMap || item instanceof ItemMap)) {
                return i;
            }
        }
        return -1;
    }

    public void refresh() {
        if (last != null) {
            view(last, null);
        }
    }

    @Override
    public void close() throws IOException {
        last = null;
    }

}

/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.bukkit;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.entity.metadata.EntityProperties;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.NullWorld;
//import com.sk89q.worldedit.world.entity.EntityType;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An adapter to adapt a Bukkit entity into a WorldEdit one.
 */
//FAWE start - made class public
public class BukkitEntity implements Entity {
//FAWE end

    private final WeakReference<cn.nukkit.entity.Entity> entityRef;
    private final WeakReference<cn.nukkit.blockentity.BlockEntity> blockEntityRef;
    //FAWE start
//    private final EntityType type;
    //FAWE end

    /**
     * Create a new instance.
     *
     * @param entity the entity
     */
    public BukkitEntity(cn.nukkit.entity.Entity entity) {
        checkNotNull(entity);
        //FAWE start
//        this.type = EntityType.REGISTRY.get(entity.getIdentifier().toString());
        //FAWE end
        this.entityRef = new WeakReference<>(entity);
        this.blockEntityRef = new WeakReference<>(null);
    }

    /**
     * Create a new instance.
     *
     * @param entity the entity
     */
    public BukkitEntity(cn.nukkit.blockentity.BlockEntity entity) {
        checkNotNull(entity);
        //FAWE start
//        this.type = EntityType.REGISTRY.get(entity.getIdentifier().toString());
        //FAWE end
        this.entityRef = new WeakReference<>(null);
        this.blockEntityRef = new WeakReference<>(entity);
    }

    @Override
    public Extent getExtent() {
        cn.nukkit.entity.Entity entity = entityRef.get();
        cn.nukkit.blockentity.BlockEntity blockEntity = blockEntityRef.get();
        if (entity != null) {
            return BukkitAdapter.adapt(entity.getLevel());
        } else if (blockEntity != null) {
            return BukkitAdapter.adapt(blockEntity.getLevel());
        } else {
            return NullWorld.getInstance();
        }
    }

    @Override
    public Location getLocation() {
        cn.nukkit.entity.Entity entity = entityRef.get();
        cn.nukkit.blockentity.BlockEntity blockEntity = blockEntityRef.get();
        if (entity != null) {
            return BukkitAdapter.adapt(entity.getLocation());
        } else if (blockEntity != null) {
            return BukkitAdapter.adapt(blockEntity.getLocation());
        } else {
            return new Location(NullWorld.getInstance());
        }
    }

    @Override
    public boolean setLocation(Location location) {
        cn.nukkit.entity.Entity entity = entityRef.get();
        cn.nukkit.blockentity.BlockEntity blockEntity = blockEntityRef.get();
        if (entity != null) {
            return entity.teleport(BukkitAdapter.adapt(location));
        } else if (blockEntity != null) {
            return blockEntity.getLevelBlock().cloneTo(BukkitAdapter.adapt(location));
        } else {
            return false;
        }
    }

    @Override
    public BaseEntity getState() {
        cn.nukkit.entity.Entity entity = entityRef.get();
        cn.nukkit.blockentity.BlockEntity blockEntity = blockEntityRef.get();
        if (entity != null) {
            if (entity instanceof Player) {
                return null;
            }

            BukkitImplAdapter<?> adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
            if (adapter != null) {
                return adapter.getEntity(entity);
            } else {
                return null;
            }
        } else if (blockEntity != null) {
            return null;
        } else {
            return null;
        }
    }

    @Override
    public boolean remove() {
        cn.nukkit.entity.Entity entity = entityRef.get();
        cn.nukkit.blockentity.BlockEntity blockEntity = blockEntityRef.get();
        if (entity != null) {
            try {
                entity.close();
            } catch (UnsupportedOperationException e) {
                return false;
            }
            return entity.isClosed();
        } else if (blockEntity != null) {
            var level = blockEntity.getLevel();
            var pos = blockEntity.getLocation();
            try {
                level.setBlock(pos, 0, Block.get(Block.AIR), false, false);
                blockEntity.close();
            } catch (UnsupportedOperationException e) {
                return false;
            }
            return blockEntity.closed && level.getBlock(pos).getId() == BlockID.AIR;
        } else {
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        cn.nukkit.entity.Entity entity = entityRef.get();
        cn.nukkit.blockentity.BlockEntity blockEntity = blockEntityRef.get();
        if (entity != null && EntityProperties.class.isAssignableFrom(cls)) {
            return (T) new BukkitEntityProperties(entity);
        } else if (blockEntity != null && EntityProperties.class.isAssignableFrom(cls)) {
            return (T) new BukkitEntityProperties(blockEntity);
        } else {
            return null;
        }
    }

}

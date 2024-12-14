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

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityBanner;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.entity.EntityOwnable;
import cn.nukkit.entity.item.EntityArmorStand;
import cn.nukkit.entity.item.EntityBoat;
import cn.nukkit.entity.item.EntityFallingBlock;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.entity.item.EntityMinecartAbstract;
import cn.nukkit.entity.item.EntityPrimedTNT;
import cn.nukkit.entity.item.EntityXPOrb;
import cn.nukkit.entity.mob.EntityEnderDragon;
import cn.nukkit.entity.mob.EntitySnowGolem;
import cn.nukkit.entity.mob.EntityWither;
import cn.nukkit.entity.passive.EntityAnimal;
import cn.nukkit.entity.passive.EntityIronGolem;
import cn.nukkit.entity.passive.EntityNPCEntity;
import cn.nukkit.entity.passive.EntityVillager;
import cn.nukkit.entity.passive.EntityWaterAnimal;
import cn.nukkit.entity.projectile.EntityProjectile;
import com.sk89q.worldedit.entity.metadata.EntityProperties;

import static com.google.common.base.Preconditions.checkNotNull;

class BukkitEntityProperties implements EntityProperties {

    protected final Entity entity;
    protected final BlockEntity blockEntity;

    BukkitEntityProperties(Entity entity) {
        checkNotNull(entity);
        this.entity = entity;
        this.blockEntity = null;
    }

    BukkitEntityProperties(BlockEntity entity) {
        checkNotNull(entity);
        this.entity = null;
        this.blockEntity = entity;
    }
    @Override
    public boolean isPlayerDerived() {
        if (entity == null) return false;
        return entity instanceof EntityHuman;
    }

    @Override
    public boolean isProjectile() {
        if (entity == null) return false;
        return entity instanceof EntityProjectile;
    }

    @Override
    public boolean isItem() {
        if (entity == null) return false;
        return entity instanceof EntityItem;
    }

    @Override
    public boolean isFallingBlock() {
        if (entity == null) return false;
        return entity instanceof EntityFallingBlock;
    }

    @Override
    public boolean isPainting() {
        if (blockEntity == null) return false;
        return blockEntity instanceof BlockEntityBanner;
    }

    @Override
    public boolean isItemFrame() {
        if (blockEntity == null) return false;
        return blockEntity instanceof BlockEntityItemFrame;
    }

    @Override
    public boolean isBoat() {
        if (entity == null) return false;
        return entity instanceof EntityBoat;
    }

    @Override
    public boolean isMinecart() {
        if (entity == null) return false;
        return entity instanceof EntityMinecartAbstract;
    }

    @Override
    public boolean isTNT() {
        if (entity == null) return false;
        return entity instanceof EntityPrimedTNT;
    }

    @Override
    public boolean isExperienceOrb() {
        if (entity == null) return false;
        return entity instanceof EntityXPOrb;
    }

    @Override
    public boolean isLiving() {
        if (entity == null) return false;
        return entity instanceof EntityLiving;
    }

    @Override
    public boolean isAnimal() {
        if (entity == null) return false;
        return entity instanceof EntityAnimal;
    }

    @Override
    public boolean isAmbient() {
        if (entity == null) return false;
        return entity instanceof EntityAnimal;
//        return entity instanceof Ambient;
    }

    @Override
    public boolean isNPC() {
        if (entity == null) return false;
        if (entity instanceof EntityNPCEntity) return true;
        return entity instanceof EntityVillager;
    }

    @Override
    public boolean isGolem() {
        if (entity == null) return false;
        return entity instanceof EntityIronGolem || entity instanceof EntitySnowGolem;
    }

    @Override
    public boolean isTamed() {
        if (entity == null) return false;
        return entity instanceof EntityOwnable;
    }

    @Override
    public boolean isTagged() {
        if (entity == null) return false;
        return entity instanceof EntityLiving && entity.hasCustomName();
    }

    @Override
    public boolean isArmorStand() {
        if (entity == null) return false;
        return entity instanceof EntityArmorStand;
    }

    @Override
    public boolean isPasteable() {
        if (entity == null) return false;
        return !(entity instanceof Player || entity instanceof EntityEnderDragon || entity instanceof EntityWither);
    }

    @Override
    public boolean isWaterCreature() {
        if (entity == null) return false;
        return entity instanceof EntityWaterAnimal;
    }

}

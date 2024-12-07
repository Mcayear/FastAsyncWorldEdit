/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.cloudburst;

import com.sk89q.worldedit.entity.metadata.EntityProperties;
import cn.nukkit.blockentity.ItemFrame;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.Projectile;
import cn.nukkit.entity.impl.EntityLiving;
import cn.nukkit.entity.impl.Human;
import cn.nukkit.entity.impl.passive.Animal;
import cn.nukkit.entity.impl.passive.EntityTameable;
import cn.nukkit.entity.impl.vehicle.EntityMinecart;
import cn.nukkit.entity.misc.*;
import cn.nukkit.entity.passive.Bat;
import cn.nukkit.entity.passive.IronGolem;
import cn.nukkit.entity.passive.Villager;
import cn.nukkit.entity.vehicle.Boat;
import cn.nukkit.entity.vehicle.Minecart;
import cn.nukkit.Player;
import cn.nukkit.utils.data.MinecartType;

import static com.google.common.base.Preconditions.checkNotNull;

class CloudburstEntityProperties implements EntityProperties {

    private final Entity entity;

    CloudburstEntityProperties(Entity entity) {
        checkNotNull(entity);
        this.entity = entity;
    }

    @Override
    public boolean isPlayerDerived() {
        return entity instanceof Human;
    }

    @Override
    public boolean isProjectile() {
        return entity instanceof Projectile;
    }

    @Override
    public boolean isItem() {
        return entity instanceof DroppedItem;
    }

    @Override
    public boolean isFallingBlock() {
        return entity instanceof FallingBlock;
    }

    @Override
    public boolean isPainting() {
        return entity instanceof Painting;
    }

    @Override
    public boolean isItemFrame() {
        return entity instanceof ItemFrame;
    }

    @Override
    public boolean isBoat() {
        return entity instanceof Boat;
    }

    @Override
    public boolean isMinecart() {
        return entity instanceof Minecart;
    }

    @Override
    public boolean isTNT() {
        return entity instanceof PrimedTnt || (entity instanceof EntityMinecart && ((EntityMinecart) entity).getMinecartType() == MinecartType.MINECART_TNT);
    }

    @Override
    public boolean isExperienceOrb() {
        return entity instanceof ExperienceOrb;
    }

    @Override
    public boolean isLiving() {
        return entity instanceof EntityLiving;
    }

    @Override
    public boolean isAnimal() {
        return entity instanceof Animal;
    }

    @Override
    public boolean isAmbient() {
        return entity instanceof Bat;
    }

    @Override
    public boolean isNPC() {
        return entity instanceof Villager;
    }

    @Override
    public boolean isGolem() {
        return entity instanceof IronGolem;
    }

    @Override
    public boolean isTamed() {
        return entity instanceof EntityTameable && ((EntityTameable) entity).isTamed();
    }

    @Override
    public boolean isTagged() {
        return entity instanceof EntityLiving && entity.getNameTag() != null;
    }

    @Override
    public boolean isArmorStand() {
        return entity instanceof ArmorStand;
    }

    @Override
    public boolean isPasteable() {
        return !(entity instanceof Player/* || entity instanceof ComplexEntityPart*/);
    }
}

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

package com.sk89q.worldedit.world.registry;

import com.sk89q.worldedit.entity.BaseEntity;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

/**
 * Provides information on entities.
 */
public interface EntityRegistry {

    /**
     * Create a new entity using its ID.
     *
     * @param id the id
     * @return the entity, which may be null if the entity does not exist
     */
    @Nullable
    BaseEntity createFromId(String id);

}

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

package com.sk89q.worldedit.function.block;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.LayerFunction;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.BooleanProperty;
import com.sk89q.worldedit.registry.state.EnumProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.Locale;

public class SnowSimulator implements LayerFunction {

    //FAWE start
    public static final BooleanProperty snowy = (BooleanProperty) (Property<?>) BlockTypes.GRASS_BLOCK.getProperty("snowy");
    private static final EnumProperty slab = (EnumProperty) (Property<?>) BlockTypes.SANDSTONE_SLAB.getProperty("type");
    private static final EnumProperty stair = (EnumProperty) (Property<?>) BlockTypes.SANDSTONE_STAIRS.getProperty("half");
    private static final EnumProperty trapdoor = (EnumProperty) (Property<?>) BlockTypes.ACACIA_TRAPDOOR.getProperty("half");
    private static final BooleanProperty trapdoorOpen = (BooleanProperty) (Property<?>) BlockTypes.ACACIA_TRAPDOOR.getProperty(
            "open");
    //FAWE end

    private final BlockState ice = BlockTypes.ICE.getDefaultState();
    private final BlockState snow = BlockTypes.SNOW.getDefaultState();
    private final BlockState snowBlock = BlockTypes.SNOW_BLOCK.getDefaultState();

    private final Property<Integer> snowLayersProperty = BlockTypes.SNOW.getProperty("layers");
    private final Property<Integer> waterLevelProperty = BlockTypes.WATER.getProperty("level");

    private final Extent extent;
    private final boolean stack;

    private int affected;

    public SnowSimulator(Extent extent, boolean stack) {

        this.extent = extent;
        this.stack = stack;

        this.affected = 0;
    }

    public int getAffected() {
        return this.affected;
    }

    @Override
    public boolean isGround(BlockVector3 position) {
        BlockState block = this.extent.getBlock(position);

        // We're returning the first block we can place *on top of*
        if (block.getBlockType().getMaterial().isAir() || (stack && block.getBlockType() == BlockTypes.SNOW)) {
            return false;
        }

        // Unless it's water
        if (block.getBlockType() == BlockTypes.WATER) {
            return true;
        }

        // Stop searching when we hit a movement blocker
        return block.getBlockType().getMaterial().isMovementBlocker();
    }

    @Override
    public boolean apply(BlockVector3 position, int depth) throws WorldEditException {
        if (depth > 0) {
            // We only care about the first layer.
            return false;
        }

        BlockState block = this.extent.getBlock(position);

        if (block.getBlockType() == BlockTypes.WATER) {
            if (block.getState(waterLevelProperty) == 0) {
                if (this.extent.setBlock(position, ice)) {
                    affected++;
                }
            }
            return false;
        }


        // Can't put snow this far up
        if (position.y() == this.extent.getMaximumPoint().y()) {
            return false;
        }

        BlockVector3 abovePosition = position.add(0, 1, 0);
        BlockState above = this.extent.getBlock(abovePosition);

        // Can only replace air (or snow in stack mode)
        if (!above.getBlockType().getMaterial().isAir() && (!stack || above.getBlockType() != BlockTypes.SNOW)) {
            return false;
            //FAWE start
        } else if (!block.getBlockType().id().toLowerCase(Locale.ROOT).contains("ice") && this.extent.getEmittedLight(
                abovePosition) > 10) {
            return false;
        } else if (!block.getBlockType().getMaterial().isFullCube()) {
            BlockType type = block.getBlockType();
            if (type.hasProperty(slab) && block.getState(slab).equalsIgnoreCase("bottom")) {
                return false;
            } else if ((type.hasProperty(trapdoorOpen) && block.getState(trapdoorOpen)) ||
                    (type.hasProperty(trapdoor) && block.getState(trapdoor).equalsIgnoreCase("bottom"))) {
                return false;
            } else if (type.hasProperty(stair) && block.getState(stair).equalsIgnoreCase("bottom")) {
                return false;
            } else {
                return false;
            }
            //FAWE end
        } else if (!BlockCategories.SNOW_LAYER_CAN_SURVIVE_ON.contains(block.getBlockType())) {
            return false;
        }

        if (stack && above.getBlockType() == BlockTypes.SNOW) {
            int currentHeight = above.getState(snowLayersProperty);
            // We've hit the highest layer (If it doesn't contain current + 2 it means it's 1 away from full)
            if (!snowLayersProperty.getValues().contains(currentHeight + 2)) {
                if (this.extent.setBlock(abovePosition, snowBlock)) {
                    if (block.getBlockType().hasProperty(snowy)) {
                        this.extent.setBlock(position, block.with(snowy, true));
                    }
                    this.affected++;
                }
            } else {
                if (this.extent.setBlock(abovePosition, above.with(snowLayersProperty, currentHeight + 1))) {
                    if (block.getBlockType().hasProperty(snowy)) {
                        this.extent.setBlock(position, block.with(snowy, true));
                    }
                    this.affected++;
                }
            }
            return false;
        }
        if (this.extent.setBlock(abovePosition, snow)) {
            if (block.getBlockType().hasProperty(snowy)) {
                this.extent.setBlock(position, block.with(snowy, true));
            }
            this.affected++;
        }
        return false;
    }

}

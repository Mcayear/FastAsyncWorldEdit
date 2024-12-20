package com.fastasyncworldedit.bukkit.adapter;

import com.fastasyncworldedit.bukkit.util.BukkitItemStack;
import com.fastasyncworldedit.core.util.TaskManager;
import com.sk89q.bukkit.util.mappings.MappingRegistries;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.NotABlockException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitEntity;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.entity.EntityTypes;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
//import org.bukkit.Material;
//import org.bukkit.NamespacedKey;
//import org.bukkit.Registry;
//import org.bukkit.TreeType;
import cn.nukkit.level.biome.Biome;
import cn.nukkit.Player;
import cn.nukkit.item.Item;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkNotNull;

public interface IBukkitAdapter {

    /**
     * Convert any WorldEdit world into an equivalent wrapped Bukkit world.
     *
     * <p>If a matching world cannot be found, a {@link RuntimeException}
     * will be thrown.</p>
     *
     * @param world the world
     * @return a wrapped Bukkit world
     */
    default BukkitWorld asBukkitWorld(World world) {
        if (world instanceof BukkitWorld) {
            return (BukkitWorld) world;
        } else {
            BukkitWorld bukkitWorld = WorldEditPlugin.getInstance().getInternalPlatform().matchWorld(world);
            if (bukkitWorld == null) {
                throw new RuntimeException("World '" + world.getName() + "' has no matching version in Bukkit");
            }
            return bukkitWorld;
        }
    }

    /**
     * Create a Bukkit world from a WorldEdit world.
     *
     * @param world the WorldEdit world
     * @return a Bukkit world
     */
    default cn.nukkit.level.Level adapt(World world) {
        checkNotNull(world);
        if (world instanceof BukkitWorld) {
            return ((BukkitWorld) world).getWorld();
        } else {
            cn.nukkit.level.Level match = Server.getInstance().getLevelByName(world.getName());
            if (match != null) {
                return match;
            } else {
                throw new IllegalArgumentException("Can't find a Bukkit world for " + world);
            }
        }
    }

    /**
     * Create a Bukkit location from a WorldEdit position with a Bukkit world.
     *
     * @param world    the Bukkit world
     * @param position the WorldEdit position
     * @return a Bukkit location
     */
    default cn.nukkit.level.Location adapt(cn.nukkit.level.Level world, Vector3 position) {
        checkNotNull(world);
        checkNotNull(position);
        return new cn.nukkit.level.Location(
                position.x(), position.y(), position.z(),
                world
        );
    }

    default cn.nukkit.level.Location adapt(cn.nukkit.level.Level world, BlockVector3 position) {
        return adapt(world, position.toVector3());
    }

    /**
     * Create a Bukkit location from a WorldEdit location with a Bukkit world.
     *
     * @param world    the Bukkit world
     * @param location the WorldEdit location
     * @return a Bukkit location
     */
    default cn.nukkit.level.Location adapt(cn.nukkit.level.Level world, Location location) {
        checkNotNull(world);
        checkNotNull(location);
        return new cn.nukkit.level.Location(
                location.x(), location.y(), location.z(),
                location.getYaw(),
                location.getPitch(),
                world
        );
    }

    /**
     * Create a WorldEdit Vector from a Bukkit location.
     *
     * @param location The Bukkit location
     * @return a WorldEdit vector
     */
    default Vector3 asVector(cn.nukkit.level.Location location) {
        checkNotNull(location);
        return Vector3.at(location.getX(), location.getY(), location.getZ());
    }

    /**
     * Create a WorldEdit BlockVector from a Bukkit location.
     *
     * @param location The Bukkit location
     * @return a WorldEdit vector
     */
    default BlockVector3 asBlockVector(cn.nukkit.level.Location location) {
        checkNotNull(location);
        return BlockVector3.at(location.getX(), location.getY(), location.getZ());
    }

    /**
     * Create a WorldEdit entity from a Bukkit entity.
     *
     * @param entity the Bukkit entity
     * @return a WorldEdit entity
     */
    default Entity adapt(cn.nukkit.entity.Entity entity) {
        checkNotNull(entity);
        return new BukkitEntity(entity);
    }

    /**
     * Create a WorldEdit entity from a Bukkit entity.
     *
     * @param entity the Bukkit entity
     * @return a WorldEdit entity
     */
    default Entity adapt(cn.nukkit.blockentity.BlockEntity entity) {
        checkNotNull(entity);
        return new BukkitEntity(entity);
    }

    /**
     * Create a Bukkit Material form a WorldEdit ItemType
     *
     * @param itemType The WorldEdit ItemType
     * @return The Bukkit Material
     */
    default Item adapt(ItemType itemType) {
        checkNotNull(itemType);
        if (!itemType.id().startsWith("minecraft:")) {
            throw new IllegalArgumentException("Bukkit only supports Minecraft items");
        }
        return MappingRegistries.ITEM.getMapping().inverse().get(itemType).getItem();
    }

    /**
     * Create a Bukkit Material form a WorldEdit BlockType
     *
     * @param blockType The WorldEdit BlockType
     * @return The Bukkit Material
     */
    default cn.nukkit.level.generator.block.state.BlockState adapt(BlockType blockType) {
        checkNotNull(blockType);
        if (!blockType.id().startsWith("minecraft:")) {
            throw new IllegalArgumentException("Bukkit only supports Minecraft blocks");
        }
        String id = blockType.id().substring(10).toUpperCase(Locale.ROOT);
        return Material.getMaterial(id);
    }

    /**
     * Converts a Material to a BlockType
     *
     * @param material The material
     * @return The blocktype
     */
    default BlockType asBlockType(Material material) {
        checkNotNull(material);
        if (!material.isBlock()) {
            throw new IllegalArgumentException(material.getKey() + " is not a block!") {
                @Override
                public synchronized Throwable fillInStackTrace() {
                    return this;
                }
            };
        }
        return BlockTypes.get(material.getKey().toString());
    }


    /**
     * Converts a Material to a ItemType
     *
     * @param namespaceId The item namespaceId example:"minecraft:xxx"
     * @return The itemtype
     */
    default ItemType asItemType(String namespaceId) {
        return ItemTypes.get(namespaceId);
    }

    /**
     * Create a WorldEdit BlockStateHolder from a Bukkit BlockData
     *
     * @param blockData The Bukkit BlockData
     * @return The WorldEdit BlockState
     */
    default BlockState adapt(Block blockData) {
        String id = blockData.get();
        return BlockState.get(id);
    }

    /**
     * Create a Bukkit BlockData from a WorldEdit BlockStateHolder
     *
     * @param block The WorldEdit BlockStateHolder
     * @return The Bukkit BlockData
     */
    default <B extends BlockStateHolder<B>> BlockData adapt(B block) {
        return Bukkit.createBlockData(block.getAsString());
    }

    /**
     * Create a WorldEdit BaseItemStack from a Bukkit ItemStack
     *
     * @param itemStack The Bukkit ItemStack
     * @return The WorldEdit BaseItemStack
     */
    default BaseItemStack adapt(Item itemStack) {
        checkNotNull(itemStack);
        return new BukkitItemStack(itemStack);
    }

    /**
     * Create a Bukkit ItemStack from a WorldEdit BaseItemStack
     *
     * @param item The WorldEdit BaseItemStack
     * @return The Bukkit ItemStack
     */
    default Item adapt(BaseItemStack item) {
        checkNotNull(item);
        if (item instanceof BukkitItemStack) {
            return ((BukkitItemStack) item).getBukkitItemStack();
        }
        Item result = Item.fromString(item.getType().id());
        result.setCount(item.getAmount());
        return result;
    }

    /**
     * Create a WorldEdit Player from a Bukkit Player.
     *
     * @param player The Bukkit player
     * @return The WorldEdit player
     */
    default BukkitPlayer adapt(Player player) {
        return WorldEditPlugin.getInstance().wrapPlayer(player);
    }

    /**
     * Create a Bukkit Player from a WorldEdit Player.
     *
     * @param player The WorldEdit player
     * @return The Bukkit player
     */
    default Player adapt(com.sk89q.worldedit.entity.Player player) {
        return ((BukkitPlayer) player).getPlayer();
    }

    default Biome adapt(BiomeType biomeType) {
        if (!biomeType.id().startsWith("minecraft:")) {
            throw new IllegalArgumentException("Bukkit only supports vanilla biomes");
        }
        try {
            return Biome.getBiome(biomeType.id().substring(10).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    default BiomeType adapt(Biome biome) {
        return BiomeTypes.get(biome.getName().toLowerCase(Locale.ROOT));
    }

    /**
     * Checks equality between a WorldEdit BlockType and a Bukkit Material
     *
     * @param blockType The WorldEdit BlockType
     * @param type      The Bukkit Material
     * @return If they are equal
     */
    default boolean equals(BlockType blockType, Item type) {
        return blockType == asItemType(type.getNamespaceId()).getBlockType();
    }

    /**
     * Create a WorldEdit world from a Bukkit world.
     *
     * @param world the Bukkit world
     * @return a WorldEdit world
     */
    default World adapt(cn.nukkit.level.Level world) {
        checkNotNull(world);
        return new BukkitWorld(world);
    }

    default String adaptEntity(EntityType entityType) {
        return entityType.id();
    }

    /**
     * Create a WorldEdit EntityType from a Bukkit one.
     *
     * @param entityType Bukkit EntityType
     * @return WorldEdit EntityType
     */
    default EntityType adaptEntity(String entityType) {
        return EntityTypes.get(entityType);
    }

    /**
     * Create a WorldEdit BlockStateHolder from a Bukkit ItemStack
     *
     * @param itemStack The Bukkit ItemStack
     * @return The WorldEdit BlockState
     */
    default BlockState asBlockState(Item itemStack) {
        checkNotNull(itemStack);
//        if (itemStack.getBlock().isAir()) {
//            throw new NotABlockException();
//        }
        return adapt(itemStack.getBlock());
    }

    /**
     * Generate a given tree type to the given editsession.
     *
     * @param type        Type of tree to generate
     * @param editSession Editsession to set blocks to
     * @param pt          Point to generate tree at
     * @param world       World to "generate" tree from (seed-wise)
     * @return If successsful
     */
    default boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, BlockVector3 pt, cn.nukkit.level.Level world) {
//        TreeType bukkitType = BukkitWorld.toBukkitTreeType(type);
//        if (bukkitType == TreeType.CHORUS_PLANT) {
//            pt = pt.add(0, 1, 0); // bukkit skips the feature gen which does this offset normally, so we have to add it back
//        }
//        return type != null && world.generateTree(
//                BukkitAdapter.adapt(world, pt), bukkitType,
//                new EditSessionBlockChangeDelegate(editSession)
//        );
        return false;
    }

    /**
     * Retrieve the list of Bukkit entities ({@link cn.nukkit.entity.Entity}) in the given world. If overridden by adapters
     * will attempt retrieval asynchronously.
     *
     * @param world world to retrieve entities in
     * @return list of {@link cn.nukkit.entity.Entity}
     */
    default List<cn.nukkit.entity.Entity> getEntities(cn.nukkit.level.Level world) {
        return Arrays.stream(world.getEntities()).toList();
    }

}

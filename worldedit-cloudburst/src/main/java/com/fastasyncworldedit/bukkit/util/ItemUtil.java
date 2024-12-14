package com.fastasyncworldedit.bukkit.util;

import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.google.common.collect.Maps;
import com.sk89q.jnbt.*;
import cn.nukkit.nbt.tag.Tag;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import cn.nukkit.item.Item;
import org.enginehub.linbus.tree.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

public class ItemUtil {

    private final BukkitImplAdapter adapter;

    public ItemUtil() throws Exception {
        this.adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
        checkNotNull(adapter);
        // Reflection classes are not used in this context, can be removed or implemented as needed
    }

    public CompoundTag getNBT(Item item) {
        cn.nukkit.nbt.tag.CompoundTag nukkitCompoundTag = item.getNamedTag();
        return ItemUtil.toJNBT(nukkitCompoundTag);
    }

    public Item setNBT(Item item, CompoundTag tag) {
        cn.nukkit.nbt.tag.CompoundTag nukkitTag = ItemUtil.toNukkit(tag);
        item.setNamedTag(nukkitTag);
        return item;
    }

    public static FaweCompoundTag toFaweNBT(cn.nukkit.nbt.tag.CompoundTag nukkitTag) {
        Map<String, com.sk89q.jnbt.Tag<?, ?>> value = toJNBT(nukkitTag).getValue();
        LinCompoundTag linCompoundTag = LinCompoundTag.of(Maps.transformValues(value, com.sk89q.jnbt.Tag::toLinTag));
        return FaweCompoundTag.of(linCompoundTag);
    }

    public static FaweCompoundTag toFaweNBT(com.sk89q.jnbt.CompoundTag tag) {
        Map<String, com.sk89q.jnbt.Tag<?, ?>> value = tag.getValue();
        LinCompoundTag linCompoundTag = LinCompoundTag.of(Maps.transformValues(value, com.sk89q.jnbt.Tag::toLinTag));
        return FaweCompoundTag.of(linCompoundTag);
    }

    public static com.sk89q.jnbt.CompoundTag toJNBT(cn.nukkit.nbt.tag.CompoundTag nukkitTag) {
        if (nukkitTag == null) return null;

        Map<String, com.sk89q.jnbt.Tag<?, ?>> tags = new HashMap<>();
        for (Map.Entry<String, Tag> entry : nukkitTag.getTags().entrySet()) {
            tags.put(entry.getKey(), convertTag(entry.getValue()));
        }
        return new com.sk89q.jnbt.CompoundTag(tags);
    }

    public static cn.nukkit.nbt.tag.CompoundTag toNukkit(com.sk89q.jnbt.CompoundTag jnbtTag) {
        if (jnbtTag == null) return null;

        cn.nukkit.nbt.tag.CompoundTag result = new cn.nukkit.nbt.tag.CompoundTag("");
        for (Map.Entry<String, com.sk89q.jnbt.Tag<?, ?>> entry : jnbtTag.getValue().entrySet()) {
            result.put(entry.getKey(), convertTag(entry.getValue()));
        }
        return result;
    }

    private static com.sk89q.jnbt.Tag convertTag(Tag nukkitTag) {
        if (nukkitTag instanceof cn.nukkit.nbt.tag.ByteTag) {
            return new com.sk89q.jnbt.ByteTag(((cn.nukkit.nbt.tag.ByteTag) nukkitTag).getData().byteValue());
        } else if (nukkitTag instanceof cn.nukkit.nbt.tag.ShortTag) {
            return new com.sk89q.jnbt.ShortTag(((cn.nukkit.nbt.tag.ShortTag) nukkitTag).getData().shortValue());
        } else if (nukkitTag instanceof cn.nukkit.nbt.tag.IntTag) {
            return new com.sk89q.jnbt.IntTag(((cn.nukkit.nbt.tag.IntTag) nukkitTag).getData());
        } else if (nukkitTag instanceof cn.nukkit.nbt.tag.LongTag) {
            return new com.sk89q.jnbt.LongTag(((cn.nukkit.nbt.tag.LongTag) nukkitTag).getData());
        } else if (nukkitTag instanceof cn.nukkit.nbt.tag.FloatTag) {
            return new com.sk89q.jnbt.FloatTag(((cn.nukkit.nbt.tag.FloatTag) nukkitTag).getData());
        } else if (nukkitTag instanceof cn.nukkit.nbt.tag.DoubleTag) {
            return new com.sk89q.jnbt.DoubleTag(((cn.nukkit.nbt.tag.DoubleTag) nukkitTag).getData());
        } else if (nukkitTag instanceof cn.nukkit.nbt.tag.ByteArrayTag) {
            return new com.sk89q.jnbt.ByteArrayTag(((cn.nukkit.nbt.tag.ByteArrayTag) nukkitTag).getData());
        } else if (nukkitTag instanceof cn.nukkit.nbt.tag.StringTag) {
            return new com.sk89q.jnbt.StringTag(((cn.nukkit.nbt.tag.StringTag) nukkitTag).parseValue());
        } else if (nukkitTag instanceof cn.nukkit.nbt.tag.ListTag) {
            List<Tag> nukkitList = ((cn.nukkit.nbt.tag.ListTag) nukkitTag).getAll();
            List<com.sk89q.jnbt.Tag> jnbtList = nukkitList.stream()
                    .map(ItemUtil::convertTag)
                    .collect(Collectors.toList());
            return new com.sk89q.jnbt.ListTag(getListTagType(jnbtList), jnbtList);
        } else if (nukkitTag instanceof cn.nukkit.nbt.tag.CompoundTag) {
            return toJNBT((cn.nukkit.nbt.tag.CompoundTag) nukkitTag);
        }
        throw new IllegalArgumentException("Unsupported tag type: " + nukkitTag.getClass().getName());
    }

    private static Tag convertTag(com.sk89q.jnbt.Tag jnbtTag) {
        if (jnbtTag instanceof com.sk89q.jnbt.ByteTag) {
            return new cn.nukkit.nbt.tag.ByteTag("", ((com.sk89q.jnbt.ByteTag) jnbtTag).getValue());
        } else if (jnbtTag instanceof com.sk89q.jnbt.ShortTag) {
            return new cn.nukkit.nbt.tag.ShortTag("", ((com.sk89q.jnbt.ShortTag) jnbtTag).getValue());
        } else if (jnbtTag instanceof com.sk89q.jnbt.IntTag) {
            return new cn.nukkit.nbt.tag.IntTag("", ((com.sk89q.jnbt.IntTag) jnbtTag).getValue());
        } else if (jnbtTag instanceof com.sk89q.jnbt.LongTag) {
            return new cn.nukkit.nbt.tag.LongTag("", ((com.sk89q.jnbt.LongTag) jnbtTag).getValue());
        } else if (jnbtTag instanceof com.sk89q.jnbt.FloatTag) {
            return new cn.nukkit.nbt.tag.FloatTag("", ((com.sk89q.jnbt.FloatTag) jnbtTag).getValue());
        } else if (jnbtTag instanceof com.sk89q.jnbt.DoubleTag) {
            return new cn.nukkit.nbt.tag.DoubleTag("", ((com.sk89q.jnbt.DoubleTag) jnbtTag).getValue());
        } else if (jnbtTag instanceof com.sk89q.jnbt.ByteArrayTag) {
            return new cn.nukkit.nbt.tag.ByteArrayTag("", ((com.sk89q.jnbt.ByteArrayTag) jnbtTag).getValue());
        } else if (jnbtTag instanceof com.sk89q.jnbt.StringTag) {
            return new cn.nukkit.nbt.tag.StringTag("", ((com.sk89q.jnbt.StringTag) jnbtTag).getValue());
        } else if (jnbtTag instanceof com.sk89q.jnbt.ListTag) {
            cn.nukkit.nbt.tag.ListTag listTag = new cn.nukkit.nbt.tag.ListTag("");
            LinListTag<? extends LinTag> linTagList = ((ListTag<?, ?>) jnbtTag).toLinTag();
            for (LinTag<?> linTag : linTagList.value()) {
                if (linTag instanceof LinByteTag) {
                    listTag.add(new cn.nukkit.nbt.tag.ByteTag("", ((LinByteTag) linTag).value()));
                } else if (linTag instanceof LinShortTag) {
                    listTag.add(new cn.nukkit.nbt.tag.ShortTag("", ((LinShortTag) linTag).value()));
                } else if (linTag instanceof LinIntTag) {
                    listTag.add(new cn.nukkit.nbt.tag.IntTag("", ((LinIntTag) linTag).value()));
                } else if (linTag instanceof LinLongTag) {
                    listTag.add(new cn.nukkit.nbt.tag.LongTag("", ((LinLongTag) linTag).value()));
                } else if (linTag instanceof LinFloatTag) {
                    listTag.add(new cn.nukkit.nbt.tag.FloatTag("", ((LinFloatTag) linTag).value()));
                } else if (linTag instanceof LinDoubleTag) {
                    listTag.add(new cn.nukkit.nbt.tag.DoubleTag("", ((LinDoubleTag) linTag).value()));
                } else if (linTag instanceof LinByteArrayTag) {
                    listTag.add(new cn.nukkit.nbt.tag.ByteArrayTag("", ((LinByteArrayTag) linTag).value()));
                } else if (linTag instanceof LinStringTag) {
                    listTag.add(new cn.nukkit.nbt.tag.StringTag("", ((LinStringTag) linTag).value()));
                } else if (linTag instanceof LinCompoundTag) {
                    listTag.add(convertTag(new com.sk89q.jnbt.CompoundTag((LinCompoundTag) linTag)));
                } else if (linTag instanceof LinListTag<?>) {
                    listTag.add(convertTag(new com.sk89q.jnbt.ListTag<>((LinListTag) linTag)));
                }
            }
            return listTag;
        } else if (jnbtTag instanceof com.sk89q.jnbt.CompoundTag) {
            return toNukkit((com.sk89q.jnbt.CompoundTag) jnbtTag);
        }
        throw new IllegalArgumentException("Unsupported tag type: " + jnbtTag.getClass().getName());
    }

    private static Class<? extends com.sk89q.jnbt.Tag> getListTagType(List<com.sk89q.jnbt.Tag> list) {
        if (list.isEmpty()) {
            return com.sk89q.jnbt.EndTag.class;
        }
        return list.get(0).getClass();
    }
}

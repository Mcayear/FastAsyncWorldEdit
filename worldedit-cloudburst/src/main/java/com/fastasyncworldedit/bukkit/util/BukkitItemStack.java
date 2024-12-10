package com.fastasyncworldedit.bukkit.util;

import com.fastasyncworldedit.bukkit.FaweBukkit;
import com.fastasyncworldedit.core.Fawe;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.item.ItemType;
import cn.nukkit.item.Item;

import javax.annotation.Nullable;

public class BukkitItemStack extends BaseItemStack {

    private Item stack;
    private Object nativeItem;
    private boolean loadedNBT;

    public BukkitItemStack(Item stack) {
        super(BukkitAdapter.asItemType(stack.getNamespaceId()));
        this.stack = stack;
        this.nativeItem = this;
    }

    public BukkitItemStack(ItemType type, Item stack) {
        super(type);
        this.stack = stack;
    }

    @Override
    public int getAmount() {
        return stack.getCount();
    }

    @Nullable
    @Override
    public Object getNativeItem() {
        return nativeItem;
    }

    public Item getBukkitItemStack() {
        return stack;
    }

    @Override
    public boolean hasNbtData() {
        if (!loadedNBT) {
            return stack.hasMeta();
        }
        return super.hasNbtData();
    }

    @Nullable
    @Override
    public CompoundTag getNbtData() {
        if (!loadedNBT) {
            loadedNBT = true;
            ItemUtil util = Fawe.<FaweBukkit>platform().getItemUtil();
            if (util != null) {
                super.setNbtData(util.getNBT(stack));
            }
        }
        return super.getNbtData();
    }

    @Override
    public void setNbtData(@Nullable CompoundTag nbtData) {
        ItemUtil util = Fawe.<FaweBukkit>platform().getItemUtil();
        if (util != null) {
            stack = util.setNBT(stack, nbtData);
            nativeItem = null;
        }
        super.setNbtData(nbtData);
    }

}

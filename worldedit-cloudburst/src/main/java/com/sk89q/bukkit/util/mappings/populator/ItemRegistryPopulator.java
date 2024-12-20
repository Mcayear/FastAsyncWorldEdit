package com.sk89q.bukkit.util.mappings.populator;

import cn.nukkit.item.Item;
import com.google.common.collect.HashBiMap;
import com.sk89q.bukkit.util.mappings.MappingRegistries;
import com.sk89q.bukkit.util.mappings.type.ItemMappings;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.util.io.ResourceLoader;
import com.sk89q.worldedit.world.item.ItemType;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class ItemRegistryPopulator {

    public static ItemMappings load() {
        final ResourceLoader resourceLoader = WorldEditPlugin.getInstance().getInternalPlatform().getResourceLoader();

        try {
            URL resource = resourceLoader.getRootResource("mappings/items.json");
            if (resource != null) {
                try (InputStream stream = resource.openStream()) {
                    HashBiMap<ItemMappings.HashItem, ItemType> ITEMS_MAPPING = HashBiMap.create();
                    Object2ByteOpenHashMap<String> PNX_ITEMS_DEFAULT_DAMAGE = new Object2ByteOpenHashMap<>();
                    Map<String, Map<String, ?>> map2 = MappingRegistries.JSON_MAPPER.fromJson(
                            new InputStreamReader(stream),
                            Map.class
                    );
                    map2.forEach((k, v) -> {
                        var name = v.get("bedrock_identifier").toString();
                        Item nkItem = Item.fromString(name);
                        if (v.containsKey("bedrock_data") && nkItem.hasMeta() && nkItem.getDamage() == 0) {
                            nkItem.setDamage(Double.valueOf(v.get("bedrock_data").toString()).intValue());
                        }
                        PNX_ITEMS_DEFAULT_DAMAGE.put(name, (byte) nkItem.getDamage());
                        ItemType.REGISTRY.register(k, new ItemType(k));
                        ITEMS_MAPPING.put(ItemMappings.HashItem.of(nkItem), ItemType.REGISTRY.get(k));
                    });
                    PNX_ITEMS_DEFAULT_DAMAGE.trim();
                    return ItemMappings
                            .builder()
                            .mapping(ITEMS_MAPPING)
                            .itemDamageMapping(PNX_ITEMS_DEFAULT_DAMAGE)
                            .build();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("ItemMappings is null");
    }

}

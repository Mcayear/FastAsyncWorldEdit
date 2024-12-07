package com.sk89q.worldedit.cloudburst;

import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.world.registry.EntityRegistry;
import cn.nukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CloudburstEntityRegistry implements EntityRegistry {
    /**
     * Create a new entity using its ID.
     *
     * @param id the id
     * @return the entity, which may be null if the entity does not exist
     */
    @Nullable
    @Override
    public BaseEntity createFromId(final String id) {
        return null;
    }

//    @Override
//    public Collection<String> registerEntities() {
//        List<String> types = new ArrayList<>();
//        for (EntityType<?> type : org.cloudburstmc.server.registry.EntityRegistry.get().getEntityTypes()) {
//            types.add(type.getIdentifier().toString());
//        }
//        return types;
//    }
}

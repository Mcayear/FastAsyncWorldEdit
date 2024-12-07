package com.boydti.fawe.cloudburst.listener;


import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityExplodeEvent;

public class ChunkListener8Plus implements Listener {

    private final ChunkListener listener;

    public ChunkListener8Plus(ChunkListener listener) {
        this.listener = listener;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void event(EntityExplodeEvent event) {
        listener.reset();
    }
}

package com.breakinblocks.nautec.client.renderer.items;

import software.bernie.geckolib.constant.dataticket.DataTicket;

import java.util.function.BiConsumer;

public final class GeoStateData {
    private GeoStateData() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <D> void put(BiConsumer<?, ?> consumer, DataTicket<D> ticket, D value) {
        ((BiConsumer) consumer).accept(ticket, value);
    }
}

package cc.mewcraft.wakame.util.gson.typeadapters.immutable.immutable;

import com.google.common.collect.*;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.lang.reflect.Type;
import java.util.Map;

public class ImmutableTypeAdapters {
    private ImmutableTypeAdapters() {
    }

    public static Map<Type, JsonDeserializer<?>> immutableTypeMap() {
        return ImmutableMap.<Type, JsonDeserializer<?>>builder()
                .put(ImmutableList.class, new ImmutableListDeserializer())
                .put(ImmutableSet.class, new ImmutableSetDeserializer())
                .put(ImmutableSortedSet.class, new ImmutableSortedSetDeserializer())
                .put(ImmutableMap.class, new ImmutableMapDeserializer())
                .put(ImmutableBiMap.class, new ImmutableBiMapDeserializer())
                .put(ImmutableSortedMap.class, new ImmutableSortedMapDeserializer())
                .build();
    }

    public static GsonBuilder withImmutableCollectionSerializers(GsonBuilder builder) {
        immutableTypeMap().forEach(builder::registerTypeAdapter);
        return builder;
    }
}

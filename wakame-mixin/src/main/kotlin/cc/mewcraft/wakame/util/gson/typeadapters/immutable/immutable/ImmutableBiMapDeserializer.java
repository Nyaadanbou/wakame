package cc.mewcraft.wakame.util.gson.typeadapters.immutable.immutable;

import com.google.common.collect.ImmutableBiMap;

import java.util.Map;

public class ImmutableBiMapDeserializer extends BaseMapDeserializer<ImmutableBiMap<?, ?>> {

    @Override
    protected ImmutableBiMap<?, ?> buildFrom(Map<?, ?> map) {
        return ImmutableBiMap.copyOf(map);
    }

}

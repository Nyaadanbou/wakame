package cc.mewcraft.wakame.util.gson.typeadapters.immutable.immutable;

import com.google.common.collect.ImmutableSet;

import java.util.Collection;

public class ImmutableSetDeserializer extends BaseCollectionDeserializer<ImmutableSet<?>> {

    @Override
    protected ImmutableSet<?> buildFrom(Collection<?> collection) {
        return ImmutableSet.copyOf(collection);
    }

}

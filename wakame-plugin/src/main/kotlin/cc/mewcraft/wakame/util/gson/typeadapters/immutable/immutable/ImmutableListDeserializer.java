package cc.mewcraft.wakame.util.gson.typeadapters.immutable.immutable;

import com.google.common.collect.ImmutableList;

import java.util.Collection;

public class ImmutableListDeserializer extends BaseCollectionDeserializer<ImmutableList<?>> {

    @Override
    protected ImmutableList<?> buildFrom(Collection<?> collection) {
        return ImmutableList.copyOf(collection);
    }

}

package cc.mewcraft.wakame.util.gson.typeadapters.immutable.immutable;

import cc.mewcraft.wakame.util.gson.typeadapters.immutable.common.ImmutableDeserializerException;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

abstract class BaseMapDeserializer<E> implements JsonDeserializer<E> {
    protected abstract E buildFrom(Map<?, ?> map);

    public E deserialize(JsonElement json, Type type, JsonDeserializationContext context) {

        try {
            Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();
            Type parameterizedType = Types.hashMapOf(typeArguments[0], typeArguments[1]).getType();
            Map<?, ?> map = context.deserialize(json, parameterizedType);

            return buildFrom(map);
        } catch (Exception e) {
            throw new ImmutableDeserializerException(e);
        }
    }
}

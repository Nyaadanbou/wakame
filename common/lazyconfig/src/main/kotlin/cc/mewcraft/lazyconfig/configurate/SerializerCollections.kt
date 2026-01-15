@file:JvmName("SerializerCollections")

package cc.mewcraft.lazyconfig.configurate

import cc.mewcraft.lazyconfig.configurate.mapper.KoishObjectMapper
import cc.mewcraft.lazyconfig.configurate.serializer.*
import org.spongepowered.configurate.serialize.Scalars
import org.spongepowered.configurate.serialize.TypeSerializerCollection

/**
 * 这些序列化器可以处理 Java/Kotlin 标准库里的数据类型.
 */
val STANDARD_SERIALIZERS: TypeSerializerCollection = TypeSerializerCollection.builder()
    .registerExact(Scalars.STRING)
    .registerExact(Scalars.BOOLEAN)
    .register(MapSerializer.TYPE, MapSerializer(false))
    .register(ListSerializer.TYPE, ListSerializer())
    .registerExact(Scalars.BYTE)
    .registerExact(Scalars.SHORT)
    .registerExact(Scalars.INTEGER)
    .registerExact(Scalars.LONG)
    .registerExact(Scalars.FLOAT)
    .registerExact(Scalars.DOUBLE)
    .registerAnnotatedObjects(KoishObjectMapper.INSTANCE)
    .register(Scalars.ENUM)
    .registerExact(Scalars.CHAR)
    .registerExact(Scalars.URI)
    .registerExact(Scalars.URL)
    .registerExact(Scalars.UUID)
    .registerExact(Scalars.PATTERN)
    .register(ArraySerializer.Objects::accepts, ArraySerializer.Objects())
    .registerExact(ArraySerializer.Booleans.TYPE, ArraySerializer.Booleans())
    .registerExact(ArraySerializer.Bytes.TYPE, ArraySerializer.Bytes())
    .registerExact(ArraySerializer.Chars.TYPE, ArraySerializer.Chars())
    .registerExact(ArraySerializer.Shorts.TYPE, ArraySerializer.Shorts())
    .registerExact(ArraySerializer.Ints.TYPE, ArraySerializer.Ints())
    .registerExact(ArraySerializer.Longs.TYPE, ArraySerializer.Longs())
    .registerExact(ArraySerializer.Floats.TYPE, ArraySerializer.Floats())
    .registerExact(ArraySerializer.Doubles.TYPE, ArraySerializer.Doubles())
    .register(SetSerializer::accepts, SetSerializer())
    .register(ConfigurationNodeSerializer.TYPE, ConfigurationNodeSerializer())
    .register(Unit::class, UnitSerializer)
    .register(PathSerializer.TYPE, PathSerializer.INSTANCE)
    .build()

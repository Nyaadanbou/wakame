@file:JvmName("ConfigurateSerializerExtra")

package cc.mewcraft.wakame.util

import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import kotlin.reflect.KClass


// TypeSerializer extensions


inline fun <reified T> TypeSerializerCollection.Builder.register(serializer: TypeSerializer<T>): TypeSerializerCollection.Builder =
    this.register(typeTokenOf<T>(), serializer)

fun <T : Any> TypeSerializerCollection.Builder.register(type: KClass<T>, serializer: TypeSerializer<T>): TypeSerializerCollection.Builder =
    this.register(type.java, serializer)

inline fun <reified T> TypeSerializerCollection.Builder.registerExact(serializer: TypeSerializer<T>): TypeSerializerCollection.Builder =
    this.registerExact(typeTokenOf<T>(), serializer)

fun <T : Any> TypeSerializerCollection.Builder.registerExact(type: KClass<T>, serializer: TypeSerializer<T>): TypeSerializerCollection.Builder =
    this.registerExact(type.java, serializer)

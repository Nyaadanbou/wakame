@file:JvmName("ConfigurateExtras")

package cc.mewcraft.lazyconfig.configurate

import cc.mewcraft.lazyconfig.access.typeTokenOf
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


// Node extensions


inline fun <reified T> ConfigurationNode.require(): T =
    this.require(typeOf<T>())

fun <T : Any> ConfigurationNode.require(clazz: KClass<T>): T =
    this.get(clazz) ?: throw NoSuchElementException("Cannot parse value of type $clazz at [${path().joinToString()}]")

fun <T> ConfigurationNode.require(type: KType): T =
    this.get(type) as T ?: throw NoSuchElementException("Cannot parse value of type $type at [${path().joinToString()}]")


// SimpleSerializer extensions


fun <T : Any> TypeSerializerCollection.Builder.register(type: KClass<T>, serializer: SimpleSerializer<T>): TypeSerializerCollection.Builder =
    this.register(type.java, serializer)

fun <T : Any> TypeSerializerCollection.Builder.registerExact(type: KClass<T>, serializer: SimpleSerializer<T>): TypeSerializerCollection.Builder =
    this.registerExact(type.java, serializer)

inline fun <reified T> TypeSerializerCollection.Builder.register(serializer: SimpleSerializer<T>): TypeSerializerCollection.Builder =
    this.register(typeTokenOf<T>(), serializer)

inline fun <reified T> TypeSerializerCollection.Builder.registerExact(serializer: SimpleSerializer<T>): TypeSerializerCollection.Builder =
    this.registerExact(typeTokenOf<T>(), serializer)


// TypeSerializer extensions


fun <T : Any> TypeSerializerCollection.Builder.register(type: KClass<T>, serializer: TypeSerializer<T>): TypeSerializerCollection.Builder =
    this.register(type.java, serializer)

fun <T : Any> TypeSerializerCollection.Builder.registerExact(type: KClass<T>, serializer: TypeSerializer<T>): TypeSerializerCollection.Builder =
    this.registerExact(type.java, serializer)

inline fun <reified T> TypeSerializerCollection.Builder.register(serializer: TypeSerializer<T>): TypeSerializerCollection.Builder =
    this.register(typeTokenOf<T>(), serializer)

inline fun <reified T> TypeSerializerCollection.Builder.registerExact(serializer: TypeSerializer<T>): TypeSerializerCollection.Builder =
    this.registerExact(typeTokenOf<T>(), serializer)

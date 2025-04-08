@file:JvmName("ConfigurateNodeExtra")

package cc.mewcraft.wakame.util

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


// Node extensions


/* internal */ inline fun <reified T> ConfigurationNode.require(): T =
    this.require(typeOf<T>())

/* internal */ fun <T : Any> ConfigurationNode.require(clazz: KClass<T>): T =
    this.get(clazz) ?: throw NoSuchElementException("Can't parse value of type '${clazz}' at '[${path().joinToString()}]'")

/* internal */ fun <T> ConfigurationNode.require(type: KType): T =
    this.get(type) as T ?: throw NoSuchElementException("Can't parse value of type '${type}' at '[${path().joinToString()}]'")

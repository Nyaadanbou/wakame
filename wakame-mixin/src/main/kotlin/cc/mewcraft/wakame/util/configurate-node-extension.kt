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

@Deprecated("Deprecated", replaceWith = ReplaceWith("this.require<T>()"))
/* internal */ inline fun <reified T> ConfigurationNode.krequire(): T =
    this.require(typeOf<T>())

@Deprecated("Deprecated", replaceWith = ReplaceWith("this.require<T>(clazz)"))
        /* internal */ fun <T : Any> ConfigurationNode.krequire(clazz: KClass<T>): T =
    this.require(clazz)

@Deprecated("Deprecated", replaceWith = ReplaceWith("this.require<T>(type)"))
        /* internal */ fun <T> ConfigurationNode.krequire(type: KType): T =
    this.require(type)

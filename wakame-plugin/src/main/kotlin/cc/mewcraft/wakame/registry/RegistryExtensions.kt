package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.BiIdentified
import cc.mewcraft.wakame.annotation.InternalApi

/**
 * Gets an element by its binary identifier.
 *
 * @param binary the binary identifier
 * @param R the type of `this` registry
 * @param B the type of binary
 * @param K the type of name (key)
 * @param V the type of value
 * @return the specified element or `null`
 */
fun <R, B, K, V : BiIdentified<K, B>> R.getBy(binary: B): V?
        where R : Registry<K, V>,
              R : BiMapRegistry<K, B> {
    return get(getNameBy(binary))
}

/**
 * Gets an element by its binary identifier.
 *
 * @param binary the binary identifier
 * @param R the type of `this` registry
 * @param B the type of binary
 * @param K the type of name (key)
 * @param V the type of value
 * @return the specified element
 * @throws IllegalStateException if the element you look for does not exist
 */
fun <R, B, K, V : BiIdentified<K, B>> R.getByOrThrow(binary: B): V
        where R : Registry<K, V>,
              R : BiMapRegistry<K, B> {
    return getOrThrow(getNameByOrThrow(binary))
}

/**
 * Register the element to **both** plain [Registry] and [BiMapRegistry].
 *
 * @param name the name of the element
 * @param value the element to be registered
 * @param R the type of `this` registry
 * @param B the type of binary
 * @param K the type of name (key)
 * @param V the type of value
 */
fun <R, B, K, V : BiIdentified<K, B>> R.registerBothMapping(name: K, value: V)
        where R : Registry<K, V>,
              R : BiMapRegistry<K, B> {
    registerName2Object(name, value)
    registerBinary2Name(name, value.binary)
}

/**
 * Clear both the base registry and bi-map registry.
 *
 * @param R the type of `this` registry
 * @param B the type of binary
 * @param K the type of name (key)
 * @param V the type of value
 */
@InternalApi
fun <R, B, K, V : BiIdentified<K, B>> R.clearBoth()
        where R : Registry<K, V>,
              R : BiMapRegistry<K, B> {
    clearName2Object()
    clearBinary2Name()
}

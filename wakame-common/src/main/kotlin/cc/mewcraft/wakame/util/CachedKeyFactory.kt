package cc.mewcraft.wakame.util

import net.kyori.adventure.key.Key
import net.kyori.adventure.key.KeyPattern
import net.kyori.adventure.key.Namespaced

//
// This is a factory for adventure `Key` with the ability to cache already created objects
//

private val KEY_POOL: MutableMap<String, MutableMap<String, Key>> = HashMap()

/**
 * @see Key.key
 */
fun Key(@KeyPattern string: String): Key {
    return Key(string, Key.DEFAULT_SEPARATOR)
}

/**
 * @see Key.key
 */
fun Key(string: String, character: Char): Key {
    // copy from adventure source code
    val index: Int = string.indexOf(character)
    val namespace = if (index >= 1) string.substring(0, index) else Key.MINECRAFT_NAMESPACE
    val value = if (index >= 0) string.substring(index + 1) else string
    return Key(namespace, value)
}

/**
 * @see Key.key
 */
fun Key(namespaced: Namespaced, @KeyPattern.Value value: String): Key {
    return Key(namespaced.namespace(), value)
}

/**
 * @see Key.key
 */
fun Key(@KeyPattern.Namespace namespace: String, @KeyPattern.Value value: String): Key {
    return KEY_POOL.getOrPut(namespace) { HashMap() }.getOrPut(value) { Key.key(namespace, value) }
}

package cc.mewcraft.wakame.util

import net.kyori.adventure.key.Key
import net.kyori.adventure.key.KeyPattern
import net.kyori.adventure.key.Namespaced

//
// This is a factory for adventure `Key` with the ability to cache already created objects.
//

private val KEY_POOL: MutableMap<String, MutableMap<String, Key>> = HashMap()

fun Key(@KeyPattern string: String): Key {
    return Key(string, Key.DEFAULT_SEPARATOR)
}

fun Key(string: String, character: Char): Key {
    val index: Int = string.indexOf(character)
    val namespace = if (index >= 1) string.substring(0, index) else Key.MINECRAFT_NAMESPACE
    val value = if (index >= 0) string.substring(index + 1) else string
    return Key(namespace, value)
}

fun Key(namespaced: Namespaced, @KeyPattern.Value value: String): Key {
    return Key(namespaced.namespace(), value)
}

fun Key(@KeyPattern.Namespace namespace: String, @KeyPattern.Value value: String): Key {
    return KEY_POOL.getOrPut(namespace) { HashMap() }.getOrPut(value) { Key.key(namespace, value) }
}

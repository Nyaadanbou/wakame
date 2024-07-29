package cc.mewcraft.wakame.util

import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey
import org.jetbrains.annotations.Contract

val Key.toNamespacedKey: NamespacedKey
    get() = NamespacedKey(this.namespace(), this.value())

@Contract(pure = true)
fun Key.toNamespacedKey(): NamespacedKey =
    NamespacedKey(this.namespace(), this.value())

/**
 * 修改 [Key.namespace] 为 [namespace].
 */
@Contract(pure = true)
fun Key.namespace(namespace: String): Key =
    Key.key(namespace, this.value())

/**
 * 修改 [Key.namespace] 为 [block] 的返回值.
 */
@Contract(pure = true)
inline fun Key.namespace(block: (String) -> String): Key =
    Key.key(block(this.namespace()), this.value())

/**
 * 修改 [Key.value] 为 [value].
 */
@Contract(pure = true)
fun Key.value(value: String): Key =
    Key.key(this.namespace(), value)

/**
 * 修改 [Key.value] 为 [block] 的返回值.
 */
@Contract(pure = true)
inline fun Key.value(block: (String) -> String): Key =
    Key.key(this.namespace(), block(this.value()))
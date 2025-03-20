package cc.mewcraft.wakame.util.adventure

import cc.mewcraft.wakame.util.KOISH_NAMESPACE
import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey

val Key.toNamespacedKey: NamespacedKey
    get() = NamespacedKey(this.namespace(), this.value())

fun Key.toNamespacedKey(): NamespacedKey =
    NamespacedKey(this.namespace(), this.value())

/**
 * 修改 [Key.withNamespace] 为 [withNamespace].
 */
fun Key.withNamespace(namespace: String): Key =
    Key.key(namespace, this.value())

/**
 * 修改 [Key.withNamespace] 为 [block] 的返回值.
 */
inline fun Key.withNamespace(block: (String) -> String): Key =
    Key.key(block(this.namespace()), this.value())

/**
 * 修改 [Key.withValue] 为 [withValue].
 */
fun Key.withValue(value: String): Key =
    Key.key(this.namespace(), value)

/**
 * 修改 [Key.withValue] 为 [block] 的返回值.
 */
inline fun Key.withValue(block: (String) -> String): Key =
    Key.key(this.namespace(), block(this.value()))

/**
 * 返回 [Key] 的最小化字符串表示.
 * 当命名空间为 [KOISH_NAMESPACE] 时, 返回值部分不包含命名空间.
 * 否则, 返回完整的 `命名空间:路径` 形式.
 */
fun Key.asMinimalStringKoish(): String =
    if (namespace() == KOISH_NAMESPACE) value() else asString()

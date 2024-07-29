package cc.mewcraft.wakame.util

import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey
import org.jetbrains.annotations.Contract

val Key.toNamespacedKey: NamespacedKey
    get() = NamespacedKey(this.namespace(), this.value())


val Key.toNamespacedKey: NamespacedKey
    get() = NamespacedKey(this.namespace(), this.value())
/**
 * 修改 [Key.value] 为 [block] 的返回值.
 */
@Contract(pure = true)
inline fun Key.value(block: (String) -> String): Key =
    Key.key(this.namespace(), block(this.value()))
package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.util.KOISH_NAMESPACE
import net.kyori.adventure.key.Key

data class ItemModelInfo(
    val itemId: Key,
    val base: Key,
) {
    fun modelKey(): Key {
        return Key.key(KOISH_NAMESPACE, "item/${itemId.namespace()}/${itemId.value()}")
    }
}

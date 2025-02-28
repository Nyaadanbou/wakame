package cc.mewcraft.wakame.pack

import net.kyori.adventure.key.Key

data class ItemModelInfo(
    val itemId: Key,
    val base: Key,
) {
    fun modelKey(): Key {
        return Key.key(RESOURCE_NAMESPACE, "item/${itemId.namespace()}/${itemId.value()}")
    }
}

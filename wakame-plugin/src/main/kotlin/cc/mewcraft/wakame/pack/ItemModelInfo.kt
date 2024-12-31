package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key

data class ItemModelInfo(
    val itemId: Key,
    val base: Key
) {
    fun modelKey(): Key {
        return Key(RESOURCE_NAMESPACE, "item/${itemId.namespace()}/${itemId.value()}")
    }

    fun baseModelKey(): Key {
        return Key(base.namespace(), "item/${base.value()}")
    }
}

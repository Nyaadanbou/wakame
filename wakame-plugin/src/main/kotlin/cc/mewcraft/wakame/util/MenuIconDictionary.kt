package cc.mewcraft.wakame.util

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class MenuIconDictionary(
    @Setting(nodeFromParent = true)
    private val dictionary: Map<String, String> = emptyMap(),
) {
    operator fun get(key: String): String? {
        return dictionary[key]
    }
}

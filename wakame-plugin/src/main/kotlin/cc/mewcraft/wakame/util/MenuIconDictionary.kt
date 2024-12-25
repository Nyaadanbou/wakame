package cc.mewcraft.wakame.util

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class MenuIconDictionary(
    private val dictionary: Map<String, String> = emptyMap(),
) {
    operator fun get(key: String): String? {
        return dictionary[key]
    }
}

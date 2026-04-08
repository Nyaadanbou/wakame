package cc.mewcraft.wakame.bridge.item

import com.mojang.serialization.Codec
import kotlin.properties.Delegates

interface ServerItemDataContainer {
    companion object {
        @JvmStatic
        var codec: Codec<ServerItemDataContainer> by Delegates.notNull()
    }
}
package cc.mewcraft.wakame.bridge.item

import com.mojang.serialization.Codec
import kotlin.properties.Delegates

interface ServerItemKey {
    companion object {
        @JvmStatic
        var codec: Codec<ServerItemKey> by Delegates.notNull()
    }
}
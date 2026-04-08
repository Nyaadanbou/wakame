package cc.mewcraft.wakame.bridge.serialization.codec

import cc.mewcraft.wakame.KoishSharedConstants
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.kyori.adventure.key.Key

/**
 * Koish 数据类型的 Codec, 强依赖于 Koish.
 */
object AdventureCodecs {
    @JvmField
    val KEY_WITH_MINECRAFT_NAMESPACE: Codec<Key> = Codec.STRING.comapFlatMap({ key ->
        try {
            DataResult.success(Key.key(Key.MINECRAFT_NAMESPACE, key))
        } catch (e: IllegalArgumentException) {
            DataResult.error { "Invalid key: '$key' (${e.message})" }
        }
    }, Any::toString).stable()

    @JvmField
    val KEY_WITH_KOISH_NAMESPACE: Codec<Key> = Codec.STRING.comapFlatMap({ key ->
        try {
            DataResult.success(Key.key(KoishSharedConstants.KOISH_NAMESPACE, key))
        } catch (e: IllegalArgumentException) {
            DataResult.error { "Invalid key: '$key' (${e.message})" }
        }
    }, Any::toString).stable()
}
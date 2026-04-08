package cc.mewcraft.wakame.bridge.codec

import cc.mewcraft.wakame.KoishSharedConstants
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.kyori.adventure.key.Key

/**
 * Koish 数据类型的 Codec, 强依赖于 Koish.
 */
object AdventureCodecs {
    @JvmField
    val KEY_WITH_MINECRAFT_NAMESPACE: Codec<Key> = Codec.STRING.comapFlatMap(::validateMinecraftKey, Any::toString).stable()

    @JvmField
    val KEY_WITH_KOISH_NAMESPACE: Codec<Key> = Codec.STRING.comapFlatMap(::validateKoishKey, Any::toString).stable()

    private fun validateMinecraftKey(key: String): DataResult<Key> = try {
        DataResult.success(Key.key(key))
    } catch (e: IllegalArgumentException) {
        DataResult.error { "Invalid key: '$key' (${e.message})" }
    }

    private fun validateKoishKey(key: String): DataResult<Key> = try {
        DataResult.success(Key.key(KoishSharedConstants.KOISH_NAMESPACE, key))
    } catch (e: IllegalArgumentException) {
        DataResult.error { "Invalid key: '$key' (${e.message})" }
    }
}
package cc.mewcraft.wakame.mixin.support.codec

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.kyori.adventure.key.Key

internal object KeyCodec {

    @JvmStatic
    val INSTANCE: Codec<Key> = Codec.STRING.comapFlatMap(this::read, Key::toString).stable()

    private fun read(key: String): DataResult<Key> = try {
        DataResult.success(Key.key(key))
    } catch (e: IllegalArgumentException) {
        DataResult.error { "Invalid key: '$key' (${e.message})" }
    }
}
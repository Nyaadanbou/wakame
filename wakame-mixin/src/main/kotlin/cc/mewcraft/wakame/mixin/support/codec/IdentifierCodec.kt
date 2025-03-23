package cc.mewcraft.wakame.mixin.support.codec

import cc.mewcraft.wakame.util.Identifier
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult

object IdentifierCodec {

    @JvmStatic
    val INSTANCE: Codec<Identifier> = Codec.STRING.comapFlatMap(this::read, Identifier::toString).stable()

    private fun read(key: String): DataResult<Identifier> = try {
        DataResult.success(Identifier.key(key))
    } catch (e: IllegalArgumentException) {
        DataResult.error { "Invalid key: '$key' (${e.message})" }
    }

}
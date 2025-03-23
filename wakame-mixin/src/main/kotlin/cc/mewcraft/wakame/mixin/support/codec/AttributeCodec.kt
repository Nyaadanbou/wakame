package cc.mewcraft.wakame.mixin.support.codec

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeProvider
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult

object AttributeCodec {

    @JvmStatic
    val INSTANCE: Codec<Attribute> = Codec.STRING.comapFlatMap(this::read, Attribute::id).stable()

    private fun read(id: String): DataResult<Attribute> {
        return try {
            DataResult.success(AttributeProvider.instance().get(id) ?: throw IllegalArgumentException())
        } catch (e: Exception) {
            DataResult.error { "Unknown attribute id: $id" }
        }
    }

}
package cc.mewcraft.wakame.serialization.codec

import cc.mewcraft.wakame.entity.attribute.Attribute
import cc.mewcraft.wakame.entity.attribute.AttributeModifier
import cc.mewcraft.wakame.entity.attribute.AttributeProvider
import cc.mewcraft.wakame.util.StringRepresentable
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult

/**
 * Koish 数据类型的 Codec, 强依赖于 Koish.
 */
object KoishCodecs {
    @JvmField
    val ATTRIBUTE: Codec<Attribute> = Codec.STRING.comapFlatMap({ id ->
        try {
            DataResult.success(AttributeProvider.get(id) ?: throw IllegalArgumentException())
        } catch (e: Exception) {
            DataResult.error { "Unknown attribute id: $id (${e.message})" }
        }
    }, Attribute::id).stable()

    @JvmField
    val ATTRIBUTE_MODIFIER_OPERATION: Codec<AttributeModifier.Operation> =
        StringRepresentable.fromValues(AttributeModifier.Operation::values)
}
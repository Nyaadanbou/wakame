package cc.mewcraft.wakame.serialization.codec

import cc.mewcraft.wakame.entity.attribute.Attribute
import cc.mewcraft.wakame.entity.attribute.AttributeModifier
import cc.mewcraft.wakame.entity.attribute.AttributeProvider
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.StringIdentifiable
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult

/**
 * Koish 数据类型的 Codec, 强依赖于 Koish.
 */
object KoishCodecs {

    @JvmField
    val IDENTIFIER: Codec<Identifier> = Codec.STRING.comapFlatMap({ key ->
        try {
            // 因为 Codec 一般用于 NMS, 而 NMS 里命名空间通常默认是“minecraft”,
            // 特别是数据包的命名空间, 所以这里遵循 NMS 的传统也默认为“minecraft”.
            DataResult.success(Identifier.key(key))
        } catch (e: IllegalArgumentException) {
            DataResult.error { "Invalid key: '$key' (${e.message})" }
        }
    }, Identifier::toString).stable()

    @JvmField
    val ATTRIBUTE: Codec<Attribute> = Codec.STRING.comapFlatMap({ id ->
        try {
            DataResult.success(AttributeProvider.instance().get(id) ?: throw IllegalArgumentException())
        } catch (e: Exception) {
            DataResult.error { "Unknown attribute id: $id (${e.message})" }
        }
    }, Attribute::id).stable()

    @JvmField
    val ATTRIBUTE_MODIFIER_OPERATION: Codec<AttributeModifier.Operation> = StringIdentifiable.createCodec(AttributeModifier.Operation::values)

}
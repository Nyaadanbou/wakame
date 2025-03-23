package cc.mewcraft.wakame.mixin.support.codec

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.util.StringIdentifiable
import com.mojang.serialization.Codec

object AttributeModifierOperationCodec {

    @JvmStatic
    val INSTANCE: Codec<AttributeModifier.Operation> = StringIdentifiable.createCodec(AttributeModifier.Operation::values)

}
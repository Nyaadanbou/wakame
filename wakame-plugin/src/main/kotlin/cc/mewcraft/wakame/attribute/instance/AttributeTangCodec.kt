package cc.mewcraft.wakame.attribute.instance

import cc.mewcraft.wakame.attribute.AttributeBinaryValue
import cc.mewcraft.wakame.attribute.AttributeSchemaValue
import cc.mewcraft.wakame.item.TangCodec

/**
 * Operations of manipulating various data of attributes。
 */
sealed interface AttributeTangCodec<B : AttributeBinaryValue, S : AttributeSchemaValue> : TangCodec<B, S>
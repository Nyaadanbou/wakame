package cc.mewcraft.wakame.attribute.instance

import cc.mewcraft.wakame.attribute.BinaryAttributeValue
import cc.mewcraft.wakame.attribute.SchemeAttributeValue
import cc.mewcraft.wakame.item.CoreCodec

/**
 * Operations of manipulating the data of attributes。
 */
sealed interface AttributeCoreCodec<B : BinaryAttributeValue, S : SchemeAttributeValue> : CoreCodec<B, S>
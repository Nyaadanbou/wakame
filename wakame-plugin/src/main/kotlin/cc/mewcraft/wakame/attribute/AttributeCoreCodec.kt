package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.item.CoreCodec

/**
 * Operations of manipulating the data of attributes。
 */
interface AttributeCoreCodec<B : BinaryAttributeValue, S : SchemeAttributeValue> : CoreCodec<B, S>
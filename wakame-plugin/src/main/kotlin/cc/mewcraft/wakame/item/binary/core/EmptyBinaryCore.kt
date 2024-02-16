package cc.mewcraft.wakame.item.binary.core

import cc.mewcraft.wakame.annotation.InternalApi
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag

/**
 * By design, an empty [BinaryCore] is a special core in which the player
 * can replace it with something else. See the "reforge" module for more
 * details.
 */
@InternalApi
internal object EmptyBinaryCore : BinaryCore {
    override val key: Nothing get() = throw UnsupportedOperationException("EmptyBinaryCore has no key")
    override val value: Nothing get() = throw UnsupportedOperationException("EmptyBinaryCore has no value")
    override fun asShadowTag(): ShadowTag = CompoundShadowTag.create()
}
package cc.mewcraft.wakame.item.binary.cell.core.empty

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.item.binary.cell.core.BinaryCore
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * A constructor function o create [BinaryEmptyCore].
 */
fun BinaryEmptyCore(): BinaryEmptyCore {
    // It's created from the configuration,
    // that means it's the same across the whole config,
    // so a singleton object would be an appropriate option.
    return BinaryEmptyCoreImpl
}

/**
 * By design, an empty [BinaryCore] is a special core in which the player
 * can replace it with something else. See the "reforge" module for more
 * details.
 */
sealed interface BinaryEmptyCore : BinaryCore

//
// Internal Implementations
//

private data object BinaryEmptyCoreImpl : BinaryEmptyCore {
    private val EMPTY_COMPOUND = CompoundShadowTag.create()
    override val key: Key = GenericKeys.EMPTY
    override fun asShadowTag(): ShadowTag = EMPTY_COMPOUND
    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key)
    )
}

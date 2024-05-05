package cc.mewcraft.wakame.item.binary.cell.core.noop

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.item.binary.cell.core.BinaryCore
import cc.mewcraft.wakame.util.toSimpleString
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * A constructor function to create [BinaryNoopCore].
 */
fun BinaryNoopCore(): BinaryNoopCore {
    return BinaryNoopCoreImpl
}

/**
 * By design, an [BinaryNoopCore] is a "mark" core which is used to
 * indicate that it should never be written to the final item NBT.
 */
sealed interface BinaryNoopCore : BinaryCore

//
// Internal Implementations
//

private data object BinaryNoopCoreImpl : BinaryNoopCore {
    override val key: Key = GenericKeys.NOOP
    override fun asTag(): ShadowTag = throwUoe()
    override fun provideTagResolverForPlay(): TagResolver = TagResolver.empty()
    override fun provideTagResolverForShow(): TagResolver = TagResolver.empty()
    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(ExaminableProperty.of("key", key))
    override fun toString(): String = toSimpleString()
    private fun throwUoe(): Nothing = throw UnsupportedOperationException("${toString()} does not support this operation")
}
package cc.mewcraft.wakame.item.components.cell.cores.noop

import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.item.components.cell.Core
import cc.mewcraft.wakame.item.components.cell.CoreType
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * By design, an [CoreNoop] is a "mark" core which is used to
 * indicate that it should never be written to the item NBT.
 */
object CoreNoop : Core, CoreType<CoreNoop> {
    override val key: Key = GenericKeys.NOOP
    override val type: CoreType<*> = this
    override val isNoop: Boolean = true
    override val isEmpty: Boolean = false

    override fun asTag(): Tag = error("No-op core does not support this operation")
    override fun provideTooltipLore(): LoreLine = error("No-op core does not support this operation")

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(ExaminableProperty.of("key", key))
    override fun toString(): String = toSimpleString()
}
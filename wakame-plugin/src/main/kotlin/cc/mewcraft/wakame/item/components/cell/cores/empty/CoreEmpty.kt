package cc.mewcraft.wakame.item.components.cell.cores.empty

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.item.components.cell.Core
import cc.mewcraft.wakame.item.components.cell.CoreType
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

// It's created from the configuration,
// that means it's the same across the whole config,
// so a singleton object would be an appropriate option.

/**
 * By design, an [CoreEmpty] is a special core in which the player
 * can replace it with something else. It works in combination with
 * the reforge system.
 */
object CoreEmpty : Core, CoreType<CoreEmpty> {
    override val key: Key = GenericKeys.EMPTY
    override val type: CoreType<CoreEmpty> = this
    override val isNoop: Boolean = false
    override val isEmpty: Boolean = true

    override fun asTag(): Tag = CompoundTag.create() // TODO 复用对象?
    override fun provideTooltipLore(): LoreLine = EmptyLoreLine

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(ExaminableProperty.of("key", key))
    override fun toString(): String = toSimpleString()
}
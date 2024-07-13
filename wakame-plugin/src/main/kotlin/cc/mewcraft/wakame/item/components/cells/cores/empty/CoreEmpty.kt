package cc.mewcraft.wakame.item.components.cells.cores.empty

import cc.mewcraft.commons.provider.immutable.map
import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.config.derive
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.display.CyclingLoreLineProvider
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.CoreType
import cc.mewcraft.wakame.registry.ItemComponentRegistry
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
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

    override fun serializeAsTag(): Tag {
        return CompoundTag.create()
    }

    override fun provideTooltipLore(): LoreLine {
        return loreLineProvider.next()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(ExaminableProperty.of("key", key))
    }

    override fun toString(): String {
        return toSimpleString()
    }

    private val loreLineProvider = CyclingLoreLineProvider(CoreEmptyDisplaySupport.MAX_DISPLAY_COUNT) { index ->
        LoreLine.supply(
            CoreEmptyDisplaySupport.derive(GenericKeys.EMPTY, index),
            ItemComponentRegistry.CONFIG.derive(ItemComponentConstants.CELLS).entry<Component>("tooltips", "empty").map(::listOf)
        )
    }
}
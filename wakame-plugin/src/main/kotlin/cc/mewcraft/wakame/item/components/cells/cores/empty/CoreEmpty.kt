package cc.mewcraft.wakame.item.components.cells.cores.empty

import cc.mewcraft.commons.provider.immutable.mapNonNull
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.config.*
import cc.mewcraft.wakame.display.*
import cc.mewcraft.wakame.display2.RendererSystemName
import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentRegistry
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.CoreType
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

    override fun isSimilar(other: Core): Boolean {
        return other === this
    }

    override fun serializeAsTag(): Tag {
        return CompoundTag.create()
    }

    override fun provideTooltipName(): NameLine {
        return nameLine
    }

    override fun provideTooltipLore(systemName: RendererSystemName): LoreLine {
        return getLoreLine(systemName).next()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(ExaminableProperty.of("key", key))
    }

    override fun toString(): String {
        return toSimpleString()
    }

    private val nameLine: NameLine = NameLine.supply(
        ItemComponentRegistry.CONFIG.derive(ItemConstants.CELLS).entry<Component>("tooltip", "empty_core", "name")
    )

    private fun getLoreLine(systemName: RendererSystemName): CyclingLoreLineProvider = CyclingLoreLineProvider(
        CoreEmptyDisplaySupport.MAX_DISPLAY_COUNT
    ) { index ->
        val tooltipKey = CoreEmptyDisplaySupport.derive(GenericKeys.EMPTY, index)
        val provider = ItemComponentRegistry.getDescriptorsByRendererSystemName(systemName).derive(ItemConstants.CELLS).optionalEntry<Component>("tooltips", "empty_core", "lore").mapNonNull(::listOf).orElse(emptyList())
        LoreLine.supply(tooltipKey, provider)
    }
}
package cc.mewcraft.wakame.display

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.koin.core.component.KoinComponent

internal class LoreLineComparator(
    private val loreOrderSupplier: LineIndexSupplier,
) : KoinComponent, Comparator<LoreLine> {
    override fun compare(o1: LoreLine?, o2: LoreLine?): Int {
        requireNotNull(o1)
        requireNotNull(o2)
        val o1Idx = loreOrderSupplier.getIndex(o1.key)
        val o2Idx = loreOrderSupplier.getIndex(o2.key)
        return if (o1Idx < o2Idx) -1 else if (o1Idx > o2Idx) +1 else 0
    }
}

// TODO use flyweight pattern to reduce memory footprint

internal data class FixedLoreLineImpl(
    override val key: Key,
    override val line: List<Component>,
) : FixedLoreLine

internal data class MetaLoreLineImpl(
    override val key: Key,
    override val line: List<Component>,
) : MetaLoreLine

internal data class AttributeLoreLineImpl(
    override val key: Key,
    override val line: List<Component>,
) : AttributeLoreLine

internal data class AbilityLoreLineImpl(
    override val key: Key,
    override val line: List<Component>,
) : AbilityLoreLine

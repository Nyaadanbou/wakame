package cc.mewcraft.wakame.display

import net.kyori.adventure.text.Component

internal class LoreLineComparator(
    private val loreIndexSupplier: LoreIndexLookup,
) : Comparator<LoreLine> {
    override fun compare(o1: LoreLine?, o2: LoreLine?): Int {
        requireNotNull(o1)
        requireNotNull(o2)
        val o1Idx = loreIndexSupplier.get(o1.key)
        val o2Idx = loreIndexSupplier.get(o2.key)
        return if (o1Idx < o2Idx) -1 else if (o1Idx > o2Idx) +1 else 0
    }
}

internal data class FixedLoreLineImpl(
    override val key: FullKey,
    override val lines: List<Component>,
) : FixedLoreLine

internal data class MetaLoreLineImpl(
    override val key: FullKey,
    override val lines: List<Component>,
) : MetaLoreLine

internal data class AttributeLoreLineImpl(
    override val key: FullKey,
    override val lines: List<Component>,
) : AttributeLoreLine

internal data class AbilityLoreLineImpl(
    override val key: FullKey,
    override val lines: List<Component>,
) : AbilityLoreLine

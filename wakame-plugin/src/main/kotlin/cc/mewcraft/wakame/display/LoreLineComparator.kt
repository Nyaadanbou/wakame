package cc.mewcraft.wakame.display

internal class LoreLineComparator(
    private val loreMetaLookup: LoreMetaLookup,
) : Comparator<LoreLine> {
    override fun compare(o1: LoreLine?, o2: LoreLine?): Int {
        requireNotNull(o1)
        requireNotNull(o2)
        val o1Idx = loreMetaLookup.get(o1.key)
        val o2Idx = loreMetaLookup.get(o2.key)
        return if (o1Idx < o2Idx) -1 else if (o1Idx > o2Idx) 1 else 0
    }
}
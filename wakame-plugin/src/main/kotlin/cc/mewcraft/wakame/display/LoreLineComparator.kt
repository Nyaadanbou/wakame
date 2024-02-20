package cc.mewcraft.wakame.display

internal class LoreLineComparator(
    private val loreIndexLookup: LoreIndexLookup,
) : Comparator<LoreLine> {
    override fun compare(o1: LoreLine?, o2: LoreLine?): Int {
        requireNotNull(o1)
        requireNotNull(o2)
        val o1Idx = loreIndexLookup.get(o1.key)
        val o2Idx = loreIndexLookup.get(o2.key)
        return if (o1Idx < o2Idx) -1 else if (o1Idx > o2Idx) 1 else 0
    }
}
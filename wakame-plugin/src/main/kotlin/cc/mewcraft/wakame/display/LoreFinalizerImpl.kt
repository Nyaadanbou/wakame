package cc.mewcraft.wakame.display

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet
import net.kyori.adventure.text.Component

internal class LoreFinalizerImpl(
    private val fixedLoreLines: Collection<LoreLine>,
    private val defaultLoreLines: Collection<LoreLine>,
    private val loreMetaLookup: LoreMetaLookup,
) : LoreFinalizer {

    private val lineComparator: Comparator<LoreLine> = Comparator { o1, o2 ->
        val o1Idx = loreMetaLookup.getIndex(o1.key)
        val o2Idx = loreMetaLookup.getIndex(o2.key)
        if (o1Idx < o2Idx) -1 else if (o1Idx > o2Idx) 1 else 0
    }

    override fun finalize(loreLines: Collection<LoreLine>): List<Component> {
        val holder = ObjectRBTreeSet(lineComparator)

        // add lore lines and sort
        holder += loreLines
        holder += fixedLoreLines
        holder += defaultLoreLines

        // if a lore line can't find a larger index
        // than it (w/ or w/o certain condition),
        // it will be removed
        val mainIterator = holder.iterator()
        while (mainIterator.hasNext()) {
            val curr = mainIterator.next()
            if (curr !is FixedLoreLine) {
                // curr 不是固定内容 - continue
                continue
            }

            val meta = loreMetaLookup.getMeta<FixedLoreMeta>(curr.key)
            val companionNamespace = meta.companionNamespace
                ?: continue // 对 companion namespace 没有要求，因此不考虑移除 - continue

            val subIterator = holder.iterator(curr)
            if (!subIterator.hasNext()) {
                mainIterator.remove() // 要求下面有内容，但下面没有 - remove curr and continue
                continue
            }

            // 跑到这里，说明 curr 下面一定有内容

            if (companionNamespace == "*") {
                // 只要求下面有任意内容，无论 namespace - continue
                continue
            }

            val higher = subIterator.next() // 比 curr 大的最小元素 (紧贴着 curr 的下面一行)
            if (higher.key.namespace() != companionNamespace) {
                mainIterator.remove() // higher 不符合 curr 对 namespace 的要求，因此移除 curr
            }
        }

        // unwrap the components
        return holder.flatMapTo(ObjectArrayList(holder.size * 2)) { it.lines }
    }
}
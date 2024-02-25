package cc.mewcraft.wakame.display

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet
import net.kyori.adventure.text.Component

internal class LoreFinalizerImpl(
    private val loreMetaLookup: LoreMetaLookup,
    private val fixedLoreLines: Collection<LoreLine>,
    private val defaultLoreLines: Collection<LoreLine>,
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

        // if a line can't find a larger index
        // than it, under certain conditions,
        // it will be removed from the lore

        var realLoreSize = 0 // used to preallocate array
        val iterator = holder.iterator()
        while (iterator.hasNext()) {
            val curr = iterator.next()
            realLoreSize += curr.lines.size
            if (curr !is FixedLoreLine) {
                // curr 不是固定内容 - continue
                continue
            }

            val loreMeta = loreMetaLookup.getMeta<FixedLoreMeta>(curr.key)
            val companionNamespace = loreMeta.companionNamespace
                ?: continue // curr 对 companion namespace 没有要求，因此不考虑移除 - continue

            if (!iterator.hasNext()) {
                iterator.remove() // curr 要求下面有内容，但下面没有 - remove curr and continue
                realLoreSize -= curr.lines.size
                continue
            }

            // 跑到这里说明 curr 下面一定有内容

            if (companionNamespace == "*") {
                // curr 只要求下面有任意内容，无论 namespace - continue
                continue
            }

            val higher = iterator.next() // 紧贴着 curr 的下面一行
            if (higher.key.namespace() != companionNamespace) {
                // higher 不符合 curr 对 namespace 的要求
                iterator.back(2) // 回到 curr
                iterator.remove() // 移除 curr
                realLoreSize -= curr.lines.size
            } else {
                // higher 符合 curr 对 namespace 的要求
                iterator.back(1) // 回到 curr
            }
        }

        // unwrap the components
        return holder.flatMapTo(ObjectArrayList(realLoreSize)) { it.lines }
    }
}
package cc.mewcraft.wakame.display

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet
import net.kyori.adventure.text.Component

internal class LoreFinalizerImpl(
    private val fixedLoreLines: Collection<FixedLoreLine>,
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

        // if a lore line can't find a larger index
        // than it (w/ or w/o certain condition),
        // it will be removed
        val iterator = holder.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next !is FixedLoreLine) {
                continue
            }

            val meta = loreMetaLookup.getMeta(next.key) as FixedLoreMeta
            val namespaceBelow = meta.companionNamespace ?: continue // 此固定行没有指定 namespace 的要求

            // 找到比当前大的所有行
            val higherLines = holder.tailSet(next)
            val higherLine = runCatching { higherLines.first() }.getOrNull()
            // 既然对下面的行有需求，那么如果找不到下面的行，那么这个行就会被移除
            if (higherLine == null) { // 如果找不到比它大的行, 那么它就是最后一行
                iterator.remove()
                continue
            }

            if (namespaceBelow == "*") {
                continue
            }

            // 如果找到的比它大的行的 namespace 不符合要求，那么它也会被移除
            if (higherLine.key.namespace() != namespaceBelow) {
                iterator.remove()
                continue
            }

            // 幸存者，不会被移除
        }

        // unwrap the components
        return holder.flatMapTo(ObjectArrayList(holder.size * 2)) { it.lines }
    }
}
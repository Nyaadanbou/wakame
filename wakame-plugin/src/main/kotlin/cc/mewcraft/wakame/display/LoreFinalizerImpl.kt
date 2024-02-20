package cc.mewcraft.wakame.display

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet
import net.kyori.adventure.text.Component

internal class LoreFinalizerImpl(
    private val comparator: LoreLineComparator,
    private val fixedLines: Collection<FixedLoreLine>,
    private val loreMetaLookup: Map<FullKey, LoreMeta>,
) : LoreFinalizer {
    override fun finalize(loreLines: Collection<LoreLine>): List<Component> {
        val holder = ObjectRBTreeSet(comparator)

        // add lore lines and sort
        holder += loreLines
        holder += fixedLines

        // if a lore line can't find a larger index
        // than it (w/ or w/o certain condition),
        // it will be removed
        val iterator = holder.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next !is FixedLoreLine) {
                continue
            }

            val meta = loreMetaLookup[next.key] as FixedLoreMeta
            val requiredNamespace = meta.requiredNamespace ?: continue // 此固定行没有指定 namespace 的要求

            // 找到比 it 大的所有行
            val loreLine = runCatching { holder.tailSet(next).first() }.getOrNull()
            // 既然对下面的行有需求，那么如果找不到下面的行，那么这个行就会被移除
            if (loreLine == null) { // 如果找不到比它大的行, 那么它就是最后一行
                iterator.remove()
                continue
            }

            if (requiredNamespace == "*") {
                continue
            }

            // 如果找到的比它大的行的 namespace 不符合要求，那么它也会被移除
            if (loreLine.key.namespace() != requiredNamespace) {
                iterator.remove()
                continue
            }
            // 幸存者，不会被移除
        }

        // unwrap the components
        val ret = holder.flatMapTo(ObjectArrayList(holder.size * 2)) { it.lines }
        return ret
    }
}
package cc.mewcraft.wakame.display

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet
import net.kyori.adventure.text.Component

internal class LoreLineFinalizerImpl(
    private val comparator: LoreLineComparator,
    private val fixedLines: List<FixedLoreLine>,
) : LoreLineFinalizer {
    override fun finalize(loreLines: Collection<LoreLine>): List<Component> {
        val holder = ObjectRBTreeSet(comparator)

        // add lore lines and sort
        holder += loreLines
        holder += fixedLines

        // unwrap the components
        val ret = holder.flatMapTo(ObjectArrayList(holder.size * 2)) { it.lines }
        return ret
    }
}
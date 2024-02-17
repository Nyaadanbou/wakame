package cc.mewcraft.wakame.display

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet
import net.kyori.adventure.text.Component

internal class LoreLineFinalizerImpl(
    private val loreLineComparator: LoreLineComparator,
    private val rendererConfiguration: RendererConfiguration,
) : LoreLineFinalizer {
    override fun finalize(loreLines: Collection<LoreLine>): List<Component> {
        val holder = ObjectRBTreeSet(loreLineComparator)

        // add lore lines (and sort simultaneously, thanks to RBTree)
        holder += rendererConfiguration.fixedLoreLines
        holder += loreLines

        // unwrap the components
        val ret = holder.flatMapTo(ObjectArrayList(holder.size * 2)) { it.line }
        return ret
    }
}
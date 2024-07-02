package cc.mewcraft.wakame.display

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet
import net.kyori.adventure.text.Component
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal fun Collection<LoreLine>.flatten(): List<Component> {
    return LoreLineFlatter.flatten(this)
}

internal object LoreLineFlatter : KoinComponent {

    /**
     * Flattens the [loreLines] so that it's converted to a list
     * of [Component], which then is ready to be put on an item
     * as the content of component `minecraft:lore`.
     *
     * The flattening process includes the following (but not least):
     * - sorting the lines by certain order
     * - inserting extra lines into the lore
     * - ...
     *
     * See the implementation for more details.
     *
     * @param loreLines a collection of [LoreLine] to be flattened
     * @return a sorted list of [Component]
     */
    fun flatten(loreLines: Collection<LoreLine>): List<Component> {
        val holder = ObjectRBTreeSet(lineComparator)
        holder += loreLines
        holder += rendererConfig.constantLoreLines
        // Adding duplicates to RBTree set makes no difference to the set.
        // Hence, we add in the default lines at the end so that
        // the default lines won't be added in
        // if these lines already exist.
        holder += rendererConfig.defaultLoreLines

        // if a line can't find a larger index
        // than it, under certain conditions,
        // it will be removed from the lore

        var realLoreSize = 0 // used to preallocate array
        val iterator = holder.iterator()
        while (iterator.hasNext()) {
            val curr = iterator.next()
            realLoreSize += curr.content.size
            if (!curr.isConstant) {
                continue
            }

            val loreMeta = loreMetaLookup.getMeta<ConstantLoreMeta>(curr.key)
            val companionNamespace = loreMeta.companionNamespace
            if (companionNamespace == null) {
                // curr 对 companion namespace 没有要求，因此不考虑移除 - continue
                continue
            }

            if (!iterator.hasNext()) {
                iterator.remove() // curr 要求下面有内容，但下面没有 - remove curr and continue
                realLoreSize -= curr.content.size
                continue
            }

            // 如果跑到这里，则说明 curr 下面一定有内容

            if (companionNamespace == "*") {
                // curr 只要求下面有任意内容，无论 namespace - continue
                continue
            }

            val higher = iterator.next() // 紧贴着 curr 的下面一行
            if (higher.key.namespace() != companionNamespace) {
                // higher 不符合 curr 对 namespace 的要求
                iterator.back(2) // 回到 curr
                iterator.remove() // 移除 curr
                realLoreSize -= curr.content.size
            } else {
                // higher 符合 curr 对 namespace 的要求
                iterator.back(1) // 回到 curr
            }
        }

        // unwrap the components
        return holder.flatMapTo(ObjectArrayList(realLoreSize)) { it.content }
    }

    private val rendererConfig: RendererConfig by inject()
    private val loreMetaLookup: LoreMetaLookup by inject()

    private val lineComparator: Comparator<LoreLine> = Comparator { o1, o2 ->
        val o1Idx = loreMetaLookup.getIndex(o1.key)
        val o2Idx = loreMetaLookup.getIndex(o2.key)
        if (o1Idx < o2Idx) -1 else if (o1Idx > o2Idx) 1 else 0
    }
}
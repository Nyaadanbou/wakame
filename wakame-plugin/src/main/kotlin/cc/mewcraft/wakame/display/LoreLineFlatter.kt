package cc.mewcraft.wakame.display

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet
import net.kyori.adventure.text.Component

internal class LoreLineFlatter(
    private val rendererConfig: RendererConfig,
    private val loreMetaLookup: LoreMetaLookup,
) {

    private val lineComparator: Comparator<LoreLine> = Comparator { o1, o2 ->
        val o1Idx = loreMetaLookup.getIndex(o1.key)
        val o2Idx = loreMetaLookup.getIndex(o2.key)
        if (o1Idx < o2Idx) -1 else if (o1Idx > o2Idx) 1 else 0
    }

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
        // 因为要排序, 所以使用 RBTreeSet
        val tree = ObjectRBTreeSet(lineComparator)

        // 首先添加传入的 lore lines
        tree.addAll(loreLines)
        // 然后添加 renderer config 中的固定内容
        tree.addAll(rendererConfig.constantLoreLines)
        // 最后添加 renderer config 中的默认内容
        // 这里利用了 RBTreeSet 的特性，即重复元素(重复键值)不会被添加.
        // 因此，如果这些内容已经存在，那么默认内容就不会被添加.
        // 相反, 如果不存在, 那么默认内容就会被添加.
        tree.addAll(rendererConfig.defaultLoreLines)

        // 接下来要清理固定内容. 整体策略:
        // 1. 遍历整个 tree
        // 2. 对于每一个 ConstantLoreLine:
        //  - 如果下面*没有*符合要求的 LoreLine, 那么移除当前遍历的 ConstantLoreLine
        //  - 如果下面*有*符合要求的 LoreLine, 那么保留当前遍历的 ConstantLoreLine

        var loreSize = 0 // 用于记录最终的 lore size
        val treeIterator = tree.iterator()
        while (treeIterator.hasNext()) {
            val curr: LoreLine = treeIterator.next()
            loreSize += curr.content.size
            if (!curr.isConstant) {
                continue
            }

            val meta: ConstantLoreMeta = loreMetaLookup.getMeta(curr.key)
            val companionNamespace = meta.companionNamespace
            if (companionNamespace == null) {
                // curr 对 companion namespace 没有任何要求，因此不考虑移除 - continue
                continue
            }

            if (!treeIterator.hasNext()) {
                treeIterator.remove() // curr 要求下面有内容 (`*` 或指定的 `namespace`)，但下面没有 - 移除 curr 然后 continue
                loreSize -= curr.content.size
                continue
            }

            // 如果跑到这里, 则说明 curr 下面一定有内容
            if (companionNamespace == "*") {
                // curr 只要求下面有任意内容，无论是什么 namespace - continue
                continue
            }

            val larger = treeIterator.next() // curr 的下面一行
            if (larger.key.namespace() != companionNamespace) {
                // larger 不符合 curr 对 namespace 的要求
                treeIterator.back(2) // 回到 curr
                treeIterator.remove() // 移除 curr
                loreSize -= curr.content.size
            } else {
                // larger 符合 curr 对 namespace 的要求
                treeIterator.back(1) // 回到 curr
            }
        }

        return tree.flatMapTo(ObjectArrayList(loreSize)) { it.content }
    }
}
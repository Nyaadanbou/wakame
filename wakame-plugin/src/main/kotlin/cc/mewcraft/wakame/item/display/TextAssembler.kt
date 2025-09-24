package cc.mewcraft.wakame.item.display

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.kyori.adventure.text.Component

internal class TextAssembler(
    private val rendererLayout: RendererLayout,
) {
    /**
     * Assembles the [data] so that it's converted to a list
     * of [Component], which is ready to be put on an ItemStack
     * as the content of data component `minecraft:lore`.
     *
     * The assembling process includes the following (but not least):
     * - sorting the lines by certain order
     * - inserting extra lines into the lore
     * - ...
     *
     * See the implementation for more details.
     *
     * @param data a collection of [IndexedText] to be assembled
     * @return a sorted list of [Component]
     */
    fun assemble(data: ReferenceOpenHashSet<IndexedText>): List<Component> {
        // TODO 理想的实现应该是在数据被完全渲染之前, 就应该知道要不要渲染,
        //  而不是渲染完毕之后, 再在这里进行一次过滤. 在这里过滤的好处就是
        //  实现起来简单, 但会渲染一些实际不需要的内容 (相当于白渲染了).

        // 首先过滤掉不存在于 layout 中的内容
        val setIterator = data.iterator()
        while (setIterator.hasNext()) {
            val curr = setIterator.next()
            if (rendererLayout.getOrdinal(curr.idx) == -1) {
                setIterator.remove()
            }
        }

        // 因为要排序, 并且更多的是插入操作, 所以使用 RBTreeSet
        val tree = ObjectRBTreeSet(indexComparator)

        // 首先添加传入的 indexed text
        tree.addAll(data)
        // 然后添加 layout 中的固定内容
        tree.addAll(rendererLayout.staticIndexedTextList)
        // 最后添加 layout 中的默认内容
        // 这里利用了集合的特性，即重复元素(重复键值)不会被添加.
        // 因此，如果这些内容已经存在，那么默认内容就不会被添加.
        // 相反, 如果不存在, 那么默认内容就会被添加.
        tree.addAll(rendererLayout.defaultIndexedTextList)

        // 接下来要清理固定内容. 整体策略:
        // 1. 遍历整个 tree
        // 2. 对于每一个 StaticIndexedText:
        //  - 如果下面*没有*符合要求的 IndexedText, 那么移除当前遍历的 StaticIndexedText
        //  - 如果下面*有*符合要求的 IndexedText, 那么保留当前遍历的 StaticIndexedText

        var loreSize = 0 // 用于记录最终的 lore size, 以一次性分配足够大的 array
        val treeIterator = tree.iterator()
        while (treeIterator.hasNext()) {
            val curr = treeIterator.next()
            loreSize += curr.text.size
            if (curr !is StaticIndexedText) {
                continue
            }

            val staticTextMeta = rendererLayout.getMetadata<StaticTextMeta>(curr.idx) ?: continue
            val companionNamespace = staticTextMeta.companionNamespace
            if (companionNamespace == null) {
                // curr 对 companion namespace 没有任何要求，因此不考虑移除 - continue
                continue
            }

            if (!treeIterator.hasNext()) {
                treeIterator.remove() // curr 要求下面有内容 (`*` 或指定的 `namespace`)，但下面没有 - 移除 curr 然后 continue
                continue
            }

            // 如果跑到这里, 则说明 curr 下面一定有内容
            if (companionNamespace == "*") {
                // curr 只要求下面有任意内容，无论是什么 namespace - continue
                continue
            }

            val larger = treeIterator.next() // curr 的下面一行
            if (larger.idx.namespace() != companionNamespace) {
                // larger 不符合 curr 对 namespace 的要求
                treeIterator.back(2) // 回到 curr
                treeIterator.remove() // 移除 curr
            } else {
                // larger 符合 curr 对 namespace 的要求
                treeIterator.back(1) // 回到 curr
            }
        }

        return tree.flatMapTo(ObjectArrayList(loreSize)) { it.text }
    }

    private val indexComparator: Comparator<IndexedText> = TextComparator()

    private inner class TextComparator : Comparator<IndexedText> {
        override fun compare(o1: IndexedText, o2: IndexedText): Int {
            val o1Idx = rendererLayout.getOrdinal(o1.idx)
            val o2Idx = rendererLayout.getOrdinal(o2.idx)
            return if (o1Idx < o2Idx) -1 else if (o1Idx > o2Idx) 1 else 0
        }
    }
}
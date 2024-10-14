package cc.mewcraft.wakame.display2

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet
import net.kyori.adventure.text.Component

internal class IndexedTextListTransformer(
    private val rendererLayout: RendererLayout,
) {
    private val textComparator: Comparator<IndexedText> = TextComparator()

    /**
     * Flattens the given [dataList] so that it's converted to a list
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
     * @param dataList a collection of [IndexedText] to be flattened
     * @return a sorted list of [Component]
     */
    fun flatten(dataList: ObjectArrayList<IndexedText>): List<Component> {
        // 因为要排序, 并且更多的是插入操作, 所以使用 RBTreeSet
        val tree = ObjectRBTreeSet(textComparator)

        // 首先添加传入的 lore lines
        tree.addAll(dataList)
        // 然后添加 renderer config 中的固定内容
        tree.addAll(rendererLayout.staticIndexedTexts)
        // 最后添加 renderer config 中的默认内容
        // 这里利用了集合的特性，即重复元素(重复键值)不会被添加.
        // 因此，如果这些内容已经存在，那么默认内容就不会被添加.
        // 相反, 如果不存在, 那么默认内容就会被添加.
        tree.addAll(rendererLayout.defaultIndexedTexts)

        // 接下来要清理固定内容. 整体策略:
        // 1. 遍历整个 tree
        // 2. 对于每一个 StaticIndexedText:
        //  - 如果下面*没有*符合要求的 IndexedText, 那么移除当前遍历的 StaticIndexedText
        //  - 如果下面*有*符合要求的 IndexedText, 那么保留当前遍历的 StaticIndexedText

        var loreSize = 0 // 用于记录最终的 lore size
        val iterator = tree.iterator()
        while (iterator.hasNext()) {
            val curr = iterator.next()
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

            if (!iterator.hasNext()) {
                iterator.remove() // curr 要求下面有内容 (`*` 或指定的 `namespace`)，但下面没有 - 移除 curr 然后 continue
                loreSize -= curr.text.size
                continue
            }

            // 如果跑到这里, 则说明 curr 下面一定有内容
            if (companionNamespace == "*") {
                // curr 只要求下面有任意内容，无论是什么 namespace - continue
                continue
            }

            val larger = iterator.next() // curr 的下面一行
            if (larger.idx.namespace() != companionNamespace) {
                // larger 不符合 curr 对 namespace 的要求
                iterator.back(2) // 回到 curr
                iterator.remove() // 移除 curr
                loreSize -= curr.text.size
            } else {
                // larger 符合 curr 对 namespace 的要求
                iterator.back(1) // 回到 curr
            }
        }

        return tree.flatMapTo(ObjectArrayList(loreSize)) { it.text }
    }

    private inner class TextComparator : Comparator<IndexedText> {
        override fun compare(o1: IndexedText, o2: IndexedText): Int {
            val o1Idx = rendererLayout.getOrdinal(o1.idx) ?: return -1
            val o2Idx = rendererLayout.getOrdinal(o2.idx) ?: return -1
            return if (o1Idx < o2Idx) -1 else if (o1Idx > o2Idx) 1 else 0
        }
    }
}
package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.GenericKeys

interface LineKeyFactory<T> {
    /**
     * 根据某种规则为特定的 [obj] 生成唯一的标识。
     *
     * **该函数会特别返回 [SKIP_DISPLAY] 用来表示 [obj] 不应该在物品上显示出来。**
     *
     * **也就是说，你应该对返回值做检查，以确保不渲染标记为 [SKIP_DISPLAY] 的内容。**
     *
     * 你可以用该函数所返回的 [FullKey] 配合 [LoreMetaLookup] 找到其在 Item Lore 中的顺序。
     *
     * @return [obj] 的唯一标识
     */
    fun get(obj: T): FullKey

    /**
     * This companion object holds the constant variables of the interface.
     */
    companion object Constants {
        /**
         * Signals that a display should be skipped.
         *
         * It is to be used to compare by reference.
         */
        val SKIP_DISPLAY: FullKey = GenericKeys.EMPTY
    }
}

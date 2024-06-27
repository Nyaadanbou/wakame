package cc.mewcraft.wakame.item.behavior

import cc.mewcraft.wakame.item.NekoItem

interface ItemBehaviorMap {
    /**
     * Checks whether this [NekoItem] has an [ItemBehavior] of the reified type [T], or a subclass of it.
     */
    fun <T : ItemBehavior> has(behaviorType: ItemBehavior): Boolean

    /**
     * Gets the first [ItemBehavior] that is an instance of [T], or null if there is none.
     */
    fun <T : ItemBehavior> get(behaviorType: ItemBehavior): T?

    /**
     * [ItemBehaviorMap] 的构造函数.
     */
    companion object {
        /**
         * 获取一个空的 [ItemBehaviorMap].
         */
        fun empty(): ItemBehaviorMap {
            return Empty
        }
    }

    private object Empty : ItemBehaviorMap {
        override fun <T : ItemBehavior> has(behaviorType: ItemBehavior): Boolean = false
        override fun <T : ItemBehavior> get(behaviorType: ItemBehavior): T? = null
    }
}

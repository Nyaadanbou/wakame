package cc.mewcraft.wakame.item.template

/**
 * 代表一个 [ItemTemplate] 的生成结果.
 *
 * @param T 物品组件的快照类型
 */
sealed interface ItemGenerationResult<out T> {

    /**
     * 生成的数据.
     */
    val value: T

    /**
     * 检查生成的结果是否为空. 如果为空，则结果不应该写入生成的物品.
     */
    fun isEmpty(): Boolean

    /**
     * 包含构建 [ItemGenerationResult] 实例的函数.
     */
    companion object {
        /**
         * 构建一个空的生成结果. 空的结果不应写入到生成的物品上.
         *
         * @param T 物品组件的快照类型
         */
        fun <T> empty(): ItemGenerationResult<T> {
            return Empty
        }

        /**
         * 构建一个非空的生成结果. 非空的结果将会写入到生成的物品上.
         *
         * @param T 物品组件的快照类型
         */
        fun <T> of(value: T): ItemGenerationResult<T> {
            return Thing(value)
        }
    }

    private data class Thing<T>(override val value: T) : ItemGenerationResult<T> {
        override fun isEmpty(): Boolean = false
    }

    private data object Empty : ItemGenerationResult<Nothing> {
        override val value: Nothing
            get() = throw UnsupportedOperationException("Empty result has no value")

        override fun isEmpty(): Boolean = true
    }
}
package cc.mewcraft.wakame.item.template

/**
 * 代表一个 [ItemTemplate] 的生成结果.
 *
 * @param T 物品组件的快照类型
 */
sealed interface GenerationResult<T> {

    /**
     * 检查生成的结果是否为空. 如果为空，则结果不应该写入最终生成的物品.
     */
    val isEmpty: Boolean

    /**
     * 包含构建 [GenerationResult] 实例的函数.
     */
    companion object {
        /**
         * 构建一个空的生成结果. 该结果**不会**应用到最终生成的物品上.
         *
         * @param T 物品组件的快照类型
         */
        fun <T> empty(): GenerationResult<T> {
            return Empty as GenerationResult<T>
        }

        /**
         * 构建一个非空的生成结果. 该结果**会**应用到最终生成的物品上.
         *
         * @param T 物品组件的快照类型
         */
        fun <T> of(value: T): GenerationResult<T> {
            return Thing(value)
        }
    }

    private data class Thing<T>(val value: T) : GenerationResult<T> {
        override val isEmpty: Boolean = false
    }

    private data object Empty : GenerationResult<Nothing> {
        override val isEmpty: Boolean = true
    }
}
package cc.mewcraft.wakame.item2.config.datagen

/**
 * 代表一个由 [ItemMetaEntry] 生成的结果.
 *
 * @param T 数据类型
 */
sealed interface ItemMetaResult<out T> {

    /**
     * 包含构建 [ItemMetaResult] 实例的函数.
     */
    companion object {
        /**
         * 构建一个空数据 [T].
         * 空数据不会写入到生成的物品堆叠上.
         *
         * @param T 数据类型
         */
        fun <T> empty(): ItemMetaResult<T> = Empty

        /**
         * 构建一个非空的数据 [T].
         * 非空的数据将写入到生成的物品堆叠上.
         *
         * @param T 数据类型
         */
        fun <T> of(value: T): ItemMetaResult<T> = Value(value)
    }

    /**
     * 生成的数据.
     */
    fun unwrap(): T

    /**
     * 检查生成的数据是否为空.
     * 如果为空，则不应该将数据写入到物品堆叠上.
     */
    fun isEmpty(): Boolean

    /**
     * 检查生成的数据是否存在.
     * 如果存在，则应该将数据写入到物品堆叠上.
     */
    fun isPresent(): Boolean

    // ------------
    // 内部实现
    // ------------

    private data class Value<T>(private val value: T) : ItemMetaResult<T> {
        override fun unwrap(): T = value
        override fun isEmpty(): Boolean = false
        override fun isPresent(): Boolean = true
    }

    private data object Empty : ItemMetaResult<Nothing> {
        override fun unwrap(): Nothing = throw UnsupportedOperationException("Empty result has no value")
        override fun isEmpty(): Boolean = true
        override fun isPresent(): Boolean = false
    }

}
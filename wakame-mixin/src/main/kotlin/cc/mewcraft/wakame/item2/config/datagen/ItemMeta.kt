package cc.mewcraft.wakame.item2.config.datagen

/**
 * 本类负责储存一个物品堆叠上所有可能的 "Item Data" 的[配置项][ItemMetaEntry].
 */
class ItemMetaContainer {

}

/**
 * 代表一个 "Item Data" 的配置项.
 */
interface ItemMetaEntry<T> {

    /**
     * 生成数据 [T].
     */
    fun generate(context: Context): ItemMetaResult<T>

}

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
         * 构建一个空数据.
         * 空数据不会写入到生成的物品堆叠上.
         *
         * @param T 数据类型
         */
        fun <T> empty(): ItemMetaResult<T> = Empty

        /**
         * 构建一个非空的数据.
         * 非空的数据将写入到生成的物品堆叠上.
         *
         * @param T 数据类型
         */
        fun <T> of(value: T): ItemMetaResult<T> = Simple(value)
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

    // ------------
    // 内部实现
    // ------------

    private data class Simple<T>(val value: T) : ItemMetaResult<T> {
        override fun unwrap(): T = value
        override fun isEmpty(): Boolean = false
    }

    private data object Empty : ItemMetaResult<Nothing> {
        override fun unwrap(): Nothing = throw UnsupportedOperationException("Empty result has no value")
        override fun isEmpty(): Boolean = true
    }

}

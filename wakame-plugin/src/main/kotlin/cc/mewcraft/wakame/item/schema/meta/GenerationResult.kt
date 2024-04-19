package cc.mewcraft.wakame.item.schema.meta

/**
 * The constructor function of [GenerationResult] to create a non-empty result.
 *
 * @param T the value type
 * @param value the value
 * @return a new result
 */
fun <T> GenerationResult(value: T): GenerationResult<T> {
    return GenerationResult.Thing(value)
}

/**
 * Represents the result of a [meta][SchemaItemMeta] generation.
 *
 * @param T the type of the value
 */
sealed interface GenerationResult<T> {

    /**
     * 检查生成的结果是否为空。如果为空，则结果不应该写入物品。
     */
    val isEmpty: Boolean

    /**
     * Represents a generated data which should be written to the ItemStack.
     */
    data class Thing<T>(val value: T) : GenerationResult<T> {
        override val isEmpty: Boolean = false
    }

    /**
     * Represents a result indicating that no data should be written to the ItemStack.
     *
     * **Do not directly use it as a return value.** Instead, use the
     * [GenerationResult.empty] function to construct an empty result.
     *
     * The use site of this singleton should follow the contract of this class.
     * That is, the use site code should not write the data to the ItemStack
     * if the [GenerationResult] is the [Empty] instance.
     */
    private data object Empty : GenerationResult<Nothing> {
        override val isEmpty: Boolean = true
    }

    /**
     * Companion object for [GenerationResult] class that contains its constructor function [empty].
     */
    companion object {
        /**
         * Constructs an [empty][Empty] result.
         *
         * Check the kdoc of [Empty] for the usage of empty result.
         */
        fun <T> empty(): GenerationResult<T> {
            @Suppress("UNCHECKED_CAST")
            return Empty as GenerationResult<T>
        }
    }
}

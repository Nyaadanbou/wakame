package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext

/**
 * 代表一个物品的元数据模板。
 *
 * **实现必须声明一个 `companion object` 并且实现 [net.kyori.adventure.key.Keyed].**
 *
 * @param T 模板最终产生的数据类型
 */
sealed interface SchemeItemMeta<T : Any?> {

    companion object ResultUtil {
        internal fun <T : Any?> T.toMetaResult(): Result.NonEmptyResult<T> = Result.NonEmptyResult(this)

        internal fun <T : Any?> nonGenerate(): Result<T> = Result.NonGenerateResult
    }

    /**
     * Generate a value [T] from this scheme.
     *
     * **A [Result.NonGenerateResult] value indicates that nothing is generated, which is used
     * to instruct that the item meta should not be written to the item.**
     *
     * The implementation must populate the [context] with relevant
     * information about the generation result.
     *
     * @param context the generation context
     * @return the generated [T]
     */
    fun generate(context: SchemeGenerationContext): Result<T>

    /**
     * Represents the result of a meta-generation.
     *
     * @param T the type of the value
     */
    sealed interface Result<out T : Any?> {
        /**
         * Represents a non-generate result.
         */
        data object NonGenerateResult : Result<Nothing>

        /**
         * Represents a non-empty result.
         */
        data class NonEmptyResult<T : Any?>(val value: T) : Result<T>
    }
}

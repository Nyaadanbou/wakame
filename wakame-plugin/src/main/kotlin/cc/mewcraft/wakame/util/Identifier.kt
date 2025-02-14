package cc.mewcraft.wakame.util

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key

const val KOISH_NAMESPACE = "koish"

// adventure 已经提供比较完备的实现,
// 直接用 typealias 也许更具有维护性
/**
 * 代表一个特定资源的位置 (命名空间:路径).
 */
typealias Identifier = Key

typealias Identifiers = IdentifierExt

/**
 * 包含 [Identifier] 的静态函数.
 */
object IdentifierExt {

    @JvmField
    val CODEC: Codec<Identifier> = Codec.STRING.comapFlatMap(Identifiers::validate, Identifier::toString)

    /**
     * 从形如 `player`, `foo:player` 的字符串创建一个 [Identifier].
     * 如果字符串不包含命名空间与路径的分隔符 [Key.DEFAULT_SEPARATOR]
     * 则自动将其命名空间设置为 [KOISH_NAMESPACE].
     *
     * @param id the string
     * @return the identifier
     */
    fun of(id: String): Identifier {
        val index = id.indexOf(Key.DEFAULT_SEPARATOR);
        val namespace = if (index >= 1) id.substring(0, index) else KOISH_NAMESPACE
        val path = if (index >= 0) id.substring(index + 1) else id
        return Identifier.key(namespace, path)
    }

    /**
     * Creates an identifier from namespace and path.
     * When passed an invalid value, this throws [InvalidKeyException].
     *
     * @param namespace the namespace
     * @param path the path
     * @return the identifier
     * @throws InvalidKeyException
     */
    fun of(namespace: String, path: String): Identifier {
        return Identifier.key(namespace, path)
    }

    /**
     * Creates an identifier in the [KOISH_NAMESPACE] namespace.
     *
     * @param path the path
     * @return the identifier
     */
    fun ofKoish(path: String): Identifier {
        return Identifier.key(KOISH_NAMESPACE, path)
    }

    /**
     * Creates an identifier from a string in `<namespace>:<path>` format.
     * If the colon is missing, the created identifier has the namespace [KOISH_NAMESPACE] and the argument is used as the path.
     * When passed an invalid value, this returns null.
     *
     * @param id the string
     * @return the identifier
     */
    fun tryParse(id: String): Identifier? {
        return runCatching { of(id) }.getOrNull()
    }

    /**
     * Creates an identifier from namespace and path.
     * When passed an invalid value, this returns null.
     *
     * @param namespace the namespace
     * @param path the path
     * @return the identifier
     */
    fun tryParse(namespace: String, path: String): Identifier? {
        return runCatching { of(namespace, path) }.getOrNull()
    }

    /**
     * Creates an identifier.
     * This will parse string as an identifier, using character as a separator between the namespace and the path.
     * The namespace is optional. If you do not provide one (for example, if you provide player or character + "player" as the string)
     * then [KOISH_NAMESPACE] will be used as a namespace and [id] will be used as the path,
     * removing the provided separator character if necessary.
     *
     * @param id the string
     * @param delimiter the character to split the namespace and path
     * @return the identifier
     * @throws InvalidKeyException if the string [id] is not a valid identifier
     */
    fun splitOn(id: String, delimiter: Char): Identifier {
        return Identifier.key(id, delimiter)
    }

    /**
     * Creates an identifier from a string with specific [delimiter].
     *
     * @param id the string
     * @param delimiter the character to split the namespace and path
     * @return the identifier
     */
    fun trySplitOn(id: String, delimiter: Char): Identifier? {
        return runCatching { Identifier.key(id, delimiter) }.getOrNull()
    }

    /**
     * 用于方便创建 [Codec].
     */
    fun validate(string: String): DataResult<Identifier> = try {
        DataResult.success(of(string))
    } catch (e: Exception) {
        DataResult.error { "Not a valid resource location: '$string' ${e.message}" }
    }

}

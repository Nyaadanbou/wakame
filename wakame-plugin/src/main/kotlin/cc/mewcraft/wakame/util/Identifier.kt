package cc.mewcraft.wakame.util

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.kyori.adventure.key.Key

const val KOISH_NAMESPACE = "koish"

// adventure 已经提供比较完备的实现,
// 直接用 typealias 也许更具有维护性
/**
 * 代表一个特定资源的位置 (命名空间:路径).
 */
typealias Identifier = Key

/**
 * 包含 [Identifier] 的静态函数, 用于统一创建实例的方式.
 */
object Identifiers {
    @JvmField
    val CODEC: Codec<Identifier> = Codec.STRING.comapFlatMap(Identifiers::validate, Identifier::toString)

    /**
     * 从形如 `player`, `foo:player` 的字符串创建一个 [Identifier].
     * 如果字符串不包含命名空间与路径的分隔符 [Key.DEFAULT_SEPARATOR]
     * 则自动将其命名空间设置为 [KOISH_NAMESPACE].
     *
     * @param string 用于创建实例的字符串
     * @return 创建的实例
     */
    fun of(string: String): Identifier {
        val index = string.indexOf(Key.DEFAULT_SEPARATOR);
        val namespace = if (index >= 1) string.substring(0, index) else KOISH_NAMESPACE
        val value = if (index >= 0) string.substring(index + 1) else string
        return Identifier.key(namespace, value)
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

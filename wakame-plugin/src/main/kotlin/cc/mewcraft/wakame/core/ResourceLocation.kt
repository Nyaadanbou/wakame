package cc.mewcraft.wakame.core

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.kyori.adventure.key.Key

const val KOISH_NAMESPACE = "koish"

// adventure 已经提供比较完备的实现,
// 直接用 typealias 也许更具有维护性
/**
 * 代表一个特定资源的位置 (命名空间:路径).
 */
typealias ResourceLocation = Key

/**
 * 包含 [ResourceLocation] 的静态函数, 用于统一创建实例的方式.
 */
object ResourceLocations {
    @JvmField
    val CODEC: Codec<ResourceLocation> = Codec.STRING.comapFlatMap(ResourceLocations::read, ResourceLocation::toString)

    /**
     * 从形如 `player`, `koish:player` 的字符串创建一个 [ResourceLocation].
     *
     * 如果该字符串不包含命名空间, 则使用默认的命名空间 [KOISH_NAMESPACE].
     *
     * @param string 用于创建实例的字符串
     * @return 创建的实例
     */
    fun withKoishNamespace(string: String): ResourceLocation {
        val index = string.indexOf(Key.DEFAULT_SEPARATOR);
        val namespace = if (index >= 1) string.substring(0, index) else KOISH_NAMESPACE
        val value = if (index >= 0) string.substring(index + 1) else string
        return ResourceLocation.key(namespace, value)
    }

    /**
     * 用于方便创建 [Codec].
     */
    fun read(string: String): DataResult<ResourceLocation> = try {
        DataResult.success(withKoishNamespace(string))
    } catch (e: Exception) {
        DataResult.error { "Not a valid resource location: '$string' ${e.message}" }
    }
}

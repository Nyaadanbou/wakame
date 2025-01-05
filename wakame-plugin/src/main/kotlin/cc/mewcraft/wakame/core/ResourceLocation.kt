package cc.mewcraft.wakame.core

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.kyori.adventure.key.Key

const val DEFAULT_NAMESPACE = "koish"

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

    fun withDefaultNamespace(path: String): ResourceLocation = ResourceLocation.key(DEFAULT_NAMESPACE, path)

    fun read(id: String): DataResult<ResourceLocation> = try {
        DataResult.success(ResourceLocation.key(id))
    } catch (e: Exception) {
        DataResult.error { "Not a valid resource location: $id ${e.message}" }
    }
}

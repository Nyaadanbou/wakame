package cc.mewcraft.wakame.core

import net.kyori.adventure.key.Key

const val DEFAULT_NAMESPACE = "koish"

// adventure 已经提供比较完备的实现,
// 直接用 typealias 也许更具有维护性
/**
 * 代表一个特定资源的位置 (命名空间:路径).
 */
typealias ResourceLocation = Key

/**
 * 包含 [ResourceLocation] 的静态函数, 用于统一创建实例的过程.
 */
object ResourceLocations {
    fun defaultNamespace(path: String): ResourceLocation = ResourceLocation.key(DEFAULT_NAMESPACE, path)
}

// data class ResourceLocation(
//     val namespace: String,
//     val path: String,
// ) {
//     companion object {
//         fun defaultNamespace(path: String): ResourceLocation = ResourceLocation("koish", path)
//     }
//
//     override fun toString(): String {
//         return "$namespace:$path"
//     }
// }
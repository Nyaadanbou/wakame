package cc.mewcraft.wakame.display2


// 开发日记 2024/9/18
// 因为每个 DataComponentRenderer 需要的
// RendererFormats.Format 都不太一样,
// 所以写成泛型.

/**
 * 用于聚合 [DataComponentRenderer] 所需要的数据.
 *
 * @param T 被渲染的数据类型
 * @param F 渲染格式的类型
 */
class RenderingBundle<T, F : RendererFormat>(
    val rendererFormat: F,
    val dataRenderer: DataComponentRenderer<T>
) {

}

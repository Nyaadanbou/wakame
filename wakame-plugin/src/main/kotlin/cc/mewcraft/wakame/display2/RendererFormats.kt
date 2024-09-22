package cc.mewcraft.wakame.display2

import net.kyori.adventure.key.Key

// 开发日记 2024/9/17
// 读取配置文件时, 应该先读 formats.yml, 再读 layout.yml

/**
 * 封装了一个 [ItemRenderer] 包含的所有渲染格式.
 */
interface RendererFormats {
    /**
     * 获取指定的 [RendererFormat].
     */
    fun <T : RendererFormat> get(id: String): T?
}

/**
 * 代表一种内容的渲染格式.
 *
 * 这只是一个顶级接口, 实现需要根据具体的渲染内容, 增加属性和函数.
 */
interface RendererFormat {
    /**
     * 该内容的索引, 由 [Key] 来表示.
     *
     * 该索引的构成为:
     * - [Key.namespace] 为该内容的索引的命名空间
     *   *由用户自行定义*
     * - [Key.value] 为该内容的唯一标识
     *   *编译时已经确定*
     */
    val index: DerivedTooltipIndex
}
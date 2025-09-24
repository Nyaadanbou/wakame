package cc.mewcraft.wakame.item.display

import xyz.xenondevs.commons.provider.Provider

// 开发日记 2024/9/17
// 读取配置文件时, 应该先读 formats.yml, 再读 layout.yml

/**
 * 封装了一个 [ItemRenderer] 包含的所有渲染格式.
 */
interface RendererFormatRegistry {
    /**
     * 获取指定的 [RendererFormat].
     */
    fun <T : RendererFormat> getRendererFormat(id: String): Provider<T>
}


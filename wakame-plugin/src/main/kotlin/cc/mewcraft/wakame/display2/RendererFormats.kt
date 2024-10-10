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
     * 该内容的命名空间, 由用户提供.
     */
    val namespace: String

    /**
     * 代表一个索引在编译时已经确定的 [RendererFormat].
     */
    interface Simple : RendererFormat {
        val id: String
        val index: Key // namespace + id
        fun createIndex(): Key = Key.key(namespace, id)
    }

    /**
     * 代表一个索引会在运行时动态生成的 [RendererFormat].
     */
    interface Dynamic<T> : RendererFormat {
        fun computeIndex(source: T): Key
    }
}
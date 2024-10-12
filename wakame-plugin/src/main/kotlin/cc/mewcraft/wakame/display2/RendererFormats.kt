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
 *
 * ### 实现建议
 *
 * - 使用 `data class` 以便使用 ObjectMapper 来实现快速序列化
 * - 所有属性(property)应该提供默认值, 以便应对配置文件缺省的情况
 */
interface RendererFormat {
    /**
     * 该内容的命名空间, 由用户提供.
     */
    val namespace: String

    // TODO display2 设计上这里应该包含一个 TextMetaFactory 的实例, 用于生成 TextMeta.
    //  这是因为创建 TextMeta 要求已知 namespace, 而 namespace
    //  只有在 RendererFormat 创建好之后才能知道. 因此获取 TextMetaFactory
    //  的职责由 RendererFormat 来承担就比较合适了.

    // fun createTextMetaFactory(): TextMetaFactory

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
    interface Dynamic<in T> : RendererFormat {
        fun computeIndex(data: T): Key
    }

    companion object Shared {
        /**
         * 当一个 [RendererFormat] 的配置缺省时使用的字符串.
         */
        const val FIXME = "fixme"

        /**
         * 获取一个空的 [RendererFormat].
         */
        fun empty(): RendererFormat = EMPTY

        private val EMPTY = object : Simple, Dynamic<Nothing> {
            override val namespace: String = "internal"
            override val id: String = "empty"
            override val index: Key = createIndex()
            override fun computeIndex(data: Nothing): Key = index
        }
    }
}
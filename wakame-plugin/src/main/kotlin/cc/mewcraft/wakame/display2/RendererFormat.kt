package cc.mewcraft.wakame.display2

import net.kyori.adventure.key.Key

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

    // 开发日记 2024/10/13 小米
    //  设计上这里应该包含一个 TextMetaFactory 的实例, 用于生成 TextMeta.
    //  这是因为创建 TextMeta 要求已知 namespace, 而 namespace
    //  只有在 RendererFormat 创建好之后才能知道. 因此获取 TextMetaFactory
    //  的职责由 RendererFormat 来承担就比较合适了.

    // 开发日记 2024/12/12 小米
    //  为了更好的将代码模块化, 将 TextMetaFactory 的 create 和 test 分离开来.

    /**
     * 该渲染格式对应的 [TextMetaFactory] 实例.
     */
    val textMetaFactory: TextMetaFactory

    /**
     * 该渲染格式对应的 [TextMetaFactoryPredicate] 实例.
     */
    val textMetaPredicate: TextMetaFactoryPredicate

    /**
     * 代表一个索引在编译时已经确定的 [RendererFormat].
     */
    interface Simple : RendererFormat {
        val id: String
        val index: DerivedIndex // namespace + id
        fun createIndex(): DerivedIndex = Key.key(namespace, id)
    }

    /**
     * 代表一个索引会在运行时动态生成的 [RendererFormat].
     *
     * 实现上, 动态生成的索引必须是对应 [TextMeta.derivedIndexes]
     * 里面的子集, 否则会出现无法找到对应索引的序数和元数据等等问题.
     */
    interface Dynamic<in T> : RendererFormat {
        fun computeIndex(data: T): DerivedIndex
    }
}
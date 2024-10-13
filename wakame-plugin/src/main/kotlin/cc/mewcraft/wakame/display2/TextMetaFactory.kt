package cc.mewcraft.wakame.display2

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.slf4j.Logger

interface TextMetaFactory {
    /**
     * [IndexedText] 的命名空间, 由用户提供.
     */
    val namespace: String

    /**
     * 检查该工厂是否可以从 [sourceIndex] 创建一个 [SimpleTextMeta].
     *
     * @param sourceIndex the source index (without any derivation)
     * @return `true` if this factory can handle the [sourceIndex]
     */
    fun test(sourceIndex: SourceIndex): Boolean

    /**
     * 创建一个新的 [SimpleTextMeta], 其命名空间应该与 [namespace] 一致.
     *
     * @param sourceIndex the line identity (without any derivation)
     * @param sourceOrdinal the source ordinal
     * @param defaultText the default text (can be `null`)
     * @return a new instance of [SimpleTextMeta]
     *
     * @throws IllegalArgumentException if the [sourceIndex] is unrecognized
     */
    fun create(sourceIndex: SourceIndex, sourceOrdinal: SourceOrdinal, defaultText: List<Component>?): SimpleTextMeta
}

/**
 * 该注册表存放了所有已知的 [TextMetaFactory] 实例.
 */
class TextMetaFactoryRegistry : KoinComponent {
    private val logger = get<Logger>()

    // namespace -> factory
    private val factories: MutableSet<TextMetaFactory> = LinkedHashSet()

    /**
     * 注册一个 [TextMetaFactory]. 多次注册将重复添加.
     */
    fun registerFactory(factory: TextMetaFactory) {
        factories += factory
        logger.info("Registered TextMetaFactory: {}", factory)
    }

    /**
     * 获取一个适用于 [identity] 的 [TextMetaFactory].
     *
     * @param identity 配置文件中的原始字符串, 未经任何修改
     * @return 返回一个合适的 [TextMetaFactory]
     */
    fun getApplicableFactory(identity: Key): TextMetaFactory? {
        return factories.firstOrNull { factory -> factory.test(identity) }
    }

    /**
     * 返回所有已知的 [TextMetaFactory].
     */
    fun entries(): Set<TextMetaFactory> {
        // namespace -> factory
        return factories
    }

    /**
     * 清空所有已知的 [TextMetaFactory].
     */
    fun clear() {
        factories.clear()
    }
}
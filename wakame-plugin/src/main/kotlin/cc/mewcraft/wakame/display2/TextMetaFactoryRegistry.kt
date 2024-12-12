package cc.mewcraft.wakame.display2

import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.slf4j.Logger

/**
 * 该注册表存放了所有已知的 [TextMetaFactory] 实例.
 */
class TextMetaFactoryRegistry : KoinComponent {
    private val logger = get<Logger>()

    // namespace -> factory
    private val factories: MutableSet<Entry> = LinkedHashSet()

    /**
     * 注册一个 [TextMetaFactory]. 多次注册将重复添加.
     */
    fun registerFactory(
        factory: TextMetaFactory,
        predicate: TextMetaFactoryPredicate,
    ) {
        factories += Entry(factory, predicate)
        // logger.info("Registered TextMetaFactory: {}", factory)
    }

    /**
     * 获取一个适用于 [identity] 的 [TextMetaFactory].
     *
     * @param identity 配置文件中的原始字符串, 未经任何修改
     * @return 返回一个合适的 [TextMetaFactory]
     */
    fun getApplicableFactory(identity: SourceIndex): TextMetaFactory? {
        return factories.firstOrNull { entry -> entry.predicate.test(identity) }?.factory
    }

    /**
     * 返回所有已知的 [TextMetaFactory].
     */
    fun getKnownFactories(): Set<TextMetaFactory> {
        return factories.map(Entry::factory).toSet() // namespace -> factory
    }

    /**
     * 清空所有已知的 [TextMetaFactory].
     */
    fun reset() {
        factories.clear()
    }

    private class Entry(
        val factory: TextMetaFactory,
        val predicate: TextMetaFactoryPredicate,
    )
}
package cc.mewcraft.wakame.item.display

/**
 * 该注册表存放了所有已知的 [TextMetaFactory] 实例.
 */
class TextMetaFactoryRegistry {

    // namespace -> factory
    private val factorySet: MutableSet<InternEntry> = LinkedHashSet()

    private class InternEntry(val factory: TextMetaFactory, val predicate: TextMetaFactoryPredicate)

    /**
     * 注册一个 [TextMetaFactory]. 多次注册将重复添加.
     */
    fun registerFactory(
        factory: TextMetaFactory,
        predicate: TextMetaFactoryPredicate,
    ) {
        factorySet += InternEntry(factory, predicate)
        // LOGGER.info("Registered TextMetaFactory: {}", factory)
    }

    /**
     * 获取一个适用于 [identity] 的 [TextMetaFactory].
     *
     * @param identity 配置文件中的原始字符串, 未经任何修改
     * @return 返回一个合适的 [TextMetaFactory]
     */
    fun getMatchedFactory(identity: SourceIndex): TextMetaFactory? {
        return factorySet.firstOrNull { entry -> entry.predicate.test(identity) }?.factory
    }

    /**
     * 返回所有已知的 [TextMetaFactory].
     */
    fun getKnownFactory(): Set<TextMetaFactory> {
        return factorySet.map(InternEntry::factory).toSet() // namespace -> factory
    }

    /**
     * 清空所有已知的 [TextMetaFactory].
     */
    fun reset() {
        factorySet.clear()
    }

}
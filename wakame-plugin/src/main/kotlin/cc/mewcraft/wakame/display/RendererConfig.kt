package cc.mewcraft.wakame.display

import net.kyori.adventure.text.Component
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

internal interface RendererConfig {
    /**
     * 所有的 [RawTooltipKey], 用于判断内容是否需要渲染.
     *
     * 如果一个 [RawTooltipKey] 不在该集合里, 则说明不应该渲染.
     */
    val rawTooltipKeys: Set<RawTooltipKey>

    /**
     * 用于查询指定内容的 [LoreMeta].
     */
    val loreMetaMap: Map<TooltipKey, LoreMeta>

    /**
     * 用于查询指定内容的 [TooltipIndex].
     */
    val loreIndexMap: Map<TooltipKey, TooltipIndex>

    /**
     * 始终要渲染的内容. 这些内容的文本在物品中始终不变.
     */
    val constantLoreLines: Collection<LoreLine>

    /**
     * 带有默认值的内容. 当源数据不存在时将采用这里的默认值.
     */
    val defaultLoreLines: Collection<LoreLine>

    /**
     * 从配置文件重新加载数据.
     */
    fun loadLayout()
}

/**
 * A creator of a [DynamicLoreMeta].
 */
internal interface DynamicLoreMetaCreator {
    /**
     * The namespace of this creator. Used to identify different creators.
     */
    val namespace: String

    /**
     * Checks whether this creator is capable of creating a [LoreMeta]
     * from the given [rawLine].
     *
     * @param rawLine the raw line text
     * @return `true` if the [rawLine] is "legal" for this creator
     */
    fun test(rawLine: String): Boolean

    /**
     * Creates the [DynamicLoreMeta].
     *
     * @param rawTooltipIndex the raw tooltip index
     * @param rawLine the raw line text
     * @param default the default text if there is any
     * @return the created [LoreMeta]
     *
     * @throws IllegalArgumentException if the raw line text is unrecognized
     */
    fun create(rawTooltipIndex: RawTooltipIndex, rawLine: String, default: List<Component>?): DynamicLoreMeta
}

/**
 * 注册表之 [DynamicLoreMetaCreator].
 *
 * 使用该对象来注册一个 [DynamicLoreMetaCreator].
 */
internal class DynamicLoreMetaCreators : KoinComponent {
    private val logger by inject<Logger>()

    /**
     * 所有的 [DynamicLoreMetaCreator].
     *
     * K - 原始的
     * V - [DynamicLoreMetaCreator]
     */
    private val creators: HashMap<String, DynamicLoreMetaCreator> = HashMap()

    /**
     * 获取所有的 [DynamicLoreMetaCreator].
     */
    fun entries(): Set<Map.Entry<String, DynamicLoreMetaCreator>> {
        return this.creators.entries
    }

    /**
     * 注册一个 [DynamicLoreMetaCreator].
     *
     * 会覆盖 [DynamicLoreMetaCreator.namespace] 相同的实例.
     */
    fun register(creator: DynamicLoreMetaCreator) {
        this.creators += creator.namespace to creator
        logger.info("Registered DynamicLoreMetaCreator: {}", creator.namespace)
    }

    /**
     * 获取一个适用于 [rawLine] 的 [DynamicLoreMetaCreator].
     *
     * @param rawLine 配置文件中的原始字符串, 未经任何修改
     * @return 返回一个合适的 [DynamicLoreMetaCreator]
     */
    fun getApplicableCreator(rawLine: String): DynamicLoreMetaCreator? {
        return this.creators.values.firstOrNull { creator -> creator.test(rawLine) }
    }
}
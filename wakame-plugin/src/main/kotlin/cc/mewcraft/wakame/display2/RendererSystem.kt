package cc.mewcraft.wakame.display2

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.display.*
import cc.mewcraft.wakame.display.LoreLineFlatter
import cc.mewcraft.wakame.display.LoreMetaLookup
import cc.mewcraft.wakame.display.RendererConfig
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttributeLoreMetaCreator
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttributeTooltipKeyProvider
import java.nio.file.Path

internal interface RendererSystem<in T> {
    companion object {
        /**
         * 根据指定配置文件路径创建一个 [RendererSystem].
         *
         * @param path 配置文件路径.
         */
        fun <T> create(path: Path, rendererProvider: (LoreLineFlatter) -> ItemRenderer<T>): RendererSystem<T> {
            return SimpleRendererSystem(path, rendererProvider)
        }
    }

    /**
     * 此 [RendererSystem] 的配置.
     */
    val config: RendererConfig

    /**
     * 此 [RendererSystem] 的 [ItemRenderer].
     */
    val renderer: ItemRenderer<T>

    /**
     * 根据 [config] 的配置, 返回一个 [LoreMetaLookup].
     */
    val loreMetaLookup: LoreMetaLookup

    /**
     * 根据 [config] 的配置, 返回一个 [LoreLineFlatter].
     */
    val loreLineFlatter: LoreLineFlatter

    /**
     * 根据 [config] 的配置, 返回一个 [CoreAttributeLoreMetaCreator].
     */
    val coreAttributeLoreMetaCreator: CoreAttributeLoreMetaCreator

    /**
     * 根据 [config] 的配置, 返回一个 [CoreAttributeTooltipKeyProvider].
     */
    val coreAttributeTooltipKeyProvider: CoreAttributeTooltipKeyProvider

    /**
     * 渲染一个物品 [T].
     */
    fun render(item: T) {
        renderer.render(item)
    }
}

private class SimpleRendererSystem<T>(
    path: Path,
    rendererProvider: (LoreLineFlatter) -> ItemRenderer<T>
) : RendererSystem<T> {
    private val provider = Configs.YAML[path.toString()]

    override val config: RendererConfig = RendererConfigImpl(provider)
    override val loreMetaLookup: LoreMetaLookup = LoreMetaLookup.create(config.loreIndexLookup, config.loreMetaLookup)
    override val loreLineFlatter: LoreLineFlatter = LoreLineFlatter(config, loreMetaLookup)
    override val renderer: ItemRenderer<T> = rendererProvider(loreLineFlatter)
    override val coreAttributeLoreMetaCreator: CoreAttributeLoreMetaCreator = CoreAttributeLoreMetaCreator(provider)
    override val coreAttributeTooltipKeyProvider: CoreAttributeTooltipKeyProvider = CoreAttributeTooltipKeyProvider(config)
}
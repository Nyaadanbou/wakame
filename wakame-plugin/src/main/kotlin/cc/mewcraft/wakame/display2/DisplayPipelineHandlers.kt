package cc.mewcraft.wakame.display2

import cc.mewcraft.wakame.core.PipelineHandler
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.LoreLineFlatter
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.NekoStack
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.text.Component

internal object DisplayPipelineHandlers {
    val EXTRACT_LORE_LINES: (RendererSystemName) -> PipelineHandler.Stateless<NekoStack, Collection<LoreLine>> = ::ExtractLoreLines
    val MAP_TO_COMPONENTS: (LoreLineFlatter) -> PipelineHandler.Stateless<Collection<LoreLine>, List<Component>> = ::MapToComponents
}

/**
 * 提取物品上所有的 [LoreLine].
 */
private class ExtractLoreLines(
    private val systemName: RendererSystemName
) : PipelineHandler.Stateless<NekoStack, Collection<LoreLine>> {
    override fun process(input: NekoStack): Collection<LoreLine> {
        val components = input.components
        val ret = ObjectArrayList<LoreLine>(components.fuzzySize())
        for ((_, data) in components) {

            when (data) {
                is TooltipProvider.Single -> {
                    val line = data.provideTooltipLore(systemName)
                    if (line.shouldShow) {
                        ret.add(line)
                    }
                }

                is TooltipProvider.Cluster -> {
                    val collection = data.provideTooltipLore(systemName)
                    if (collection.isEmpty()) {
                        continue
                    }
                    for (line in collection) {
                        if (line.shouldShow) {
                            ret.add(line)
                        }
                    }
                }
            }
        }

        return ret
    }
}

/**
 * 将 [LoreLine] 映射为 [Component].
 */
private class MapToComponents(
    private val loreLineFlatter: LoreLineFlatter
) : PipelineHandler.Stateless<Collection<LoreLine>, List<Component>> {
    override fun process(input: Collection<LoreLine>): List<Component> {
        return loreLineFlatter.flatten(input) // flatten the lore lines
    }
}
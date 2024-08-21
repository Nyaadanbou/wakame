package cc.mewcraft.wakame.display2

import cc.mewcraft.wakame.core.PipelineHandler
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.NekoStack
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.text.Component


/**
 * 负责移除物品上的萌芽 NBT.
 */
private class EraseNekoStackData(
    val inPlace: Boolean
) : PipelineHandler.Stateless<NekoStack, NekoStack> {
    override fun process(input: NekoStack): NekoStack {
        if (inPlace) {
            input.erase()
            return input
        } else {
            val clone = input.clone()
            clone.erase()
            return clone
        }
    }
}

/**
 * 提取物品上所有的 [LoreLine].
 */
private class ExtractLoreLines() : PipelineHandler.Stateless<NekoStack, Collection<LoreLine>> {
    override fun process(input: NekoStack): Collection<LoreLine> {
        val components = input.components
        val ret = ObjectArrayList<LoreLine>(components.fuzzySize())
        for ((_, data) in components) {

            when (data) {
                is TooltipProvider.Single -> {
                    val line = data.provideTooltipLore()
                    if (line.shouldShow) {
                        ret.add(line)
                    }
                }

                is TooltipProvider.Cluster -> {
                    val collection = data.provideTooltipLore()
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
private class MapToComponents() : PipelineHandler.Stateless<Collection<LoreLine>, List<Component>> {
    override fun process(input: Collection<LoreLine>): List<Component> {
        TODO("Not yet implemented")
    }
}
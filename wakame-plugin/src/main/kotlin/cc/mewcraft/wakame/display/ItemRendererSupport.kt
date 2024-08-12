package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.packet.PacketNekoStack
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.jetbrains.annotations.Contract
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The [ItemRenderer] used to render [PacketNekoStack].
 */
internal class PacketItemRenderer : KoinComponent, ItemRenderer<PacketNekoStack> {
    private val loreLineFlatter: LoreLineFlatter by inject()
    private val itemModelDataLookup: ItemModelDataLookup by inject()

    override fun render(item: PacketNekoStack) {
        val loreLineList = LoreLineExtractor.extract(item)
        val lore = loreLineFlatter.flatten(loreLineList) // flatten the lore lines
        item.lore(lore)

        val customModelData = itemModelDataLookup[item.key, item.variant]
        item.customModelData(customModelData)

        item.erase() // 为了麦若, 去掉 `minecraft:custom_data`
    }
}

object NameLineExtractor {
    /**
     * Generates a [NameLine] from the [stack].
     *
     * @param stack the item to generate name for
     * @return the generated name
     */
    @Contract(pure = true)
    private fun renderName(stack: NekoStack): NameLine {
        return NameLine.noop()
    }
}

object LoreLineExtractor {
    /**
     * Generates a collection of [LoreLine] from the [stack].
     *
     * The returned collection of [LoreLine] need to be **flattened**
     * before they are put on the item component `minecraft:lore`. Also,
     * the returned collection should not contain any [ConstantLoreLine]
     * since they should be processed by another system.
     *
     * @param stack the stack to generate [LoreLine] for
     * @return the generated collection of [LoreLine]
     */
    fun extract(stack: NekoStack): List<LoreLine> {
        val components = stack.components
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
package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.item.NekoStack
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.jetbrains.annotations.Contract

/**
 * Generates stylized name and lore for a [NekoStack].
 */
internal object TextRenderer {

    /**
     * Generates a [NameLine] from the [stack]。
     *
     * @param stack the item to generate name for
     * @return the generated name
     */
    @Contract(pure = true)
    fun generateNameLine(stack: NekoStack): NameLine {
        return NameLine.noop() // TODO 移除
    }

    /**
     * Generates a collection of [LoreLine] from the [stack].
     *
     * The returned collection of [LoreLine] need to be **flattened**
     * before they are put on the item component `minecraft:lore`. Also,
     * the returned collection should not contain any [ConstantLoreLine]
     * - they are processed in another subsystem.
     *
     * @param stack the stack to generate [LoreLine] for
     * @return the generated collection of [LoreLine]
     */
    @Contract(pure = true)
    fun generateLoreLines(stack: NekoStack): Collection<LoreLine> {
        val components = stack.components
        val ret = ObjectArrayList<LoreLine>(components.fuzzySize())
        for ((_, data) in components) {
            when (data) {
                is TooltipProvider.Single -> {
                    selectiveAdd(ret, data.provideTooltipLore())
                }

                is TooltipProvider.Cluster -> {
                    val collection = data.provideTooltipLore()
                    if (collection.isEmpty()) {
                        continue
                    }
                    for (line in collection) {
                        selectiveAdd(ret, line)
                    }
                }
            }
        }
        return ret
    }

    private fun selectiveAdd(list: ObjectArrayList<LoreLine>, line: LoreLine) {
        if (!line.shouldShow) {
            return
        }
        list.add(line)
    }

}

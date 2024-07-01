package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.binary.getMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.BCustomNameMeta
import it.unimi.dsi.fastutil.objects.ObjectArrayList

internal class TextStylizerImpl : TextStylizer {
    override fun stylizeName(item: NekoStack): NameLine {
        // TODO 2024/6/29 NameLine 不需要发包处理
        return item.getMetaAccessor<BCustomNameMeta>().provideDisplayName()
    }

    override fun stylizeLore(item: NekoStack): Collection<LoreLine> {
        val ret = ObjectArrayList<LoreLine>(16)
        // TODO 2024/6/29 也许单独按组件去渲染就好了? LoreLine 就还是保持原样?

        // add lore lines provided by each meta in the item
        ret.addLoreLines(item.meta.snapshot)

        // add lore lines provided by each cell in the item
        ret.addLoreLines(item.cell.snapshot.values)

        return ret
    }

    private fun ObjectArrayList<LoreLine>.addLoreLines(collection: Iterable<TooltipProvider>) {
        for (provider in collection) {
            val loreLine = provider.provideDisplayLore()
            if (loreLine.shouldShow) continue
            this += loreLine
        }
    }
}

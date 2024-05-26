package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.binary.getMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.BCustomNameMeta
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.text.Component

internal class TextStylizerImpl : TextStylizer {
    override fun stylizeName(item: NekoStack<*>): Component {
        return item.getMetaAccessor<BCustomNameMeta>().provideDisplayName()
    }

    override fun stylizeLore(item: NekoStack<*>): Collection<LoreLine> {
        val ret = ObjectArrayList<LoreLine>(16)

        // for each meta in the item
        for (meta in item.meta.snapshot) {
            val loreLine = meta.provideDisplayLore()
            if (loreLine is NoopLoreLine) continue
            ret += loreLine
        }

        // for each cell in the item
        for (cell in item.cell.snapshot.values) {
            val loreLine = cell.provideDisplayLore()
            if (loreLine is NoopLoreLine) continue
            ret += loreLine
        }

        return ret
    }
}

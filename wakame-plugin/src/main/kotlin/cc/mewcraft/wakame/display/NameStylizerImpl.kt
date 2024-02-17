package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.item.binary.NekoItemStack
import net.kyori.adventure.text.Component

internal class NameStylizerImpl(
    private val metaStylizer: MetaStylizer,
) : NameStylizer {
    override fun stylize(nekoItemStack: NekoItemStack): Component {
        val name = nekoItemStack.itemMeta.name
        return if (name != null) {
            metaStylizer.stylizeName(name)
        } else {
            Component.empty()
        }
    }
}
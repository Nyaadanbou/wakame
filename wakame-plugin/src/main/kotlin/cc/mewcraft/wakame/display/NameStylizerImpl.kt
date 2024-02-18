package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.item.binary.NekoItemStack
import net.kyori.adventure.text.Component

internal class NameStylizerImpl(
    private val metaStylizer: MetaStylizer,
) : NameStylizer {
    override fun stylize(item: NekoItemStack): Component {
        val name = item.metadata.name
        return if (name != null) {
            metaStylizer.stylizeName(name)
        } else {
            Component.empty()
        }
    }
}
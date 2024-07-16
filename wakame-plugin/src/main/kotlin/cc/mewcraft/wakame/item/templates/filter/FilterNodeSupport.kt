package cc.mewcraft.wakame.item.templates.filter

import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.random3.Filter
import cc.mewcraft.wakame.random3.FilterNodeReader
import cc.mewcraft.wakame.util.krequire
import org.spongepowered.configurate.ConfigurationNode

// 实现 FilterNodeReader, 这样我们才能获得具体的泛型信息, 否则 krequire 无法工作.
internal class ItemFilterNodeReader : FilterNodeReader<GenerationContext>() {
    override fun readValue(node: ConfigurationNode): Filter<GenerationContext> {
        return node.krequire()
    }
}
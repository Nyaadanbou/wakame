package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.core.LorePipeline
import cc.mewcraft.wakame.display2.ItemRendererType
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.packet.PacketNekoStack

/**
 * The [ItemRenderer] used to render [PacketNekoStack].
 */
internal class StandardItemRenderer(
    private val loreLineFlatter: LoreLineFlatter
) : ItemRenderer<PacketNekoStack> {

    override fun render(item: PacketNekoStack) {
        val lore = LorePipeline.create(DisplayPipelineHandlers.EXTRACT_LORE_LINES(ItemRendererType.STANDARD))
            .concat(DisplayPipelineHandlers.MAP_TO_COMPONENTS(loreLineFlatter))
            .execute(item)
        item.lore(lore)

        val customModelData = ItemModelDataLookup[item.key, item.variant]
        item.customModelData(customModelData)

        item.erase() // 为了麦若, 去掉 `minecraft:custom_data`
    }
}
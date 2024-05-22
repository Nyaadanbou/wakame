package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.item.binary.PacketNekoStack
import cc.mewcraft.wakame.item.binary.PlayNekoStack
import cc.mewcraft.wakame.item.binary.ShowNekoStack
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.util.backingCustomModelData
import cc.mewcraft.wakame.util.backingCustomName
import cc.mewcraft.wakame.util.backingLore
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The [ItemRenderer] used to render [PlayNekoStacks][PlayNekoStack].
 *
 * @property textStylizer
 * @property loreFinalizer
 */
internal class PlayItemRenderer(
    private val textStylizer: TextStylizer,
    private val loreFinalizer: LoreFinalizer,
) : KoinComponent, ItemRenderer<PlayNekoStack> {
    private val modelLookup: ItemModelDataLookup by inject()

    override fun render(nekoStack: PlayNekoStack) {
        val customName = textStylizer.stylizeName(nekoStack)
        val lore = textStylizer.stylizeLore(nekoStack).let(loreFinalizer::finalize)

        val variant = nekoStack.variant
        val customModelData = modelLookup[nekoStack.key, variant]

        // directly edit the backing ItemMeta to avoid cloning
        nekoStack.itemStack.apply {
            backingCustomName = customName
            backingLore = lore
            backingCustomModelData = customModelData
        }

        // 为了麦若，去掉物品的真实根标签
        nekoStack.erase()
    }
}

/**
 * The [ItemRenderer] used to render [ShowNekoStacks][ShowNekoStack].
 *
 * @property textStylizer
 * @property loreFinalizer
 */
internal class ShowItemRenderer(
    private val textStylizer: TextStylizer,
    private val loreFinalizer: LoreFinalizer,
) : ItemRenderer<ShowNekoStack> {
    override fun render(nekoStack: ShowNekoStack) {
        TODO("Not yet implemented")
    }
}

internal class PacketItemRenderer(
    private val textStylizer: TextStylizer,
    private val loreFinalizer: LoreFinalizer,
) : ItemRenderer<PacketNekoStack> {
    override fun render(nekoStack: PacketNekoStack) {
        println(nekoStack)
    }
}
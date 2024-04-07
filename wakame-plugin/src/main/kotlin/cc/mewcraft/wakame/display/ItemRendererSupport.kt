package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.item.binary.PlayNekoStack
import cc.mewcraft.wakame.item.binary.ShowNekoStack
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.util.backingCustomModelData
import cc.mewcraft.wakame.util.backingDisplayName
import cc.mewcraft.wakame.util.backingLore
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
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
    private val gsonSerial: GsonComponentSerializer by inject()
    private val modelLookup: ItemModelDataLookup by inject()

    override fun render(nekoStack: PlayNekoStack) {
        val displayName = textStylizer.stylizeName(nekoStack)
        val displayLore = textStylizer.stylizeLore(nekoStack).let(loreFinalizer::finalize)

        val variant = nekoStack.variant
        val cmd = modelLookup[nekoStack.key, variant]

        // directly edit the backing ItemMeta to avoid cloning
        nekoStack.itemStack.apply {
            backingDisplayName = gsonSerial.serialize(displayName)
            backingLore = displayLore.map(gsonSerial::serialize)
            backingCustomModelData = cmd
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
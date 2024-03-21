package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.util.backingCustomModelData
import cc.mewcraft.wakame.util.backingDisplayName
import cc.mewcraft.wakame.util.backingLore
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class ItemRendererImpl(
    private val textStylizer: TextStylizer,
    private val loreFinalizer: LoreFinalizer,
) : KoinComponent, ItemRenderer {
    private val gsonSerial: GsonComponentSerializer by inject()
    private val modelLookup: ItemModelDataLookup by inject()

    override fun render(copy: NekoStack) {
        require(copy.isNeko) { "Can't render a non-neko ItemStack" }

        val displayName = textStylizer.stylizeName(copy)
        val displayLore = textStylizer.stylizeLore(copy).let(loreFinalizer::finalize)

        val variant = copy.variant
        val cmd = modelLookup[copy.key, variant]

        // directly edit the backing ItemMeta to avoid cloning
        copy.handle.apply {
            backingDisplayName = gsonSerial.serialize(displayName)
            backingLore = displayLore.map(gsonSerial::serialize)
            backingCustomModelData = cmd
        }

        // 为了麦若，去掉物品的真实根标签
        copy.erase()
    }
}
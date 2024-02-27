package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.item.binary.NekoItemStack
import cc.mewcraft.wakame.util.backingLore
import cc.mewcraft.wakame.util.backingDisplayName
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class ItemRendererImpl(
    private val textStylizer: TextStylizer,
    private val loreFinalizer: LoreFinalizer,
) : KoinComponent, ItemRenderer {
    private val gsonSerial: GsonComponentSerializer by inject()

    override fun render(copy: NekoItemStack) {
        require(copy.isNeko) { "Can't render a non-neko ItemStack" }

        // 因为对这里的 itemStack 进行修改不会影响原始的 itemStack，所以我们可以放心地修改它
        val displayName = textStylizer.stylizeName(copy)
        val displayLore = textStylizer.stylizeLore(copy).let(loreFinalizer::finalize)

        // directly edit the backing ItemMeta to avoid cloning
        copy.handle.let {
            it.backingDisplayName = gsonSerial.serialize(displayName)
            it.backingLore = displayLore.map(gsonSerial::serialize)
        }

        // 为了麦若，去掉物品的真实根标签
        copy.erase()
    }
}
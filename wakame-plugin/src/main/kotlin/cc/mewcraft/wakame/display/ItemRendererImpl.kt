package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.item.binary.NekoItemStack
import cc.mewcraft.wakame.util.displayLoreNms
import cc.mewcraft.wakame.util.displayNameNms
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

internal class ItemRendererImpl(
    private val textStylizer: TextStylizer,
    private val loreFinalizer: LoreFinalizer,
    private val gsonComponentSerializer: GsonComponentSerializer,
) : ItemRenderer {
    override fun render(copy: NekoItemStack) {
        require(copy.isNeko) { "Can't render a non-neko ItemStack" }

        // 因为对这里的 itemStack 进行修改不会影响原始的 itemStack，所以我们可以放心地修改它
        val displayName = textStylizer.stylizeName(copy)
        val displayLore = textStylizer.stylizeLore(copy).let(loreFinalizer::finalize)

        // directly edit the backing ItemMeta to avoid cloning
        copy.handle.let {
            it.displayNameNms = gsonComponentSerializer.serialize(displayName)
            it.displayLoreNms = displayLore.map(gsonComponentSerializer::serialize)
        }

        // 为了麦若，去掉物品的真实根标签
        @OptIn(InternalApi::class) copy.erase()
    }
}
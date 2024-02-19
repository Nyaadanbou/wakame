package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.item.binary.NekoItemStack
import cc.mewcraft.wakame.item.binary.NekoItemStackImpl

internal class ItemRendererImpl(
    private val nameStylizer: NameStylizer,
    private val loreStylizer: LoreStylizer,
    private val loreFinalizer: LoreFinalizer,
) : ItemRenderer {
    override fun render(copy: NekoItemStack) {
        require(copy.isNeko) { "Can't render a non-neko ItemStack" }

        // 因为对这里的 itemStack 进行修改不会影响原始的 itemStack，所以我们可以放心地修改它
        val name = nameStylizer.stylize(copy)
        val lore = loreStylizer.stylize(copy) // To g22: 不包含 FixedLoreLine
        val finalizedLore = loreFinalizer.finalize(lore)

        copy.handle.editMeta {
            it.displayName(name)
            it.lore(finalizedLore)
        }

        // 为了麦若，去掉物品的真实根标签
        (copy as NekoItemStackImpl).tags.remove(NekoTags.ROOT)
    }
}
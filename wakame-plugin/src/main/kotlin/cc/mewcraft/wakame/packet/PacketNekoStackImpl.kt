package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.item.binary.BaseNekoStack
import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.util.nekoCompound
import cc.mewcraft.wakame.util.nekoCompoundOrNull
import cc.mewcraft.wakame.util.removeNekoCompound
import com.github.retrooper.packetevents.protocol.item.ItemStack
import me.lucko.helper.shadows.nbt.CompoundShadowTag

interface PacketNekoStack : NekoStack {
    val itemStack: ItemStack

    val isNeko: Boolean
}

@JvmInline
internal value class PacketNekoStackImpl(
    override val itemStack: ItemStack,
) : BaseNekoStack, PacketNekoStack {
    override val tags: CompoundShadowTag
        get() = itemStack.nekoCompound

    override val isNeko: Boolean
        get() = itemStack.nekoCompoundOrNull != null

    override fun erase() {
        itemStack.removeNekoCompound()
    }
}
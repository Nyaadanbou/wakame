package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.item.binary.BaseNekoStack
import cc.mewcraft.wakame.item.binary.PacketNekoStack
import cc.mewcraft.wakame.item.binary.PlayNekoStack
import cc.mewcraft.wakame.item.binary.ShowNekoStack
import cc.mewcraft.wakame.util.nekoCompound
import cc.mewcraft.wakame.util.nekoCompoundOrNull
import cc.mewcraft.wakame.util.removeNekoCompound
import com.github.retrooper.packetevents.protocol.item.ItemStack
import me.lucko.helper.shadows.nbt.CompoundShadowTag

@JvmInline
internal value class PacketNekoStackImpl(
    override val itemStack: ItemStack,
) : BaseNekoStack<ItemStack>, PacketNekoStack {
    override val tags: CompoundShadowTag
        get() = itemStack.nekoCompound

    override val isNmsBacked: Boolean
        get() = true

    override val isNeko: Boolean
        get() = itemStack.nekoCompoundOrNull != null

    override val play: PlayNekoStack
        get() = TODO("Not yet implemented")

    override val show: ShowNekoStack
        get() = TODO("Not yet implemented")

    override fun erase() {
        itemStack.removeNekoCompound()
    }
}
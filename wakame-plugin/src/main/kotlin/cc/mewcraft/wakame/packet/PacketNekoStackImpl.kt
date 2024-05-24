package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.item.binary.BaseNekoStack
import cc.mewcraft.wakame.item.binary.PacketNekoStack
import cc.mewcraft.wakame.item.binary.PlayNekoStack
import cc.mewcraft.wakame.item.binary.ShowNekoStack
import cc.mewcraft.wakame.util.nekoCompound
import com.github.retrooper.packetevents.protocol.item.ItemStack
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import org.bukkit.inventory.ItemStack as BukkitItemStack

@JvmInline
internal value class PacketNekoStackImpl(
    override val packetStack: ItemStack,
) : BaseNekoStack, PacketNekoStack {
    override val tags: CompoundShadowTag
        get() = packetStack.nekoCompound

    override val itemStack: BukkitItemStack
        get() = TODO("Not yet implemented")

    override val play: PlayNekoStack
        get() = TODO("Not yet implemented")

    override val show: ShowNekoStack
        get() = TODO("Not yet implemented")
}
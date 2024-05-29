package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.binary.NekoStackBase
import cc.mewcraft.wakame.util.WAKAME_COMPOUND_NAME
import cc.mewcraft.wakame.util.packetevents.nekoCompoundOrNull
import cc.mewcraft.wakame.util.packetevents.removeNekoCompound
import com.github.retrooper.packetevents.protocol.component.ComponentTypes
import com.github.retrooper.packetevents.protocol.item.ItemStack
import me.lucko.helper.shadows.nbt.CompoundShadowTag

/**
 * Wraps the [ItemStack] as a [PacketNekoStack].
 */
val ItemStack.packetNekoStackOrNull: PacketNekoStack?
    get() {
        if (!this.isNeko) return null
        val packetNekoStack = PacketNekoStackImpl(this) // 发包系统只读取 NBT，因此不需要 copy
        return packetNekoStack
    }

/**
 * Checks whether the [ItemStack] is neko.
 */
val ItemStack.isNeko: Boolean
    get() {
        return this.components.get(ComponentTypes.CUSTOM_DATA)?.getCompoundTagOrNull(WAKAME_COMPOUND_NAME) != null
    }

/**
 * A wrapper of a [Packet ItemStack][ItemStack].
 */
interface PacketNekoStack : NekoStack {
    val itemStack: ItemStack
}

private class PacketNekoStackImpl(
    override val itemStack: ItemStack,
) : NekoStackBase, PacketNekoStack {
    // We use property initializer here as it would be called multiple times,
    // and we don't want to do the unnecessary NBT conversion again and again
    override val tags: CompoundShadowTag = requireNotNull(itemStack.nekoCompoundOrNull) {
        "The ItemStack is not neko. Did you forget to check it before instantiating the PacketNekoStack?"
    }

    override fun erase() {
        itemStack.removeNekoCompound()
    }
}
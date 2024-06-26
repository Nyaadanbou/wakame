package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.item.binary.NekoStackBase
import cc.mewcraft.wakame.util.WAKAME_TAG_NAME
import cc.mewcraft.wakame.util.packetevents.wakameTagOrNull
import com.github.retrooper.packetevents.protocol.component.ComponentTypes
import com.github.retrooper.packetevents.protocol.item.ItemStack
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import kotlin.jvm.optionals.getOrNull

val ItemStack.isNeko: Boolean
    get() = this.components.get(ComponentTypes.CUSTOM_DATA)?.getCompoundTagOrNull(WAKAME_TAG_NAME) != null

val ItemStack.tryPacketNekoStack: PacketNekoStack?
    get() {
        if (!this.isNeko) return null
        return PacketNekoStack(this) // 发包系统只读取 NBT，因此不需要 copy
    }

class PacketNekoStack(
    val itemStack: ItemStack,
) : NekoStackBase {

    // We use property initializer here as it would be called multiple times,
    // and we don't want to do the unnecessary NBT conversion again and again
    override val tags: CompoundShadowTag = requireNotNull(itemStack.wakameTagOrNull) {
        "The ItemStack is not neko. Did you forget to check it before instantiating the PacketNekoStack?"
    }

    override fun erase() {
        itemStack.getComponent(ComponentTypes.CUSTOM_DATA).getOrNull()?.removeTag(WAKAME_TAG_NAME)
    }
}

package cc.mewcraft.wakame.packet

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.item.binary.NekoStackBase
import cc.mewcraft.wakame.util.WAKAME_TAG_NAME
import cc.mewcraft.wakame.util.packetevents.wakameTagOrNull
import com.github.retrooper.packetevents.protocol.component.ComponentTypes
import com.github.retrooper.packetevents.protocol.item.ItemStack
import kotlin.jvm.optionals.getOrNull
import org.bukkit.inventory.ItemStack as BukkitStack

val ItemStack.isNeko: Boolean
    get() {
        return this.components.get(ComponentTypes.CUSTOM_DATA)?.getCompoundTagOrNull(WAKAME_TAG_NAME) != null
    }

val ItemStack.tryNekoStack: PacketNekoStack?
    get() {
        if (!this.isNeko)
            return null
        return PacketNekoStack(this) // 发包系统只读取 NBT，因此不需要 copy
    }

class PacketNekoStack(
    val packetStack: ItemStack,
) : NekoStackBase {

    override val itemStack: BukkitStack
        get() {
            throw UnsupportedOperationException("This operation is not allowed in PacketNekoStack")
        }

    // We use property initializer here as it would be called multiple times,
    // and we don't want to do the unnecessary NBT conversion again and again
    override val tags: CompoundTag = requireNotNull(packetStack.wakameTagOrNull) {
        "The ItemStack is not from wakame. Did you check it before instantiating the NekoStack?"
    }

    override fun erase() {
        packetStack.getComponent(ComponentTypes.CUSTOM_DATA).getOrNull()?.removeTag(WAKAME_TAG_NAME)
    }

    private companion object {
        val EMPTY_BUKKIT_STACK = BukkitStack.empty()
    }
}

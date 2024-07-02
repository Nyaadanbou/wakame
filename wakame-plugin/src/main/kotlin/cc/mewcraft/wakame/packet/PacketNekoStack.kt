package cc.mewcraft.wakame.packet

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.NekoStackSupport
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.util.WAKAME_TAG_NAME
import cc.mewcraft.wakame.util.packetevents.wakameTagOrNull
import com.github.retrooper.packetevents.protocol.component.ComponentTypes
import net.kyori.adventure.key.Key
import java.util.UUID
import kotlin.jvm.optionals.getOrNull
import com.github.retrooper.packetevents.protocol.item.ItemStack as PacketStack
import org.bukkit.inventory.ItemStack as BukkitStack

internal val PacketStack.isNeko: Boolean
    get() {
        return this.components.get(ComponentTypes.CUSTOM_DATA)?.getCompoundTagOrNull(WAKAME_TAG_NAME) != null
    }

internal val PacketStack.tryNekoStack: PacketNekoStack?
    get() {
        if (!this.isNeko)
            return null
        return PacketNekoStack(this) // 发包系统只读取 NBT，因此不需要 copy
    }

internal class PacketNekoStack(
    val stack: PacketStack,
) : NekoStack {

    // We use property initializer here as it would be called multiple times,
    // and we don't want to do the unnecessary NBT conversion again and again
    override val nbt: CompoundTag = requireNotNull(stack.wakameTagOrNull) {
        "The ItemStack is not from wakame. Did you check it before instantiating the PacketNekoStack?"
    }

    override val handle: BukkitStack
        // FIXME 2024/7/2 需要所有组件可能会用到的信息从 PacketStack 复制到 BukkitStack 上; 不要抛 UnsupportedOperationEx
        get() = unsupportedOperation()

    override val namespace: String
        get() = NekoStackSupport.getNamespaceOrThrow(nbt)

    override val path: String
        get() = NekoStackSupport.getPathOrThrow(nbt)

    override val key: Key
        get() = NekoStackSupport.getKeyOrThrow(nbt)

    override var variant: Int
        get() = NekoStackSupport.getVariant(nbt)
        set(value) = NekoStackSupport.setVariant(nbt, value)

    override val uuid: UUID
        get() = NekoStackSupport.getUuid(nbt)

    override val slot: ItemSlot
        get() = NekoStackSupport.getSlot(nbt)

    override val prototype: NekoItem
        get() = NekoStackSupport.getPrototypeOrThrow(nbt)

    override val components: ItemComponentMap
        get() = NekoStackSupport.getComponents(handle)

    override val templates: ItemTemplateMap
        get() = NekoStackSupport.getTemplates(nbt)

    override fun erase() {
        stack.getComponent(ComponentTypes.CUSTOM_DATA).getOrNull()?.removeTag(WAKAME_TAG_NAME)
    }

    private fun unsupportedOperation(): Nothing {
        throw UnsupportedOperationException("This operation is not allowed in PacketNekoStack")
    }
}

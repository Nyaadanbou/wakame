package cc.mewcraft.wakame.packet

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.NekoStackSupport
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.util.WAKAME_TAG_NAME
import cc.mewcraft.wakame.util.wakameTag
import com.github.retrooper.packetevents.protocol.component.ComponentTypes
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore
import com.github.retrooper.packetevents.protocol.item.ItemStack
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import java.util.UUID
import kotlin.jvm.optionals.getOrNull
import com.github.retrooper.packetevents.protocol.item.ItemStack as PacketStack
import org.bukkit.inventory.ItemStack as BukkitStack

internal fun PacketStack.takeUnlessEmpty(): ItemStack? {
    return this.takeIf { !it.isEmpty } // FIXME 需要这个吗？
}

internal val PacketStack.isNeko: Boolean
    get() {
        if (!this.hasComponentPatches()) {
            return false // early return
        }
        return this.components.get(ComponentTypes.CUSTOM_DATA)
            ?.getCompoundTagOrNull(WAKAME_TAG_NAME) != null
    }

internal val PacketStack.tryNekoStack: PacketNekoStack?
    get() {
        return PacketNekoStack.of(this) // 发包系统只读取 NBT，因此不需要 copy
    }

// 开发日记:
// 该 NekoStack 仅用于物品发包系统内部.
internal class PacketNekoStack
private constructor(
    /**
     * 警告: 为确保代码的可维护性, 该成员仅用于直接构建 PacketWrapper.
     */
    val handle0: PacketStack,
) : NekoStack {
    /**
     * Sets the custom name. You may pass a `null` to remove the name.
     */
    fun setCustomName(value: Component?) {
        if (value != null) {
            handle0.setComponent(ComponentTypes.CUSTOM_NAME, value)
        } else {
            handle0.unsetComponent(ComponentTypes.CUSTOM_NAME)
        }
    }

    /**
     * Sets the item name. You may pass a `null` to remove it.
     */
    fun setItemName(value: Component?) {
        if (value != null) {
            handle0.setComponent(ComponentTypes.ITEM_NAME, value)
        } else {
            handle0.unsetComponent(ComponentTypes.ITEM_NAME)
        }
    }

    /**
     * Sets the item lore. You may pass a `null` to remove it.
     */
    fun setLore(value: List<Component>?) {
        if (value != null) {
            handle0.setComponent(ComponentTypes.LORE, ItemLore(value))
        } else {
            handle0.unsetComponent(ComponentTypes.LORE)
        }
    }

    /**
     * Sets the custom model data. You may pass a `null` to remove it.
     */
    fun setCustomModelData(value: Int?) {
        if (value != null) {
            handle0.setComponent(ComponentTypes.CUSTOM_MODEL_DATA, value)
        } else {
            handle0.unsetComponent(ComponentTypes.CUSTOM_MODEL_DATA)
        }
    }

    // 开发日记:
    // 由于 ItemComponentMap 对 BukkitStack 有直接依赖, 我们需要转换一个
    override val handle: BukkitStack = SpigotConversionUtil.toBukkitItemStack(handle0)

    // 开发日记1: We use property initializer here as it would be called multiple times,
    // and we don't want to do the unnecessary NBT conversion again and again
    // 开发日记2: 该 NBT 标签应该只接受读操作 (虽然可以写, 但不保证生效)
    override val nbt: CompoundTag = handle.wakameTag

    override val namespace: String
        get() = NekoStackSupport.getNamespaceOrThrow(nbt)

    override val path: String
        get() = NekoStackSupport.getPathOrThrow(nbt)

    override val key: Key
        get() = NekoStackSupport.getKeyOrThrow(nbt)

    override var variant: Int
        get() = NekoStackSupport.getVariant(nbt)
        set(value) = abortWriteOps()

    override val uuid: UUID
        get() = NekoStackSupport.getUuid(nbt)

    override val slot: ItemSlot
        get() = NekoStackSupport.getSlot(nbt)

    override val prototype: NekoItem
        get() = NekoStackSupport.getPrototypeOrThrow(nbt)

    override val components: ItemComponentMap
        get() = NekoStackSupport.getImmutableComponents(handle) // 使用 ImmutableMap 以禁止写入新的组件信息

    override val templates: ItemTemplateMap
        get() = NekoStackSupport.getTemplates(nbt)

    override val behaviors: ItemBehaviorMap
        get() = NekoStackSupport.getBehaviors(nbt)

    override fun erase() {
        handle0.getComponent(ComponentTypes.CUSTOM_DATA).getOrNull()?.removeTag(WAKAME_TAG_NAME)
    }

    companion object {
        fun of(stack: PacketStack): PacketNekoStack? {
            if (!stack.isNeko)
                return null
            return PacketNekoStack(stack) // 发包系统只读取 NBT，因此不需要 copy
        }
    }

    private fun abortWriteOps(): Nothing {
        throw UnsupportedOperationException("Write operation is not allowed in PacketNekoStack")
    }
}

package cc.mewcraft.wakame.packet

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.NekoStackSupport
import cc.mewcraft.wakame.item.VanillaNekoStackRegistry
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.WAKAME_TAG_NAME
import cc.mewcraft.wakame.util.wakameTag
import com.github.retrooper.packetevents.protocol.component.ComponentTypes
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore
import com.github.retrooper.packetevents.protocol.item.ItemStack
import com.github.retrooper.packetevents.resources.ResourceLocation
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import java.util.UUID
import com.github.retrooper.packetevents.protocol.item.ItemStack as PacketStack
import org.bukkit.inventory.ItemStack as BukkitStack

internal fun PacketStack.takeUnlessEmpty(): ItemStack? =
    this.takeIf { !it.isEmpty }

internal fun ResourceLocation.toKey(): Key =
    Key(this.namespace, this.key)

internal val PacketStack.isNeko: Boolean
    get() {
        if (this.hasComponentPatches()) {
            val customData = this.components.get(ComponentTypes.CUSTOM_DATA)
            if (customData != null) {
                if (customData.getCompoundTagOrNull(WAKAME_TAG_NAME) != null) {
                    return true
                }
            }
        }
        val key = this.type.name.toKey()
        val ret = VanillaNekoStackRegistry.has(key)
        return ret
    }

internal val PacketStack.isCustomNeko: Boolean
    get() {
        if (!this.hasComponentPatches()) {
            return false
        }
        val ret = this.components.get(ComponentTypes.CUSTOM_DATA)?.getCompoundTagOrNull(WAKAME_TAG_NAME) != null
        return ret
    }

internal val PacketStack.isVanillaNeko: Boolean
    get() {
        if (isCustomNeko) {
            return false
        }
        val key = this.type.name.toKey()
        val ret = VanillaNekoStackRegistry.has(key)
        return ret
    }

internal val PacketStack.tryNekoStack: PacketNekoStack?
    get() {
        if (this.hasComponentPatches()) {
            val customData = this.components.get(ComponentTypes.CUSTOM_DATA)
            if (customData != null) {
                val wakameTag = customData.getCompoundTagOrNull(WAKAME_TAG_NAME)
                if (wakameTag != null) {
                    return PacketCustomNekoStack(this)
                }
            }
        }
        val key = this.type.name.toKey()
        val vns = VanillaNekoStackRegistry.get(key)
        if (vns != null) {
            return PacketVanillaNekoStack(this, key, vns.prototype, vns.components)
        }
        return null
    }

// 开发日记 2024/7/11
// 发包还需要修改原版物品, 因此底层的实现会有不同.
internal interface PacketNekoStack : NekoStack {
    /**
     * 为确保代码的可维护性, 该成员仅用于直接构建 PacketWrapper.
     */
    val handle0: PacketStack

    /**
     * 设置自定义名称. 您可以传递 `null` 来移除名称.
     */
    fun customName(value: Component?) {
        if (value != null) {
            handle0.setComponent(ComponentTypes.CUSTOM_NAME, value)
        } else {
            handle0.unsetComponent(ComponentTypes.CUSTOM_NAME)
        }
    }

    /**
     * 设置物品名称. 您可以传递 `null` 来移除它.
     */
    fun itemName(value: Component?) {
        if (value != null) {
            handle0.setComponent(ComponentTypes.ITEM_NAME, value)
        } else {
            handle0.unsetComponent(ComponentTypes.ITEM_NAME)
        }
    }

    /**
     * 设置物品描述. 您可以传递 `null` 来移除它.
     */
    fun lore(value: List<Component>?) {
        if (value != null) {
            handle0.setComponent(ComponentTypes.LORE, ItemLore(value))
        } else {
            handle0.unsetComponent(ComponentTypes.LORE)
        }
    }

    /**
     * 设置自定义模型数据. 您可以传递 `null` 来移除它.
     */
    fun customModelData(value: Int?) {
        if (value != null) {
            handle0.setComponent(ComponentTypes.CUSTOM_MODEL_DATA, value)
        } else {
            handle0.unsetComponent(ComponentTypes.CUSTOM_MODEL_DATA)
        }
    }
}

// 开发日记:
// 该 NekoStack 仅用于物品发包系统内部.
private class PacketCustomNekoStack(
    override val handle0: PacketStack,
) : PacketNekoStack {
    // 开发日记:
    // 由于 ItemComponentMap 对 BukkitStack 有直接依赖, 我们需要转换一个
    override val handle: BukkitStack = SpigotConversionUtil.toBukkitItemStack(handle0)

    override val itemStack: BukkitStack
        get() = handle.clone()

    // 开发日记1: We use property initializer here as it would be called multiple times,
    // and we don't want to do the unnecessary NBT conversion again and again
    // 开发日记2: 该 NBT 标签应该只接受读操作 (虽然可以写, 但不保证生效, 也没啥用应该)
    override val nbt: CompoundTag = handle.wakameTag

    override val namespace: String
        get() = NekoStackSupport.getNamespaceOrThrow(nbt)

    override val path: String
        get() = NekoStackSupport.getPathOrThrow(nbt)

    override val key: Key
        get() = NekoStackSupport.getKeyOrThrow(nbt)

    override var variant: Int
        get() = NekoStackSupport.getVariant(nbt)
        set(_) = abortWriteOps()

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
        handle0.unsetComponent(ComponentTypes.CUSTOM_DATA)
    }

    private fun abortWriteOps(): Nothing {
        throw UnsupportedOperationException("Write operation is not allowed in PacketCustomNekoStack")
    }
}

private class PacketVanillaNekoStack(
    override val handle0: ItemStack,
    override val key: Key,
    override val prototype: NekoItem,
    override val components: ItemComponentMap,
) : PacketNekoStack {
    override val nbt: CompoundTag
        get() = abortReadOps()

    override val itemStack: org.bukkit.inventory.ItemStack
        get() = abortReadOps()

    override val handle: BukkitStack
        get() = abortReadOps()

    override val namespace: String
        get() = key.namespace()

    override val path: String
        get() = key.value()

    override var variant: Int
        get() = 0
        set(_) = abortWriteOps()

    override val uuid: UUID
        get() = prototype.uuid

    override val slot: ItemSlot
        get() = prototype.slot

    override val templates: ItemTemplateMap
        get() = prototype.templates

    override val behaviors: ItemBehaviorMap
        get() = prototype.behaviors

    override fun erase() {
        // no-op
        // 本来就是原版物品, 不需要擦除 `custom_data`
    }

    private fun abortReadOps(): Nothing {
        throw UnsupportedOperationException("Read operation is not allowed in PacketVanillaNekoStack")
    }

    private fun abortWriteOps(): Nothing {
        throw UnsupportedOperationException("Write operation is not allowed in PacketVanillaNekoStack")
    }
}
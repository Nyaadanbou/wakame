package cc.mewcraft.wakame.packet

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.SharedConstants
import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.ItemSlotGroup
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.NekoStackSupport
import cc.mewcraft.wakame.item.VanillaNekoStackRegistry
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.util.unsafeNyaTagOrThrow
import com.github.retrooper.packetevents.protocol.component.ComponentTypes
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore
import com.github.retrooper.packetevents.protocol.item.ItemStack
import com.github.retrooper.packetevents.resources.ResourceLocation
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Material
import kotlin.jvm.optionals.getOrNull
import org.bukkit.inventory.ItemStack as BukkitStack

internal fun ItemStack.takeUnlessEmpty(): ItemStack? =
    this.takeIf { !it.isEmpty }

internal fun ResourceLocation.toKey(): Key =
    Key.key(namespace, key)

internal val ItemStack.isNeko: Boolean
    get() {
        if (this.hasComponentPatches()) {
            val customData = this.components.get(ComponentTypes.CUSTOM_DATA)
            if (customData != null) {
                if (customData.getCompoundTagOrNull(SharedConstants.PLUGIN_NAME) != null) {
                    return true
                }
            }
        }
        val key = this.type.name.toKey()
        val ret = VanillaNekoStackRegistry.has(key)
        return ret
    }

internal val ItemStack.isCustomNeko: Boolean
    get() {
        if (!this.hasComponentPatches()) {
            return false
        }
        val ret = this.components.get(ComponentTypes.CUSTOM_DATA)?.getCompoundTagOrNull(SharedConstants.PLUGIN_NAME) != null
        return ret
    }

internal val ItemStack.isVanillaNeko: Boolean
    get() {
        if (isCustomNeko) {
            return false
        }
        val key = this.type.name.toKey()
        val ret = VanillaNekoStackRegistry.has(key)
        return ret
    }

internal val ItemStack.isRenderable: Boolean
    get() = getComponent(ComponentTypes.CUSTOM_DATA)
        ?.getOrNull()
        ?.getCompoundTagOrNull(SharedConstants.PLUGIN_NAME)
        ?.getCompoundTagOrNull(ItemComponentMap.TAG_COMPONENTS)
        ?.getTagOrNull(ItemConstants.SYSTEM_USE) == null

/**
 * 尝试将 ItemStack 转换为 NekoStack, 用作渲染.
 */
internal val ItemStack.tryNekoStack: PacketNekoStack?
    get() {
        if (isEmpty)
            return null
        if (!isRenderable)
            return null

        val wakameTag = getComponent(ComponentTypes.CUSTOM_DATA).getOrNull()?.getCompoundTagOrNull(SharedConstants.PLUGIN_NAME)
        if (wakameTag != null) {
            return PacketCustomNekoStack(this)
        }

        val itemTypeKey = type.name.toKey()
        val vanillaNekoStack = VanillaNekoStackRegistry.get(itemTypeKey)
        if (vanillaNekoStack != null) {
            return PacketVanillaNekoStack(this, itemTypeKey, vanillaNekoStack.prototype, vanillaNekoStack.components)
        }

        return null
    }

// 开发日记 2024/7/11
// 发包还需要修改原版物品, 因此底层的实现会有不同.
internal interface PacketNekoStack : NekoStack {
    /**
     * 该成员仅用于直接构建 [com.github.retrooper.packetevents.wrapper.PacketWrapper].
     */
    val packetItemStack: ItemStack

    /**
     * 设置自定义名称. 您可以传递 `null` 来移除名称.
     */
    fun customName(value: Component?) {
        if (value != null) {
            packetItemStack.setComponent(ComponentTypes.CUSTOM_NAME, value)
        } else {
            packetItemStack.unsetComponent(ComponentTypes.CUSTOM_NAME)
        }
    }

    /**
     * 设置物品名称. 您可以传递 `null` 来移除它.
     */
    fun itemName(value: Component?) {
        if (value != null) {
            packetItemStack.setComponent(ComponentTypes.ITEM_NAME, value)
        } else {
            packetItemStack.unsetComponent(ComponentTypes.ITEM_NAME)
        }
    }

    /**
     * 设置物品描述. 您可以传递 `null` 来移除它.
     */
    fun lore(value: List<Component>?) {
        if (value != null) {
            packetItemStack.setComponent(ComponentTypes.LORE, ItemLore(value))
        } else {
            packetItemStack.unsetComponent(ComponentTypes.LORE)
        }
    }

    /**
     * 设置自定义模型数据. 您可以传递 `null` 来移除它.
     */
    fun customModelData(value: Int?) {
        if (value != null) {
            packetItemStack.setComponent(ComponentTypes.CUSTOM_MODEL_DATA, value)
        } else {
            packetItemStack.unsetComponent(ComponentTypes.CUSTOM_MODEL_DATA)
        }
    }
}

// 开发日记:
// 该 NekoStack 仅用于物品发包系统内部.
private class PacketCustomNekoStack(
    override val packetItemStack: ItemStack,
) : PacketNekoStack {
    // 开发日记:
    // 由于 ItemComponentMap 对 BukkitStack 有直接依赖, 我们需要转换一个
    private val handle: BukkitStack = SpigotConversionUtil.toBukkitItemStack(packetItemStack)

    // 开发日记1: We use property initializer here as it would be called multiple times,
    // and we don't want to do the unnecessary NBT conversion again and again
    // 开发日记2: 该 NBT 标签应该只接受读操作 (虽然可以写, 但不保证生效, 也没啥用应该)
    private val nyaTag: CompoundTag = handle.unsafeNyaTagOrThrow

    override val isEmpty: Boolean
        get() = false

    override val itemType: Material
        get() = handle.type

    override val itemStack: BukkitStack
        get() = abortReads()

    override val namespace: String
        get() = NekoStackSupport.getNamespaceOrThrow(nyaTag)

    override val path: String
        get() = NekoStackSupport.getPathOrThrow(nyaTag)

    override val key: Key
        get() = NekoStackSupport.getKeyOrThrow(nyaTag)

    override var variant: Int
        get() = NekoStackSupport.getVariant(nyaTag)
        set(_) = abortWrites()

    override val slotGroup: ItemSlotGroup
        get() = NekoStackSupport.getSlotGroup(nyaTag)

    override val prototype: NekoItem
        get() = NekoStackSupport.getPrototypeOrThrow(nyaTag)

    override val components: ItemComponentMap
        get() = NekoStackSupport.getImmutableComponents(handle) // 使用 ImmutableMap 以禁止写入新的组件信息

    override val templates: ItemTemplateMap
        get() = NekoStackSupport.getTemplates(nyaTag)

    override val behaviors: ItemBehaviorMap
        get() = NekoStackSupport.getBehaviors(nyaTag)

    override val unsafe: NekoStack.Unsafe
        get() = Unsafe(this)

    override fun clone(): NekoStack {
        throw UnsupportedOperationException("clone() is not supported")
    }

    override fun erase() {
        packetItemStack.unsetComponent(ComponentTypes.CUSTOM_DATA)
    }

    private fun abortReads(): Nothing {
        throw UnsupportedOperationException("Read operation is not allowed in PacketCustomNekoStack")
    }

    private fun abortWrites(): Nothing {
        throw UnsupportedOperationException("Write operation is not allowed in PacketCustomNekoStack")
    }

    class Unsafe(val nekoStack: PacketCustomNekoStack) : NekoStack.Unsafe {
        override val nyaTag: CompoundTag
            get() = nekoStack.nyaTag
        override val handle: BukkitStack
            get() = nekoStack.handle
    }
}

private class PacketVanillaNekoStack(
    override val packetItemStack: ItemStack,
    override val key: Key,
    override val prototype: NekoItem,
    override val components: ItemComponentMap,
) : PacketNekoStack {
    override val isEmpty: Boolean
        get() = false

    override val itemType: Material
        get() = SpigotConversionUtil.toBukkitItemMaterial(packetItemStack.type)

    override val itemStack: BukkitStack
        get() = abortReads()

    override val namespace: String
        get() = key.namespace()

    override val path: String
        get() = key.value()

    override var variant: Int
        get() = 0
        set(_) = abortWrites()

    override val slotGroup: ItemSlotGroup
        get() = prototype.slotGroup

    override val templates: ItemTemplateMap
        get() = prototype.templates

    override val behaviors: ItemBehaviorMap
        get() = prototype.behaviors

    override val unsafe: NekoStack.Unsafe
        get() = abortReads()

    override fun clone(): NekoStack =
        abortReads()

    override fun erase() {
        // NOP: 本来就是原版物品, 不需要擦除 `custom_data`
    }

    private fun abortReads(): Nothing {
        throw UnsupportedOperationException("Read operation is not allowed in PacketVanillaNekoStack")
    }

    private fun abortWrites(): Nothing {
        throw UnsupportedOperationException("Write operation is not allowed in PacketVanillaNekoStack")
    }
}
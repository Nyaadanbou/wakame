package cc.mewcraft.wakame.packet

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.SharedConstants
import cc.mewcraft.wakame.item.*
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.component.ItemComponentMaps
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.util.unsafeNyaTagOrThrow
import com.github.retrooper.packetevents.protocol.attribute.AttributeOperation
import com.github.retrooper.packetevents.protocol.attribute.Attributes
import com.github.retrooper.packetevents.protocol.component.ComponentType
import com.github.retrooper.packetevents.protocol.component.ComponentTypes
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemAttributeModifiers
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore
import com.github.retrooper.packetevents.protocol.item.ItemStack
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound
import com.github.retrooper.packetevents.resources.ResourceLocation
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack as BukkitStack

internal val ItemStack.id
    get() = type.name.let { location -> Key.key(location.namespace, location.key) }

internal fun <T : Any> ItemStack.get(type: ComponentType<T>): T? {
    return getComponent(type).orElse(null)
}

internal fun <T : Any> ItemStack.set(type: ComponentType<T>, value: T) {
    setComponent(type, value)
}

internal fun <T : Any> ItemStack.update(type: ComponentType<T>, block: (T) -> T) {
    val oldData = get(type) ?: return
    val newData = block(oldData)
    set(type, newData)
}

internal fun ItemStack.unset(type: ComponentType<*>) {
    unsetComponent(type)
}

internal val ItemStack.customData: NBTCompound?
    get() {
        if (!hasComponentPatches())
            return null
        return components.get(ComponentTypes.CUSTOM_DATA)
    }

internal val ItemStack.nekooTag: NBTCompound?
    get() {
        return customData?.getCompoundTagOrNull(SharedConstants.PLUGIN_NAME)
    }

internal val ItemStack.isNeko: Boolean
    get() {
        return nekooTag != null || VanillaNekoStackRegistry.has(id)
    }

internal val ItemStack.isCustomNeko: Boolean
    get() {
        return nekooTag != null
    }

internal val ItemStack.isVanillaNeko: Boolean
    get() {
        return !isCustomNeko && VanillaNekoStackRegistry.has(id)
    }

internal val ItemStack.isClientSide: Boolean
    get() = customData?.getTagOrNull(NekoStack.CLIENT_SIDE_KEY) == null

/**
 * 尝试将 ItemStack 转换为 NekoStack, 用作渲染.
 * 返回 `null` 则说明该物品堆叠不需要被发包渲染接管.
 */
internal val ItemStack.tryNekoStack: PacketNekoStack?
    get() {
        if (isEmpty || !isClientSide) {
            return null
        }
        if (isCustomNeko) {
            return PacketCustomNekoStack(this)
        }
        val nekoStack = VanillaNekoStackRegistry.get(id)
        if (nekoStack != null) {
            return PacketVanillaNekoStack(this, nekoStack)
        }
        return null
    }

// 开发日记 2024/7/11
// 发包还需要修改原版物品, 因此底层的实现会有不同.
internal interface PacketNekoStack : NekoStack {
    /**
     * 该成员仅用于直接构建 [com.github.retrooper.packetevents.wrapper.PacketWrapper].
     */
    val packetItem: ItemStack

    /**
     * 设置自定义名称. 您可以传递 `null` 来移除名称.
     */
    fun customName(value: Component?) {
        if (value != null) {
            packetItem.set(ComponentTypes.CUSTOM_NAME, value)
        } else {
            packetItem.unset(ComponentTypes.CUSTOM_NAME)
        }
    }

    /**
     * 设置物品名称. 您可以传递 `null` 来移除它.
     */
    fun itemName(value: Component?) {
        if (value != null) {
            packetItem.set(ComponentTypes.ITEM_NAME, value)
        } else {
            packetItem.unset(ComponentTypes.ITEM_NAME)
        }
    }

    /**
     * 设置物品描述. 您可以传递 `null` 来移除它.
     */
    fun lore(value: List<Component>?) {
        if (value != null) {
            packetItem.set(ComponentTypes.LORE, ItemLore(value))
        } else {
            packetItem.unset(ComponentTypes.LORE)
        }
    }

    /**
     * 设置自定义模型数据. 您可以传递 `null` 来移除它.
     */
    fun customModelData(value: Int?) {
        if (value != null) {
            packetItem.set(ComponentTypes.CUSTOM_MODEL_DATA, value)
        } else {
            packetItem.unset(ComponentTypes.CUSTOM_MODEL_DATA)
        }
    }

    //<editor-fold desc="Show In Tooltip">
    fun showAttributeModifiers(value: Boolean) {
        // Mojang sucks, packetevents sucks too.
        // The line of code below doesn't work simply
        // because packetevents creates invalid packets
        // for the attribute modifier entries.
        // packetItem.update(ComponentTypes.ATTRIBUTE_MODIFIERS) { ItemAttributeModifiers(it.modifiers, value) }

        packetItem.update(ComponentTypes.ATTRIBUTE_MODIFIERS) { EMPTY_ATTRIBUTE_MODIFIERS }
    }

    fun showCanBreak(value: Boolean) {
        packetItem.get(ComponentTypes.CAN_BREAK)?.isShowInTooltip = value
    }

    fun showCanPlaceOn(value: Boolean) {
        packetItem.get(ComponentTypes.CAN_PLACE_ON)?.isShowInTooltip = value
    }

    fun showDyedColor(value: Boolean) {
        packetItem.get(ComponentTypes.DYED_COLOR)?.isShowInTooltip = value
    }

    fun showEnchantments(value: Boolean) {
        packetItem.get(ComponentTypes.ENCHANTMENTS)?.takeIf { !it.isEmpty }?.isShowInTooltip = value
    }

    fun showJukeboxPlayable(value: Boolean) {
        packetItem.get(ComponentTypes.JUKEBOX_PLAYABLE)?.isShowInTooltip = value
    }

    fun showStoredEnchantments(value: Boolean) {
        packetItem.get(ComponentTypes.STORED_ENCHANTMENTS)?.isShowInTooltip = value
    }

    fun showTrim(value: Boolean) {
        packetItem.get(ComponentTypes.TRIM)?.isShowInTooltip = value
    }

    fun showUnbreakable(value: Boolean) {
        // FIXME packetevents 不支持设置 `minecraft:unbreakable` 的 `show_in_tooltip`
    }
    //</editor-fold>

    companion object Constants {
        private val DUMMY_ATTRIBUTE_MODIFIER = ItemAttributeModifiers.Modifier(ResourceLocation("wakame:dummy"), .0, AttributeOperation.ADDITION)
        private val DUMMY_ATTRIBUTE_MODIFIER_ENTRY = ItemAttributeModifiers.ModifierEntry(Attributes.GENERIC_LUCK, DUMMY_ATTRIBUTE_MODIFIER, ItemAttributeModifiers.EquipmentSlotGroup.ANY)
        private val EMPTY_ATTRIBUTE_MODIFIERS = ItemAttributeModifiers(emptyList(), false)
    }
}

// 开发日记:
// 该 NekoStack 仅用于物品发包系统内部.
private class PacketCustomNekoStack(
    override val packetItem: ItemStack,
) : PacketNekoStack {
    // 开发日记:
    // 由于 ItemComponentMap 对 BukkitStack 有直接依赖, 我们需要转换一个
    private val handle: BukkitStack = SpigotConversionUtil.toBukkitItemStack(packetItem)

    // 开发日记1: We use property initializer here as it would be called multiple times,
    // and we don't want to do the unnecessary NBT conversion again and again
    // 开发日记2: 该 NBT 标签应该只接受读操作 (虽然可以写, 但不保证生效, 也没啥用应该)
    private val nyaTag: CompoundTag = handle.unsafeNyaTagOrThrow

    override val isEmpty: Boolean
        get() = false

    override var isClientSide: Boolean
        get() = abortReads() // 实际不会调用这里, 而是外部直接读取
        set(_) = abortWrites()

    override val itemType: Material
        get() = handle.type

    override val itemStack: BukkitStack
        get() = abortReads()
    override val wrapped: BukkitStack
        get() = abortReads()

    override val id: Key
        get() = NekoStackSupport.getIdOrThrow(nyaTag)

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
        // 网络发包是物品渲染的最后一环,
        // 不会再有其他系统读取这个物品的自定义数据,
        // 所以可以直接移除整个 `custom_data`.
        packetItem.unset(ComponentTypes.CUSTOM_DATA)
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
    }
}

private class PacketVanillaNekoStack(
    override val packetItem: ItemStack,
    private val nekoStack: VanillaNekoStack, // 已注册的原版实例
) : PacketNekoStack {
    private val handle: BukkitStack = SpigotConversionUtil.toBukkitItemStack(packetItem)

    override val isEmpty: Boolean
        get() = false

    override var isClientSide: Boolean
        // 我们无法修改原版物品, 但又必须要渲染原版物品,
        // 所以这里必须返回 true, 发包渲染系统才能接管.
        get() = true
        set(_) = abortWrites()

    override val itemType: Material
        get() = SpigotConversionUtil.toBukkitItemMaterial(packetItem.type)

    override val itemStack: BukkitStack
        get() = abortReads()
    override val wrapped: BukkitStack
        get() = abortReads()

    override val id: Key
        get() = nekoStack.id

    override var variant: Int
        get() = 0
        set(_) = abortWrites()

    override val slotGroup: ItemSlotGroup
        get() = nekoStack.slotGroup

    override val prototype: NekoItem
        get() = nekoStack.prototype

    // 虽然这是一个NBT (custom_data) 未经修改的原版物品,
    // 但其仍然会包含一些原版的物品组件, 例如魔咒, 耐久度.
    override val components: ItemComponentMap
        get() {
            val base = nekoStack.components
            val patch = ItemComponentMaps.wrapStack(handle)
            return ItemComponentMaps.composite(base, patch)
        }

    override val templates: ItemTemplateMap
        get() = nekoStack.templates

    override val behaviors: ItemBehaviorMap
        get() = nekoStack.behaviors

    override val unsafe: NekoStack.Unsafe
        get() = abortReads()

    override fun clone(): NekoStack {
        abortReads()
    }

    override fun erase() {
        // 本来就是原版物品, 不需要擦除 `custom_data`
    }

    private fun abortReads(): Nothing {
        throw UnsupportedOperationException("Read operation is not allowed in PacketVanillaNekoStack")
    }

    private fun abortWrites(): Nothing {
        throw UnsupportedOperationException("Write operation is not allowed in PacketVanillaNekoStack")
    }
}
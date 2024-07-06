package cc.mewcraft.wakame.item

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.backingCustomModelData
import cc.mewcraft.wakame.util.backingCustomName
import cc.mewcraft.wakame.util.backingItemName
import cc.mewcraft.wakame.util.backingLore
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.isNms
import cc.mewcraft.wakame.util.removeWakameTag
import cc.mewcraft.wakame.util.toSimpleString
import cc.mewcraft.wakame.util.wakameTag
import cc.mewcraft.wakame.util.wakameTagOrNull
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import java.util.UUID
import java.util.stream.Stream

@get:Contract(pure = true)
val ItemStack.isNeko: Boolean
    get() {
        val tag = this.wakameTagOrNull ?: return false
        return NekoStackSupport.getPrototype(tag) != null
    }

@get:Contract(pure = false)
val ItemStack.tryNekoStack: NekoStack?
    get() {
        // 开发日记 2024/6/26 小米
        // 看 CraftBukkit 的实现, 发现调用 hasItemMeta 时,
        // 如果这个物品是 NMS, 则会产生一个 copy, 非常慢!
        // 而且似乎这里也不需要判断.
        // if (!this.hasItemMeta())
        //     return null
        if (!this.isNms)
            return null
        if (!this.isNeko)
            return null
        if (NekoStackSupport.isSystemUse(this.wakameTag))
            return null
        return NekoStackImpl(this)
    }

@get:Contract(pure = false)
val ItemStack.toNekoStack: NekoStack
    get() {
        // 开发日记 2024/6/26 小米
        // 同 tryNekoStack 里所说的.
        // require(this.hasItemMeta()) {
        //     "The ItemStack has no ItemMeta"
        // }
        require(this.isNms) {
            "The ItemStack is not an NMS object"
        }
        require(this.isNeko) {
            "The ItemStack is not from wakame"
        }
        require(!NekoStackSupport.isSystemUse(this.wakameTag)) {
            "The ItemStack is not to be used by players"
        }
        return NekoStackImpl(this)
    }

@get:Contract(pure = true)
val ItemStack.trySystemStack: NekoStack?
    get() {
        // if (!this.hasItemMeta())
        //     return null
        if (!this.isNeko)
            return null
        return NekoStackImpl(this.clone() /* 副本 */).takeIf {
            it.isSystemUse()
        }
    }

@get:Contract(pure = true)
val ItemStack.toSystemStack: NekoStack
    get() {
        // require(this.hasItemMeta()) {
        //     "The ItemStack has no ItemMeta"
        // }
        require(this.isNeko) {
            "The ItemStack is not from wakame"
        }
        return NekoStackImpl(this.clone() /* 副本 */).apply {
            require(this.isSystemUse()) { "The ItemStack is not a system stack" }
        }
    }

/**
 * This function is meant to be used to create a new [NekoStack]
 * **from scratch** which will ultimately be added to the world state,
 * such as adding it to a player's inventory.
 *
 * ## Caution!
 *
 * It is the caller's responsibility to modify the returned [NekoStack]
 * before it's added to the world state so that it becomes a legal NekoItem.
 * Otherwise, undefined behaviors can occur.
 */
@Contract(pure = true)
internal fun Material.createNekoStack(): NekoStack {
    return NekoStackImpl(this)
}

@Contract(pure = true)
fun NekoStack.isSystemUse(): Boolean {
    return components.has(ItemComponentTypes.SYSTEM_USE)
}

@Contract(pure = false)
private fun NekoStack.setSystemUse() {
    components.set(ItemComponentTypes.SYSTEM_USE, Unit)
}

@Contract(pure = false)
private fun NekoStack.unsetSystemUse() {
    this.handle.backingItemName = null
    this.handle.backingCustomName = null
    this.handle.backingCustomModelData = null
    this.handle.backingLore = null

    components.unset(ItemComponentTypes.SYSTEM_USE)
}

@Contract(pure = true)
internal fun NekoStack.toSystemUse(): NekoStack {
    val clone: NekoStack = NekoStackImpl(this.handle.clone())
    clone.setSystemUse()
    return clone
}

@Contract(pure = true)
internal fun NekoStack.toNonSystemUse(): NekoStack {
    val clone: NekoStack = NekoStackImpl(this.handle.clone())
    clone.unsetSystemUse()
    return clone
}

/**
 * An implementation of [NekoStack].
 */
private class NekoStackImpl(
    override val handle: ItemStack,
) : NekoStack {

    // 该构造器接受一个 ItemType, 用于从零开始创建一个物品
    constructor(mat: Material) : this(
        handle = ItemStack(mat), // strictly-Bukkit ItemStack
    )

    override val nbt: CompoundTag
        get() {
            // 开发日记 2024/6/26
            // TODO 等到 Paper 的 delegate ItemStack 完成之后,
            // 就不需要区分一个 ItemStack 是不是 NMS 了.
            // 到时候这个 if-clause 直接删掉就行.
            if (!handle.isNms) {
                // If this is a strictly-Bukkit ItemStack:

                // the `wakame` compound should always be available (if not, create it)
                // as we need to create a NekoItem realization from an empty ItemStack.
                return handle.wakameTag
            }

            // If this is a NMS-backed ItemStack:

            // reading/modifying is allowed only if it already has a `wakame` compound.
            // We explicitly prohibit modifying the ItemStacks, which are not already
            // NekoItem realization, in the world state because we want to avoid
            // undefined behaviors. Just imagine that a random code modifies a
            // vanilla item and make it an incomplete realization of NekoItem.
            return handle.wakameTagOrNull ?: throw NullPointerException(
                "Can't read/modify the NBT of NMS-backed ItemStack which is not NekoStack"
            )
        }

    override val prototype: NekoItem
        get() = NekoStackSupport.getPrototypeOrThrow(nbt)

    override var namespace: String
        get() = NekoStackSupport.getNamespaceOrThrow(nbt)
        set(value) = NekoStackSupport.setNamespace(nbt, value)

    override var path: String
        get() = NekoStackSupport.getPathOrThrow(nbt)
        set(value) = NekoStackSupport.setPath(nbt, value)

    override var key: Key
        get() = NekoStackSupport.getKeyOrThrow(nbt)
        set(value) = NekoStackSupport.setKey(nbt, value)

    override var variant: Int
        get() = NekoStackSupport.getVariant(nbt)
        set(value) = NekoStackSupport.setVariant(nbt, value)

    override val uuid: UUID
        get() = NekoStackSupport.getUuid(nbt)

    override val slot: ItemSlot
        get() = NekoStackSupport.getSlot(nbt)

    override val components: ItemComponentMap
        get() = NekoStackSupport.getComponents(handle)

    override val templates: ItemTemplateMap
        get() = NekoStackSupport.getTemplates(nbt)

    override val behaviors: ItemBehaviorMap
        get() = NekoStackSupport.getBehaviors(nbt)

    override fun erase() {
        handle.removeWakameTag()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("namespace", namespace),
        ExaminableProperty.of("path", path),
        ExaminableProperty.of("variant", variant),
        ExaminableProperty.of("slot", slot),
        ExaminableProperty.of("nbt", nbt),
        ExaminableProperty.of("handle", handle),
    )

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * Common implementations related to [NekoStack].
 */
internal object NekoStackSupport {
    fun isSystemUse(wakameTag: CompoundTag): Boolean {
        return wakameTag.getCompoundOrNull(ItemComponentMap.TAG_COMPONENTS)
            ?.contains(ItemComponentTypes.SYSTEM_USE.id)
            ?: false
    }

    fun getNamespace(wakameTag: CompoundTag): String? {
        return wakameTag.getString(BaseBinaryKeys.NAMESPACE)
    }

    fun getNamespaceOrThrow(wakameTag: CompoundTag): String {
        return requireNotNull(getNamespace(wakameTag)) { "Can't find 'namespace' on item NBT" }
    }

    fun getPath(wakameTag: CompoundTag): String? {
        return wakameTag.getString(BaseBinaryKeys.PATH)
    }

    fun getPathOrThrow(wakameTag: CompoundTag): String {
        return requireNotNull(getPath(wakameTag)) { "Can't find 'path' on item NBT" }
    }

    fun getKey(wakameTag: CompoundTag): Key? {
        return getNamespace(wakameTag)?.let { namespace -> getPath(wakameTag)?.let { path -> Key(namespace, path) } }
    }

    fun getKeyOrThrow(wakameTag: CompoundTag): Key {
        return requireNotNull(getKey(wakameTag)) { "Can' find 'key' on item NBT" }
    }

    fun getVariant(wakameTag: CompoundTag): Int {
        return wakameTag.getInt(BaseBinaryKeys.VARIANT) // 如果不存在 NBT 标签, 默认返回 0
    }

    fun getUuid(wakameTag: CompoundTag): UUID {
        val prototype = getPrototypeOrThrow(wakameTag)
        return prototype.uuid
    }

    fun getSlot(wakameTag: CompoundTag): ItemSlot {
        val prototype = getPrototypeOrThrow(wakameTag)
        return prototype.slot
    }

    fun getPrototype(wakameTag: CompoundTag): NekoItem? {
        val key = getKeyOrThrow(wakameTag)
        val prototype = ItemRegistry.INSTANCES.find(key)
        return prototype
    }

    fun getPrototypeOrThrow(wakameTag: CompoundTag): NekoItem {
        val key = getKeyOrThrow(wakameTag)
        val prototype = requireNotNull(ItemRegistry.INSTANCES.find(key)) { "Can't find a prototype by '$key'" }
        return prototype
    }

    fun getComponents(stack: ItemStack): ItemComponentMap {
        return ItemComponentMap.wrapStack(stack)
    }

    fun getImmutableComponents(stack: ItemStack): ItemComponentMap {
        return ItemComponentMap.unmodifiable(getComponents(stack))
    }

    fun getTemplates(wakameTag: CompoundTag): ItemTemplateMap {
        val prototype = getPrototypeOrThrow(wakameTag)
        return prototype.templates
    }

    fun getBehaviors(wakameTag: CompoundTag): ItemBehaviorMap {
        val prototype = getPrototypeOrThrow(wakameTag)
        return prototype.behaviors
    }

    fun setNamespace(wakameTag: CompoundTag, namespace: String) {
        wakameTag.putString(BaseBinaryKeys.NAMESPACE, namespace)
    }

    fun setPath(wakameTag: CompoundTag, path: String) {
        wakameTag.putString(BaseBinaryKeys.PATH, path)
    }

    fun setKey(wakameTag: CompoundTag, key: Key) {
        setNamespace(wakameTag, key.namespace())
        setPath(wakameTag, key.value())
    }

    fun setVariant(wakameTag: CompoundTag, variant: Int) {
        wakameTag.putInt(BaseBinaryKeys.VARIANT, variant)
    }
}

private object NekoStackInjections : KoinComponent {
    val logger: Logger by inject()
}

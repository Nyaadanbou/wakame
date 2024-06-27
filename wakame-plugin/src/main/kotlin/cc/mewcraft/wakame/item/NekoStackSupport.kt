package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.backingCustomModelData
import cc.mewcraft.wakame.util.backingCustomName
import cc.mewcraft.wakame.util.backingItemName
import cc.mewcraft.wakame.util.backingLore
import cc.mewcraft.wakame.util.isNms
import cc.mewcraft.wakame.util.removeWakameTag
import cc.mewcraft.wakame.util.wakameTag
import cc.mewcraft.wakame.util.wakameTagOrNull
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import java.util.UUID

val ItemStack.isNeko: Boolean
    get() {
        val tag = wakameTagOrNull ?: return false
        val key = NekoStackImplementations.getKey(tag) ?: return false
        return ItemRegistry.INSTANCES.find(key) != null
    }

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
        if (NekoStackImplementations.isSystemUse(this.wakameTag))
            return null
        return NekoStackImpl(this)
    }

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
            "The itemStack is not from wakame"
        }
        require(NekoStackImplementations.isSystemUse(this.wakameTag)) {
            "The ItemStack is not a play NekoStack"
        }
        return NekoStackImpl(this)
    }

internal val ItemStack.trySystemStack: NekoStack?
    get() {
        // if (!this.hasItemMeta())
        //     return null
        if (!this.isNeko)
            return null
        return NekoStackImpl(this.clone()).takeIf {
            it.isSystemUse()
        }
    }

internal val ItemStack.toSystemStack: NekoStack
    get() {
        // require(this.hasItemMeta()) {
        //     "The ItemStack has no ItemMeta"
        // }
        require(this.isNeko) {
            "The itemStack is not from wakame"
        }
        return NekoStackImpl(this.clone()).apply {
            require(this.isSystemUse()) { "" }
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
internal fun Material.createNekoStack(): NekoStack {
    return NekoStackImpl(this)
}

internal fun NekoStack.isSystemUse(): Boolean {
    return components.has(ItemComponentTypes.SYSTEM_USE)
}

internal fun NekoStack.setSystemUse() {
    components.set(ItemComponentTypes.SYSTEM_USE, Unit)
}

internal fun NekoStack.unsetSystemUse() {
    this.handle.backingItemName = null
    this.handle.backingCustomName = null
    this.handle.backingCustomModelData = null
    this.handle.backingLore = null

    components.unset(ItemComponentTypes.SYSTEM_USE)
}

@Contract(pure = true)
internal fun NekoStack.copyToSystemUse(): NekoStack {
    if (this.isSystemUse()) {
        return this
    }

    val clone: NekoStack = NekoStackImpl(this.handle.clone())
    clone.setSystemUse()
    return clone
}

@Contract(pure = true)
internal fun NekoStack.copyToNonSystemUse(): NekoStack {
    if (!this.isSystemUse()) {
        return this
    }

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

    override val nbt: CompoundShadowTag
        get() {
            // 开发日记 2024/6/26
            // 等到 Paper 的 delegate ItemStack 完成之后,
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
        get() = ItemRegistry.INSTANCES[key]

    override var namespace: String
        get() = requireNotNull(NekoStackImplementations.getNamespace(nbt)) { "Can't find 'namespace' on the item NBT" }
        set(value) = NekoStackImplementations.setNamespace(nbt, value)

    override var path: String
        get() = requireNotNull(NekoStackImplementations.getPath(nbt)) { "Can't find 'path' on the item NBT" }
        set(value) = NekoStackImplementations.setPath(nbt, value)

    override var key: Key
        get() = requireNotNull(NekoStackImplementations.getKey(nbt)) { "Can't find 'key' on the item NBT" }
        set(value) = NekoStackImplementations.setKey(nbt, value)

    override var variant: Int
        get() = nbt.getInt(BaseBinaryKeys.VARIANT)
        set(value) = nbt.putInt(BaseBinaryKeys.VARIANT, value)

    override val uuid: UUID
        get() = ItemRegistry.INSTANCES[key].uuid

    override val slot: ItemSlot
        get() = ItemRegistry.INSTANCES[key].slot

    override val components: ItemComponentMap
        get() = ItemComponentMap.wrapItem(handle)

    override val templates: ItemTemplateMap
        get() = prototype.templates

    override fun erase() {
        handle.removeWakameTag()
    }
}

/**
 * Common implementations related to [NekoStack].
 */
private object NekoStackImplementations {
    fun isSystemUse(nekoCompound: CompoundShadowTag): Boolean {
        return nekoCompound.contains(ItemComponentTypes.SYSTEM_USE.id)
    }

    fun getNamespace(nekoCompound: CompoundShadowTag): String? {
        return nekoCompound.getString(BaseBinaryKeys.NAMESPACE)
    }

    fun getPath(nekoCompound: CompoundShadowTag): String? {
        return nekoCompound.getString(BaseBinaryKeys.PATH)
    }

    fun getKey(nekoCompound: CompoundShadowTag): Key? {
        return getNamespace(nekoCompound)?.let { namespace -> getPath(nekoCompound)?.let { path -> Key(namespace, path) } }
    }

    fun setNamespace(nekoCompound: CompoundShadowTag, namespace: String) {
        nekoCompound.putString(BaseBinaryKeys.NAMESPACE, namespace)
    }

    fun setPath(nekoCompound: CompoundShadowTag, path: String) {
        nekoCompound.putString(BaseBinaryKeys.PATH, path)
    }

    fun setKey(nekoCompound: CompoundShadowTag, key: Key) {
        setNamespace(nekoCompound, key.namespace())
        setPath(nekoCompound, key.value())
    }
}

private object NekoStackInjections : KoinComponent {
    val logger: Logger by inject()
}

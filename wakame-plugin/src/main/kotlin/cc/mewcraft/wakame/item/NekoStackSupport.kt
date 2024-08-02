package cc.mewcraft.wakame.item

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.ReloadDependency
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
import cc.mewcraft.wakame.util.takeUnlessEmpty
import cc.mewcraft.wakame.util.toSimpleString
import cc.mewcraft.wakame.util.wakameTag
import cc.mewcraft.wakame.util.wakameTagOrNull
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import java.util.stream.Stream

/**
 * 检查一个物品能否算作 [NekoStack].
 */
@get:Contract(pure = true)
val ItemStack.isNeko: Boolean
    get() {
        val tag = this.wakameTagOrNull
        if (tag != null) {
            val prototype = NekoStackSupport.getPrototype(tag)
            if (prototype != null) {
                return true
            }
        }
        return VanillaNekoStackRegistry.has(this.type)
    }

/**
 * 检查一个物品是否算作 [CustomNekoStack].
 */
@get:Contract(pure = true)
val ItemStack.isCustomNeko: Boolean
    get() {
        val tag = this.wakameTagOrNull ?: return false
        val prototype = NekoStackSupport.getPrototype(tag)
        return prototype != null
    }

/**
 * 检查一个物品是否算作 [VanillaNekoStack]
 */
@get:Contract(pure = true)
val ItemStack.isVanillaNeko: Boolean
    get() {
        if (this.wakameTagOrNull != null) {
            return false
        }
        return VanillaNekoStackRegistry.has(this.type)
    }

@get:Contract(pure = false)
val ItemStack.tryNekoStack: NekoStack?
    get() {
        val tag = this.wakameTagOrNull
        if (tag != null) {
            val prototype = NekoStackSupport.getPrototype(tag)
            if (prototype != null) {
                if (!NekoStackSupport.isSystemUse(tag)) {
                    return CustomNekoStack(this)
                }
            }
        }
        return VanillaNekoStackRegistry.get(this.type)
    }

@get:Contract(pure = false)
val ItemStack.toNekoStack: NekoStack
    get() {
        return requireNotNull(this.tryNekoStack) {
            "The ItemStack is not a NekoStack"
        }
    }

@get:Contract(pure = true)
val ItemStack.trySystemStack: NekoStack?
    get() {
        if (!this.isCustomNeko) {
            return null
        }
        return CustomNekoStack(this.clone()).takeIf {
            it.isSystemUse()
        }
    }

@get:Contract(pure = true)
val ItemStack.toSystemStack: NekoStack
    get() {
        require(this.isCustomNeko) {
            "The ItemStack is not a CustomNekoStack"
        }
        return CustomNekoStack(this.clone()).apply {
            require(this.isSystemUse()) { "The ItemStack is not of system-use" }
        }
    }

fun ItemStack.takeIfNekoStack(): ItemStack? {
    return this.takeUnlessEmpty()?.takeIf { it.tryNekoStack == null }
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
    return CustomNekoStack(this)
}

@Contract(pure = true)
fun NekoStack.isSystemUse(): Boolean {
    return components.has(ItemComponentTypes.SYSTEM_USE)
}

@Contract(pure = false)
fun NekoStack.setSystemUse() {
    components.set(ItemComponentTypes.SYSTEM_USE, Unit)
}

@Contract(pure = false)
fun NekoStack.unsetSystemUse() {
    this.handle.backingItemName = null
    this.handle.backingCustomName = null
    this.handle.backingCustomModelData = null
    this.handle.backingLore = null

    components.unset(ItemComponentTypes.SYSTEM_USE)
}

/**
 * 一个标准的 [NekoStack] 实现.
 *
 * 底层物品必须拥有 `minecraft:custom_data` 组件, 并且其中存在 `wakame` 的复合标签.
 *
 * 该实现是可变的, 也就是说可以修改其中的属性.
 */
private class CustomNekoStack(
    override val handle: ItemStack,
) : NekoStack {

    // 该构造器接受一个 ItemType, 用于从零开始创建一个物品
    constructor(mat: Material) : this(
        handle = ItemStack(mat), // strictly-Bukkit ItemStack
    )

    override val nbt: CompoundTag
        get() {
            // TODO 等到 Paper 的 delegate ItemStack 完成之后,
            //  就不需要区分一个 ItemStack 是不是 NMS 了.
            //  到时候这个 if-clause 直接删掉就行.
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

    override val itemStack: ItemStack
        get() {
            // 由于我们是*直接*对 minecraft:custom_data 中的 CompoundTag
            // 进行读写操作, 而没有调用 minecraft:custom_data 的 copyTag(),
            // 因此必须显式的对 NBT 进行拷贝, 否则会出现引用问题!
            val newHandle = handle.clone()
            val wakameTagCopy = (newHandle.wakameTag.copy() as CompoundTag)
            newHandle.wakameTag = wakameTagCopy
            return newHandle
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

    override val slotGroup: ItemSlotGroup
        get() = NekoStackSupport.getSlotGroup(nbt)

    override val components: ItemComponentMap
        get() = NekoStackSupport.getComponents(handle)

    override val templates: ItemTemplateMap
        get() = NekoStackSupport.getTemplates(nbt)

    override val behaviors: ItemBehaviorMap
        get() = NekoStackSupport.getBehaviors(nbt)

    override fun clone(): NekoStack {
        return CustomNekoStack(itemStack)
    }

    override fun erase() {
        handle.removeWakameTag()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key.asString()),
        ExaminableProperty.of("variant", variant),
    )

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * 一个特殊的 [NekoStack] 实现, 代表一个 Minecraft 原版物品的萌芽版本.
 *
 * 仅用于封装原版物品的*类型*, 以便让原版物品拥有默认的萌芽特性.
 *
 * 该实现是不可变的, 也就是说不能修改其中的任何属性.
 */
internal class VanillaNekoStack(
    override val key: Key,
    override val prototype: NekoItem,
    override val components: ItemComponentMap,
) : NekoStack {
    override val nbt: CompoundTag
        get() = unsupported() // 由于本实现完全不可变, 因此不需要封装 NBT.
    override val handle: ItemStack
        get() = unsupported()
    override val itemStack: ItemStack
        get() = unsupported()

    override val namespace: String = key.namespace()
    override val path: String = key.value()

    override var variant: Int = 0 // 变体永远都是 0
    override val slotGroup: ItemSlotGroup = prototype.slotGroup
    override val templates: ItemTemplateMap = prototype.templates
    override val behaviors: ItemBehaviorMap = prototype.behaviors

    override fun clone(): NekoStack =
        unsupported()

    override fun erase(): Unit =
        unsupported()

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key.asString()),
        ExaminableProperty.of("variant", variant),
    )

    override fun toString(): String = toSimpleString()

    private fun unsupported(): Nothing {
        throw UnsupportedOperationException("This operation is not supported on VanillaNekoStack")
    }
}

@ReloadDependency(
    runBefore = [ItemRegistry::class],
)
internal object VanillaNekoStackRegistry : Initializable, KoinComponent {
    private val realizer: VanillaNekoItemRealizer by inject()
    private val registry: Object2ObjectOpenHashMap<Key, VanillaNekoStack> = Object2ObjectOpenHashMap()

    fun has(material: Material): Boolean {
        return has(material.key())
    }

    fun has(key: Key): Boolean {
        return registry.containsKey(key)
    }

    fun get(material: Material): VanillaNekoStack? {
        return get(material.key())
    }

    fun get(key: Key): VanillaNekoStack? {
        return registry[key]
    }

    fun register(key: Key, stack: VanillaNekoStack) {
        registry[key] = stack
    }

    override fun onPostWorld() {
        realizeAndRegister()
    }

    override fun onReload() {
        realizeAndRegister()
    }

    private fun realizeAndRegister() {
        registry.clear()
        for ((key, prototype) in ItemRegistry.VANILLA) {
            val stack = realizer.realize(prototype)
            register(key, stack)
        }
    }
}

/**
 * Common implementations related to [NekoStack].
 */
internal object NekoStackSupport {
    fun isSystemUse(wakameTag: CompoundTag): Boolean {
        return wakameTag.getCompoundOrNull(ItemComponentMap.TAG_COMPONENTS)?.contains(ItemComponentConstants.SYSTEM_USE) ?: false
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

    fun getSlotGroup(wakameTag: CompoundTag): ItemSlotGroup {
        val prototype = getPrototypeOrThrow(wakameTag)
        return prototype.slotGroup
    }

    fun getPrototype(wakameTag: CompoundTag): NekoItem? {
        val key = getKeyOrThrow(wakameTag)
        val prototype = ItemRegistry.CUSTOM.find(key)
        return prototype
    }

    fun getPrototypeOrThrow(wakameTag: CompoundTag): NekoItem {
        val key = getKeyOrThrow(wakameTag)
        val prototype = requireNotNull(ItemRegistry.CUSTOM.find(key)) { "Can't find a prototype by '$key'" }
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
        if (variant == 0) {
            // 如果不存在 NBT 标签, 默认返回 0
            wakameTag.remove(BaseBinaryKeys.VARIANT)
            return
        }
        wakameTag.putInt(BaseBinaryKeys.VARIANT, variant)
    }
}

private object NekoStackInjections : KoinComponent {
    val logger: Logger by inject()
}

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
import cc.mewcraft.wakame.util.editNyaTag
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.nyaTag
import cc.mewcraft.wakame.util.takeUnlessEmpty
import cc.mewcraft.wakame.util.toSimpleString
import cc.mewcraft.wakame.util.unsafeNyaTag
import cc.mewcraft.wakame.util.unsafeNyaTagOrThrow
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.stream.Stream

/**
 * 检查一个物品能否算作 [NekoStack].
 */
@get:Contract(pure = true)
val ItemStack.isNeko: Boolean
    get() {
        val tag = this.unsafeNyaTag
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
        val tag = this.unsafeNyaTag ?: return false
        val prototype = NekoStackSupport.getPrototype(tag)
        return prototype != null
    }

/**
 * 检查一个物品是否算作 [VanillaNekoStack]
 */
@get:Contract(pure = true)
val ItemStack.isVanillaNeko: Boolean
    get() {
        if (this.unsafeNyaTag != null) {
            return false
        }
        return VanillaNekoStackRegistry.has(this.type)
    }

/**
 * 尝试将 [ItemStack] 转换为一个 [NekoStack].
 * 如果无法完成转换, 则返回 `null`.
 */
@get:Contract(pure = false)
val ItemStack.tryNekoStack: NekoStack?
    get() {
        val tag = this.unsafeNyaTag
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

/**
 * 将 [ItemStack] 转换为一个 [NekoStack].
 * 如果无法完成转换, 则抛出异常.
 *
 * @throws IllegalArgumentException
 */
@get:Contract(pure = false)
val ItemStack.toNekoStack: NekoStack
    get() {
        return requireNotNull(this.tryNekoStack) { "The ItemStack is not a NekoStack" }
    }

@Deprecated("Use ItemStack#tryNekoStack instead", ReplaceWith("this.tryNekoStack"))
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

@Deprecated("Use ItemStack#toNekoStack instead", ReplaceWith("this.toNekoStack"))
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

/**
 * 如果 [ItemStack] 是一个萌芽物品, 则返回对象本身. 否则返回 `null`.
 */
@Contract(pure = true)
fun ItemStack.takeIfNeko(): ItemStack? {
    return this.takeUnlessEmpty()?.takeIf { it.isNeko }
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
fun NekoStack.bypassPacket(): Boolean {
    return components.has(ItemComponentTypes.SYSTEM_USE)
}

@Contract(pure = false)
fun NekoStack.bypassPacket(bypass: Boolean) {
    if (bypass) {
        components.set(ItemComponentTypes.SYSTEM_USE, Unit)
    } else {
        val handle = this.unsafe.handle

        handle.backingItemName = null
        handle.backingCustomName = null
        handle.backingCustomModelData = null
        handle.backingLore = null

        components.unset(ItemComponentTypes.SYSTEM_USE)
    }
}

@Deprecated("Use NekoStack#bypassPacket instead", ReplaceWith("this.bypassPacket()"))
@Contract(pure = true)
fun NekoStack.isSystemUse(): Boolean {
    return bypassPacket()
}

@Deprecated("Use NekoStack#bypassPacket instead", ReplaceWith("this.bypassPacket(bypass)"))
@Contract(pure = false)
fun NekoStack.setSystemUse() {
    bypassPacket(true)
}

@Deprecated("Use NekoStack#bypassPacket instead", ReplaceWith("this.bypassPacket(bypass)"))
@Contract(pure = false)
fun NekoStack.unsetSystemUse() {
    bypassPacket(false)
}

/**
 * 一个标准的 [NekoStack] 实现.
 *
 * 底层物品必须拥有 `minecraft:custom_data` 组件, 并且其中存在 `wakame` 的复合标签.
 *
 * 该实现是可变的, 也就是说可以修改其中的属性.
 */
private class CustomNekoStack(
    val handle: ItemStack,
) : NekoStack {

    // 该构造器接受一个 ItemType, 用于从零开始创建一个物品
    constructor(mat: Material) : this(
        handle = ItemStack(mat),
    )

    // 所有访问该对象的代码应该只能读取其状态, 禁止写入
    private val unsafeNyaTag: CompoundTag
        get() = handle.unsafeNyaTagOrThrow

    override val isEmpty: Boolean
        get() = false

    override val itemType: Material
        get() = handle.type

    override val itemStack: ItemStack
        get() = handle.clone()

    override val prototype: NekoItem
        get() = NekoStackSupport.getPrototypeOrThrow(unsafeNyaTag)

    override val namespace: String
        get() = NekoStackSupport.getNamespaceOrThrow(unsafeNyaTag)

    override val path: String
        get() = NekoStackSupport.getPathOrThrow(unsafeNyaTag)

    override val key: Key
        get() = NekoStackSupport.getKeyOrThrow(unsafeNyaTag)

    override var variant: Int
        get() = NekoStackSupport.getVariant(unsafeNyaTag)
        set(value) = NekoStackSupport.setVariant(handle, value)

    override val slotGroup: ItemSlotGroup
        get() = NekoStackSupport.getSlotGroup(unsafeNyaTag)

    override val components: ItemComponentMap
        get() = NekoStackSupport.getComponents(handle)

    override val templates: ItemTemplateMap
        get() = NekoStackSupport.getTemplates(unsafeNyaTag)

    override val behaviors: ItemBehaviorMap
        get() = NekoStackSupport.getBehaviors(unsafeNyaTag)

    override val unsafe: NekoStack.Unsafe
        get() = Unsafe(this)

    override fun clone(): NekoStack {
        return CustomNekoStack(itemStack)
    }

    override fun erase() {
        handle.nyaTag = null
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key.asString()),
        ExaminableProperty.of("variant", variant),
    )

    override fun toString(): String {
        return toSimpleString()
    }

    class Unsafe(
        val owner: CustomNekoStack,
    ) : NekoStack.Unsafe {
        override val nyaTag: CompoundTag
            get() = owner.unsafeNyaTag
        override val handle: ItemStack
            get() = owner.handle
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
    override val isEmpty: Boolean = false
    override val itemType: Material
        get() = unsupported()
    override val itemStack: ItemStack
        get() = unsupported()

    override val namespace: String = key.namespace()
    override val path: String = key.value()

    override var variant: Int = 0 // 变体永远都是 0
    override val slotGroup: ItemSlotGroup = prototype.slotGroup
    override val templates: ItemTemplateMap = prototype.templates
    override val behaviors: ItemBehaviorMap = prototype.behaviors
    override val unsafe: NekoStack.Unsafe
        get() = unsupported()

    override fun clone(): NekoStack =
        unsupported()

    override fun erase(): Unit =
        unsupported()

    override fun examinableProperties(): Stream<out ExaminableProperty> =
        Stream.of(
            ExaminableProperty.of("key", key.asString()),
            ExaminableProperty.of("variant", variant),
        )

    override fun toString(): String =
        toSimpleString()

    private fun unsupported(): Nothing {
        throw UnsupportedOperationException("This operation is not supported on ${this::class.simpleName}")
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
        return wakameTag.getCompoundOrNull(ItemComponentMap.TAG_COMPONENTS)?.contains(ItemComponentConstants.SYSTEM_USE) == true
    }

    fun getNamespace(wakameTag: CompoundTag): String? {
        return wakameTag.getString(BaseBinaryKeys.NAMESPACE)
    }

    fun getNamespaceOrThrow(wakameTag: CompoundTag): String {
        return requireNotNull(getNamespace(wakameTag)) { "Can't find 'namespace' on item NBT" }
    }

    fun setNamespace(handle: ItemStack, namespace: String) {
        handle.editNyaTag { tag -> setNamespace0(tag, namespace) }
    }

    private fun setNamespace0(wakameTag: CompoundTag, namespace: String) {
        wakameTag.putString(BaseBinaryKeys.NAMESPACE, namespace)
    }

    fun getPath(wakameTag: CompoundTag): String? {
        return wakameTag.getString(BaseBinaryKeys.PATH)
    }

    fun getPathOrThrow(wakameTag: CompoundTag): String {
        return requireNotNull(getPath(wakameTag)) { "Can't find 'path' on item NBT" }
    }

    fun setPath(handle: ItemStack, path: String) {
        handle.editNyaTag { tag -> setPath0(tag, path) }
    }

    private fun setPath0(wakameTag: CompoundTag, path: String) {
        wakameTag.putString(BaseBinaryKeys.PATH, path)
    }

    fun getKey(wakameTag: CompoundTag): Key? {
        return getNamespace(wakameTag)?.let { namespace -> getPath(wakameTag)?.let { path -> Key(namespace, path) } }
    }

    fun getKeyOrThrow(wakameTag: CompoundTag): Key {
        return requireNotNull(getKey(wakameTag)) { "Can' find 'key' on item NBT" }
    }

    fun setKey(handle: ItemStack, key: Key) {
        handle.editNyaTag { tag -> setKey0(tag, key) }
    }

    private fun setKey0(wakameTag: CompoundTag, key: Key) {
        setNamespace0(wakameTag, key.namespace())
        setPath0(wakameTag, key.value())
    }

    fun getVariant(wakameTag: CompoundTag): Int {
        // 如果不存在 NBT 标签, 默认返回 0
        return wakameTag.getInt(BaseBinaryKeys.VARIANT)
    }

    fun setVariant(handle: ItemStack, variant: Int) {
        handle.editNyaTag { tag -> setVariant0(tag, variant) }
    }

    private fun setVariant0(wakameTag: CompoundTag, variant: Int) {
        // 如果不存在 NBT 标签, 默认返回 0
        // 所以为 0 时, 应该移除 NBT 标签
        if (variant == 0) {
            wakameTag.remove(BaseBinaryKeys.VARIANT)
            return
        }
        wakameTag.putInt(BaseBinaryKeys.VARIANT, variant)
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
}

// private object NekoStackInjections : KoinComponent {
//     val logger: Logger by inject()
// }

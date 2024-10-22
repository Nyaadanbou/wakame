package cc.mewcraft.wakame.item

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.component.ItemComponentMaps
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.*
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract
import org.koin.core.component.*
import org.slf4j.Logger
import java.util.stream.Stream

/**
 * 检查一个物品能否算作 [NekoStack].
 */
@get:Contract(pure = true)
val ItemStack.isNeko: Boolean
    get() {
        val tag = this.unsafeNyaTag
        if (tag != null) {
            if (NekoStackSupport.getPrototype(tag) != null) {
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
        return NekoStackSupport.getPrototype(tag) != null
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
            if (NekoStackSupport.getPrototype(tag) != null) {
                return CustomNekoStack(this)
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

/**
 * 如果 [ItemStack] 是一个萌芽物品, 则返回对象本身. 否则返回 `null`.
 */
@Contract(pure = true)
fun ItemStack.takeIfNeko(): ItemStack? {
    return this.takeUnlessEmpty()?.takeIf { it.isNeko }
}

/**
 * 直接修改一个被 [NekoStack] 所封装的 [ItemStack]. 仅对 [CustomNekoStack] 有效.
 */
fun NekoStack.directEdit(block: ItemStackDSL.() -> Unit) {
    if (this is CustomNekoStack) {
        handle.edit(block)
    } else {
        Injector.get<Logger>().warn("Attempted to edit a non-custom NekoStack")
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
internal fun ItemBase.createNekoStack(): NekoStack {
    return CustomNekoStack(createItemStack())
}

/**
 * 一个标准的 [NekoStack] 实现.
 *
 * 底层物品必须拥有 `minecraft:custom_data` 组件, 并且其中存在 `wakame` 的复合标签.
 *
 * 该实现是 *可变的*.
 */
private class CustomNekoStack(
    val handle: ItemStack,
) : NekoStack {

    // 所有访问该对象的代码应该只能读取其状态, 禁止写入
    private val unsafeNyaTag: CompoundTag
        get() = handle.unsafeNyaTagOrThrow

    override val isEmpty: Boolean
        get() = false

    override var isClientSide: Boolean
        // 只要*没有*这个标签就返回 true; 标签的类型可以用 ByteTag
        get() = NekoStackSupport.isClientSide(handle)
        set(value) = NekoStackSupport.setClientSide(handle, value)

    override val itemType: Material
        get() = handle.type

    override val itemStack: ItemStack
        get() = handle.clone()

    override val prototype: NekoItem
        get() = NekoStackSupport.getPrototypeOrThrow(unsafeNyaTag)

    override val id: Key
        get() = NekoStackSupport.getIdOrThrow(unsafeNyaTag)

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
        ExaminableProperty.of("id", id.asString()),
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
 * 该实现是 *不可变的*.
 */
internal class VanillaNekoStack(
    override val id: Key,
    override val prototype: NekoItem,
    override val components: ItemComponentMap,
) : NekoStack {
    override val isEmpty: Boolean = false
    override var isClientSide: Boolean
        get() = true
        set(_) = unsupported()

    override val itemType: Material
        get() = unsupported()
    override val itemStack: ItemStack
        get() = unsupported()

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

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("id", id.asString()),
        ExaminableProperty.of("variant", variant),
    )

    override fun toString(): String = toSimpleString()

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

    fun has(id: Key): Boolean {
        return registry.containsKey(id)
    }

    fun get(material: Material): VanillaNekoStack? {
        return get(material.key())
    }

    fun get(id: Key): VanillaNekoStack? {
        return registry[id]
    }

    fun register(id: Key, stack: VanillaNekoStack) {
        registry[id] = stack
    }

    override fun onPostWorld() {
        realizeAndRegister()
    }

    override fun onReload() {
        realizeAndRegister()
    }

    private fun realizeAndRegister() {
        registry.clear()
        for ((id, prototype) in ItemRegistry.VANILLA) {
            val stack = realizer.realize(prototype)
            register(id, stack)
        }
    }
}

/**
 * Common implementations related to [NekoStack].
 */
internal object NekoStackSupport {
    fun isClientSide(handle: ItemStack): Boolean {
        val nbt = handle.unsafeNbt ?: return true
        return !nbt.contains(NekoStack.CLIENT_SIDE_KEY)
    }

    fun setClientSide(handle: ItemStack, clientSide: Boolean) {
        handle.editNbt { tag ->
            if (clientSide) {
                tag.remove(NekoStack.CLIENT_SIDE_KEY)
            } else {
                tag.putByte(NekoStack.CLIENT_SIDE_KEY, 0)
            }
        }
    }

    @Deprecated("Use 'getId' instead", ReplaceWith("getId(wakameTag)"))
    fun getNamespace(wakameTag: CompoundTag): String? {
        return wakameTag.getString(BaseBinaryKeys.NAMESPACE)
    }

    @Deprecated("Use 'getId' instead", ReplaceWith("getId(wakameTag)"))
    fun getPath(wakameTag: CompoundTag): String? {
        return wakameTag.getString(BaseBinaryKeys.PATH)
    }

    fun getId(wakameTag: CompoundTag): Key? {
        return wakameTag.getString(BaseBinaryKeys.ID)
            .takeIf(String::isNotEmpty)
            ?.runCatching { Key.key(this) }
            ?.getOrNull()
            ?: getNamespace(wakameTag)?.let { namespace ->
                getPath(wakameTag)?.let { path ->
                    Key.key(namespace, path)
                }
            }
    }

    fun getIdOrThrow(wakameTag: CompoundTag): Key {
        return requireNotNull(getId(wakameTag)) { "Can' find 'id' on item NBT" }
    }

    fun setId(handle: ItemStack, id: Key) {
        handle.editNyaTag { tag -> setId0(tag, id) }
    }

    private fun setId0(wakameTag: CompoundTag, id: Key) {
        wakameTag.putString(BaseBinaryKeys.ID, id.asString())
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
        val id = getIdOrThrow(wakameTag)
        val prototype = ItemRegistry.CUSTOM.find(id)
        return prototype
    }

    fun getPrototypeOrThrow(wakameTag: CompoundTag): NekoItem {
        val id = getIdOrThrow(wakameTag)
        val prototype = requireNotNull(ItemRegistry.CUSTOM.find(id)) { "Can't find item prototype by id '$id'" }
        return prototype
    }

    fun getComponents(stack: ItemStack): ItemComponentMap {
        return ItemComponentMaps.wrapStack(stack)
    }

    fun getImmutableComponents(stack: ItemStack): ItemComponentMap {
        return ItemComponentMaps.unmodifiable(getComponents(stack))
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

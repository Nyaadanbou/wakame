@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.item

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.component.ItemComponentMaps
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.ItemStackDSL
import cc.mewcraft.wakame.util.edit
import cc.mewcraft.wakame.util.editNekooTag
import cc.mewcraft.wakame.util.editRootTag
import cc.mewcraft.wakame.util.nekooTagOrNull
import cc.mewcraft.wakame.util.rootTagOrNull
import cc.mewcraft.wakame.util.toSimpleString
import cc.mewcraft.wakame.util.unsafeNekooTag
import cc.mewcraft.wakame.util.unsafeNekooTagOrNull
import cc.mewcraft.wakame.util.unsafeRootTagOrNull
import io.papermc.paper.datacomponent.DataComponentTypes
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract
import java.util.stream.Stream

/**
 * 检查一个物品能否算作 [NekoStack].
 */
@get:Contract(pure = true)
val ItemStack.isNeko: Boolean
    get() {
        val nbt = this.unsafeNekooTagOrNull
        if (nbt != null && NekoStackImplementations.getArchetypeOrNull(nbt) != null) {
            return true
        }
        return ImaginaryNekoStackRegistry.has(this.type)
    }

/**
 * 检查一个物品堆叠是否算作 [CustomNekoStack].
 */
@get:Contract(pure = true)
val ItemStack.isCustomNeko: Boolean
    get() {
        val nbt = this.unsafeNekooTagOrNull ?: return false
        return NekoStackImplementations.getArchetypeOrNull(nbt) != null
    }

/**
 * 检查一个物品堆叠是否算作 [VanillaNekoStack].
 */
@get:Contract(pure = true)
val ItemStack.isVanillaNeko: Boolean
    get() {
        if (this.unsafeNekooTagOrNull != null) {
            return false
        }
        return ImaginaryNekoStackRegistry.has(this.type)
    }

/**
 * 尝试获取 [ItemStack] 在萌芽物品系统下的投影 [NekoStack].
 * 如果该 [ItemStack] 不存在投影, 则返回 `null`.
 */
// TODO NekoStack.wrap / NekoStack.unwrap
@Contract(pure = true)
fun ItemStack.shadowNeko(excludeVanilla: Boolean = false): NekoStack? {
    val nbt = this.unsafeNekooTagOrNull
    if (nbt != null) {
        if (NekoStackImplementations.getArchetypeOrNull(nbt) != null) {
            // 存在萌芽 NBT, 并且存在对应的物品模板,
            // 那么这是一个合法的 CustomNekoStack.
            return CustomNekoStack(this)
        } else {
            // 存在萌芽 NBT, 但是没有对应的物品模板,
            // 这意味着该物品定义已经从物品库中移除.
            this.rootTagOrNull = null
        }
    } else {
        if (excludeVanilla) {
            // 函数的参数选择不对原版物品进行投影,
            // 返回 null
            return null
        }

        // 如果没有萌芽 NBT, 并且没有对应的物品模板,
        // 则看该物品是否存在一个原版物品的萌芽投影.
        val imaginary = ImaginaryNekoStackRegistry.get(this.type)
            ?: return null // 没有原版物品的萌芽投影, 返回 null

        return VanillaNekoStack(imaginary, this)
    }

    return null
}

/**
 * 获取 [ItemStack] 在萌芽物品系统下的投影 [NekoStack].
 * 如果该 [ItemStack] 不存在投影, 则抛出异常.
 *
 * @throws IllegalArgumentException
 */
@Contract(pure = true)
fun ItemStack.projectNeko(excludeVanilla: Boolean = false): NekoStack {
    return requireNotNull(this.shadowNeko(excludeVanilla)) { "The ItemStack cannot be projected to NekoStack" }
}

/**
 * 尝试获取 [ItemStack] 在萌芽物品系统下的投影.
 * 如果无法完成转换, 则返回 `null`.
 */
@Deprecated("Use 'shadowNeko' instead", ReplaceWith("shadowNeko(false)"))
@get:Contract(pure = true)
val ItemStack.tryNekoStack: NekoStack?
    get() = shadowNeko(false)

/**
 * 获取 [ItemStack] 在萌芽物品系统下的投影.
 * 如果无法完成转换, 则抛出异常.
 *
 * @throws IllegalArgumentException
 */
@Deprecated("Use 'projectNeko' instead", ReplaceWith("projectNeko(false)"))
@get:Contract(pure = true)
val ItemStack.toNekoStack: NekoStack
    get() = projectNeko(false)

/**
 * 如果 [ItemStack] 在萌芽系统中存在投影, 则返回对象本身. 否则返回 `null`.
 */
@Contract(pure = true)
fun ItemStack.takeIfNeko(): ItemStack? {
    return takeIf(ItemStack::isNeko)
}

/**
 * 直接修改一个 [NekoStack] 封装的底层 [ItemStack].
 * 警告: 如果底层封装的是纯原版物品, 该函数也会直接修改.
 */
fun NekoStack.unsafeEdit(block: ItemStackDSL.() -> Unit): ItemStack {
    return wrapped.edit(block)
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
 * 一个标准的 [NekoStack] 实现, 封装了一个 [ItemStack][handle].
 *
 * 如果该实例存在, 则说明 [handle] 肯定拥有 `custom_data` 组件,
 * 并且其中存在萌芽的 NBT 标签.
 */
private class CustomNekoStack(
    private val handle: ItemStack,
) : NekoStack {

    // 所有访问该对象的代码应该只能读取, 禁止写入!
    private val unsafeNekooTag: CompoundTag
        get() = handle.unsafeNekooTag

    override val isEmpty: Boolean
        get() = false

    override var isClientSide: Boolean
        get() = NekoStackImplementations.isClientSide(handle)
        set(value) = NekoStackImplementations.setClientSide(handle, value)

    override val itemType: Material
        get() = handle.type

    override val itemStack: ItemStack
        get() = handle.clone()

    override val wrapped: ItemStack
        get() = handle

    override val prototype: NekoItem
        get() = NekoStackImplementations.getArchetype(unsafeNekooTag)

    override val id: Key
        get() = NekoStackImplementations.getId(unsafeNekooTag)

    override var variant: Int
        get() = NekoStackImplementations.getVariant(unsafeNekooTag)
        set(value) = NekoStackImplementations.setVariant(handle, value)

    override val slotGroup: ItemSlotGroup
        get() = NekoStackImplementations.getSlotGroup(unsafeNekooTag)

    override val components: ItemComponentMap
        get() = NekoStackImplementations.getComponents(handle)

    override val templates: ItemTemplateMap
        get() = NekoStackImplementations.getTemplates(unsafeNekooTag)

    override val behaviors: ItemBehaviorMap
        get() = NekoStackImplementations.getBehaviors(unsafeNekooTag)

    override val unsafe: NekoStack.Unsafe
        get() = Unsafe(this)

    override fun clone(): NekoStack {
        return CustomNekoStack(itemStack)
    }

    override fun erase() {
        handle.nekooTagOrNull = null
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
        override val nekooTag: CompoundTag
            get() = owner.unsafeNekooTag
    }
}

/**
 * 一个特殊的 [NekoStack] 实现, 代表一个 *原版物品* 在萌芽物品系统下的投影.
 * 仅用于封装原版的物品堆叠, 以便让原版物品拥有一些 (固定的) 萌芽物品的特性.
 *
 * 本实现禁止一切写操作! 任何写操作都会导致异常.
 */
private class VanillaNekoStack(
    private val shadow: NekoStack, // 在萌芽物品系统下的投影
    private val handle: ItemStack, // 所封装的原版物品堆叠
) : NekoStack {
    override val isEmpty: Boolean = false

    // 即便是萌芽套皮的原版物品, 也允许修改 isClientSide.
    // 这算是个特例, 但具有其合理性, 原因如下:
    //
    // 按照设计原则, 我们不应该修改原版物品的任何数据, 但对于渲染来说是个特例:
    // 渲染往往是修改非玩家拥有的物品数据 (例如网络发包和箱子菜单里的物品堆叠),
    // 这些修改后的数据只会存在于客户端和箱子菜单里, 单纯起到展示的作用,
    // 不会对游戏世界里的状态产生影响.
    //
    // 当然, 以上都是假设所有渲染逻辑都是在物品的克隆上进行的.
    // 程序员当然可以直接对游戏内的萌芽套皮原版物品进行修改,
    // 但这种错误应该在 code review 时就发现并修复.
    override var isClientSide: Boolean
        get() = NekoStackImplementations.isClientSide(handle)
        set(value) = NekoStackImplementations.setClientSide(handle, value)

    override val itemType: Material
        get() = handle.type

    override val itemStack: ItemStack
        get() = handle.clone()

    override val wrapped: ItemStack
        get() = handle

    override val id: Key
        get() = prototype.id

    override var variant: Int
        get() = 0 // 变体永远都是 0
        set(_) = Unit

    override val slotGroup: ItemSlotGroup
        get() = prototype.slotGroup

    override val prototype: NekoItem
        get() = shadow.prototype

    override val components: ItemComponentMap
        get() {
            // base 和 patch 都是只读的数据结构
            val base = shadow.components
            val patch = ItemComponentMaps.unmodifiable(handle)
            return ItemComponentMaps.composite(base, patch)
        }

    override val templates: ItemTemplateMap
        get() = prototype.templates

    override val behaviors: ItemBehaviorMap
        get() = prototype.behaviors

    override val unsafe: NekoStack.Unsafe
        get() = unsupported()

    override fun clone(): NekoStack {
        return VanillaNekoStack(shadow, itemStack)
    }

    override fun erase() {
        // 原版物品没有可以擦除的 NBT
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("id", id.asString()),
        ExaminableProperty.of("variant", variant),
    )

    override fun toString(): String {
        return toSimpleString()
    }

    private fun unsupported(): Nothing {
        throw UnsupportedOperationException("This operation is not supported on ${this::class.simpleName}")
    }
}

/**
 * 一个虚拟的 [NekoStack] 实现.
 * 虚拟”指这个 [NekoStack] 不负责封装 [ItemStack],
 * 而是负责独立储存一些固定的信息 (例如: 攻速与核孔).
 *
 * ### 开发日记 2024/10/27 小米
 * 目前该实现仅仅用于储存 *原版物品* 的萌芽投影.
 *
 * @see VanillaNekoStack
 */
internal class ImaginaryNekoStack(
    // 物品模板
    override val prototype: NekoItem,
    // 物品组件 (禁止修改)
    override val components: ItemComponentMap,
) : NekoStack {
    override val isEmpty: Boolean
        get() = false

    override var isClientSide: Boolean
        get() = unsupported()
        set(_) = unsupported()

    override val itemType: Material
        get() = unsupported()

    override val itemStack: ItemStack
        get() = unsupported()

    override val wrapped: ItemStack
        get() = unsupported()

    override val id: Key = prototype.id

    override var variant: Int
        get() = unsupported()
        set(_) = unsupported()

    override val slotGroup: ItemSlotGroup = prototype.slotGroup

    override val templates: ItemTemplateMap = prototype.templates

    override val behaviors: ItemBehaviorMap = prototype.behaviors

    override val unsafe: NekoStack.Unsafe
        get() = unsupported()

    override fun clone(): NekoStack {
        unsupported()
    }

    override fun erase() {
        unsupported()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("id", id.asString()),
        ExaminableProperty.of("variant", variant),
    )

    override fun toString(): String {
        return toSimpleString()
    }

    private fun unsupported(): Nothing {
        throw UnsupportedOperationException("This operation is not supported on ${this::class.simpleName}")
    }
}

@Init(
    stage = InitStage.POST_WORLD,
    runAfter = [ItemRegistryConfigStorage::class], // deps: 需要转换成 NekoStack, 因此必须在之后
)
@Reload(
    runAfter = [ItemRegistryConfigStorage::class],
)
internal object ImaginaryNekoStackRegistry {
    private val CACHE: Object2ObjectOpenHashMap<Key, ImaginaryNekoStack> = Object2ObjectOpenHashMap(16)

    @InitFun
    private fun init() {
        realizeAndStore()
    }

    @ReloadFun
    private fun reload() {
        realizeAndStore()
    }

    fun has(material: Material): Boolean {
        return has(material.key())
    }

    fun has(id: Key): Boolean {
        return CACHE.containsKey(id)
    }

    fun get(material: Material): ImaginaryNekoStack? {
        return get(material.key())
    }

    fun get(id: Key): ImaginaryNekoStack? {
        return CACHE[id]
    }

    private fun realizeAndStore() {
        // CACHE.clear() // 由于 Registry 的 Entry 不会减少, 所以不需要手动 clear, 键名相同的会自动替换掉

        val filter = KoishRegistries.ITEM.filter { it.id.namespace() == Identifier.MINECRAFT_NAMESPACE }
        for (prototype in filter) {
            val id = prototype.id
            val stack = VanillaNekoItemRealizer.realize(prototype)
            CACHE[id] = stack
        }
    }
}

/**
 * Common implementations related to [NekoStack].
 */
internal object NekoStackImplementations {
    fun isClientSide(handle: ItemStack): Boolean {
        val nbt = handle.unsafeRootTagOrNull ?: return true
        // 只要*没有*这个标签就返回 true;
        // 标签类型可以用最小的 ByteTag.
        return !nbt.contains(NekoStack.CLIENT_SIDE_KEY)
    }

    fun setClientSide(handle: ItemStack, clientSide: Boolean) {
        handle.editRootTag { tag ->
            if (clientSide) {
                tag.remove(NekoStack.CLIENT_SIDE_KEY)
            } else {
                tag.putByte(NekoStack.CLIENT_SIDE_KEY, 0)
            }
        }
    }

    fun getIdOrNull(nekoo: CompoundTag): Key? {
        return nekoo.getString(BaseBinaryKeys.ID).takeIf(String::isNotEmpty)
            ?.runCatching { Key.key(this) }
            ?.getOrNull()
    }

    fun getId(nekoo: CompoundTag): Key {
        return requireNotNull(getIdOrNull(nekoo)) { "Can' find 'id' on item NBT" }
    }

    fun setId(handle: ItemStack, id: Key) {
        handle.editNekooTag { tag -> setId0(tag, id) }
    }

    private fun setId0(nekoo: CompoundTag, id: Key) {
        nekoo.putString(BaseBinaryKeys.ID, id.asString())
    }

    fun getVariant(nekoo: CompoundTag): Int {
        // 如果不存在 NBT 标签, 默认返回 0
        return nekoo.getInt(BaseBinaryKeys.VARIANT)
    }

    fun setVariant(handle: ItemStack, variant: Int) {
        handle.editNekooTag { tag -> setVariant0(tag, variant) }
    }

    private fun setVariant0(nekoo: CompoundTag, variant: Int) {
        // 如果不存在 NBT 标签, 默认返回 0
        // 所以为 0 时, 应该移除 NBT 标签
        if (variant == 0) {
            nekoo.remove(BaseBinaryKeys.VARIANT)
            return
        }
        nekoo.putInt(BaseBinaryKeys.VARIANT, variant)
    }

    fun setItemModel(stack: ItemStack, key: Key) {
        stack.setData(DataComponentTypes.ITEM_MODEL, key)
    }

    fun getSlotGroup(nekoo: CompoundTag): ItemSlotGroup {
        val prototype = getArchetype(nekoo)
        return prototype.slotGroup
    }

    fun getArchetypeOrNull(nekoo: CompoundTag): NekoItem? {
        val id = getId(nekoo)
        val prototype = KoishRegistries.ITEM[id]
        return prototype
    }

    fun getArchetype(nekoo: CompoundTag): NekoItem {
        val id = getId(nekoo)
        val prototype = requireNotNull(KoishRegistries.ITEM[id]) { "Can't find item prototype by id '$id'" }
        return prototype
    }

    fun getComponents(stack: ItemStack): ItemComponentMap {
        return ItemComponentMaps.wrapStack(stack)
    }

    fun getImmutableComponents(stack: ItemStack): ItemComponentMap {
        return ItemComponentMaps.unmodifiable(getComponents(stack))
    }

    fun getTemplates(nekoo: CompoundTag): ItemTemplateMap {
        val prototype = getArchetype(nekoo)
        return prototype.templates
    }

    fun getBehaviors(nekoo: CompoundTag): ItemBehaviorMap {
        val prototype = getArchetype(nekoo)
        return prototype.behaviors
    }
}

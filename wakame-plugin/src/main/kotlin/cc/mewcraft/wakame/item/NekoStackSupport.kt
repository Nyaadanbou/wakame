@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.adventure.toSimpleString
import cc.mewcraft.wakame.util.data.getCompoundOrNull
import cc.mewcraft.wakame.util.data.getOrPut
import cc.mewcraft.wakame.util.item.*
import io.papermc.paper.adventure.PaperAdventure
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import org.bukkit.Material
import org.bukkit.craftbukkit.inventory.CraftItemType
import org.bukkit.inventory.ItemStack
import java.util.stream.Stream

fun ItemStack.wrap(includeProxies: Boolean = true): NekoStack? = unwrapToMojang().wrap(includeProxies)
fun MojangStack.wrapOrThrow(includeProxies: Boolean = true): NekoStack = wrap(includeProxies) ?: error("The ItemStack cannot be wrapped to NekoStack")
fun MojangStack.wrap(includeProxies: Boolean = true): NekoStack? = KoishStackImplementations.wrap(this, includeProxies)

/**
 * 检查该物品堆叠是否在网络发包时重写.
 */
var NekoStack.isNetworkRewrite: Boolean
    get() = if (this is CustomKoishStack || this is VanillaKoishStack) KoishStackImplementations.isNetworkRewrite(mojangStack) else false
    set(value) {
        if (this is CustomKoishStack || this is VanillaKoishStack) KoishStackImplementations.setNetworkRewrite(mojangStack, value)
    }

/**
 * 一个标准的 [NekoStack] 实现, 封装了一个 [ItemStack][handle].
 *
 * 如果该实例存在, 则说明 [handle] 肯定拥有 `custom_data` 组件,
 * 并且其中存在萌芽的 NBT 标签.
 */
internal class CustomKoishStack(
    @JvmField val handle: MojangStack,
) : NekoStack {

    override val isEmpty: Boolean
        get() = false

    override val itemType: Material
        get() = KoishStackImplementations.getItemType(handle)

    override val mojangStack: MojangStack
        get() = handle

    override val bukkitStack: ItemStack
        get() = KoishStackImplementations.getItemStack(handle)

    override val prototype: NekoItem
        get() = KoishStackImplementations.getArchetypeOrThrow(handle)

    override val id: Key
        get() = KoishStackImplementations.getIdOrThrow(handle)

    override var variant: Int
        get() = KoishStackImplementations.getVariant(handle)
        set(value) = KoishStackImplementations.setVariant(handle, value)

    override val slotGroup: ItemSlotGroup
        get() = KoishStackImplementations.getSlotGroup(handle)

    override val components: ItemComponentMap
        get() = KoishStackImplementations.getMutableDataContainer(handle)

    override val templates: ItemTemplateMap
        get() = KoishStackImplementations.getTemplatesOrThrow(handle)

    override val behaviors: ItemBehaviorMap
        get() = KoishStackImplementations.getBehaviorsOrThrow(handle)

    override fun clone(): NekoStack = CustomKoishStack(handle.copy())

    override fun erase() = KoishStackImplementations.erase(handle)

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("id", id.asString()),
        ExaminableProperty.of("variant", variant),
    )

    override fun toString(): String = toSimpleString()

}

/**
 * 一个特殊的 [NekoStack] 实现, 代表一个 *原版物品* 在萌芽物品系统下的投影.
 * 仅用于封装原版的物品堆叠, 以便让原版物品拥有一些 (固定的) 萌芽物品的特性.
 *
 * 本实现禁止一切写操作! 任何写操作都会导致异常.
 */
internal class VanillaKoishStack(
    @JvmField val shadow: NekoStack, // 在萌芽物品系统下的投影
    @JvmField val handle: MojangStack, // 所封装的原版物品堆叠
) : NekoStack {

    override val isEmpty: Boolean
        get() = false

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
    //override var isClientSide: Boolean

    override val itemType: Material
        get() = KoishStackImplementations.getItemType(handle)

    override val mojangStack: MojangStack
        get() = handle

    override val bukkitStack: ItemStack
        get() = KoishStackImplementations.getItemStack(handle)

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
            val patch = ItemComponentMap.immutable(handle)
            return ItemComponentMap.compose(base, patch)
        }

    override val templates: ItemTemplateMap
        get() = prototype.templates

    override val behaviors: ItemBehaviorMap
        get() = prototype.behaviors

    override fun clone(): NekoStack = VanillaKoishStack(shadow, handle.copy())

    override fun erase() = Unit

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("id", id.asString()),
        ExaminableProperty.of("variant", variant),
    )

    override fun toString(): String = toSimpleString()

    private fun throwUOE(): Nothing = throw UnsupportedOperationException("This operation is not supported on ${this::class.simpleName}")

}

/**
 * 一个虚拟的 [NekoStack] 实现.
 * 虚拟”指这个 [NekoStack] 不负责封装 [ItemStack],
 * 而是负责独立储存一些固定的信息 (例如: 攻速与核孔).
 *
 * ### 开发日记 2024/10/27 小米
 * 目前该实现仅仅用于储存 *原版物品* 的萌芽投影.
 *
 * @see VanillaKoishStack
 */
internal class ImaginaryKoishStack(
    override val prototype: NekoItem,
    override val components: ItemComponentMap, // (禁止修改)
) : NekoStack {

    override val isEmpty: Boolean
        get() = false

    override val itemType: Material
        get() = throwUOE()

    override val mojangStack: MojangStack
        get() = throwUOE()

    override val bukkitStack: ItemStack
        get() = throwUOE()

    override val id: Key = prototype.id

    override var variant: Int
        get() = throwUOE()
        set(_) = throwUOE()

    override val slotGroup: ItemSlotGroup = prototype.slotGroup

    override val templates: ItemTemplateMap = prototype.templates

    override val behaviors: ItemBehaviorMap = prototype.behaviors

    override fun clone(): NekoStack = throwUOE()

    override fun erase(): Unit = throwUOE()

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("id", id.asString()),
        ExaminableProperty.of("variant", variant),
    )

    override fun toString(): String = toSimpleString()

    private fun throwUOE(): Nothing = throw UnsupportedOperationException("This operation is not supported on ${this::class.simpleName}")

}

@Init(
    stage = InitStage.POST_WORLD,
    runAfter = [ItemTypeRegistryLoader::class] /* deps: 需要转换成 NekoStack, 因此必须在之后 */
)
@Reload(
    runAfter = [ItemTypeRegistryLoader::class]
)
internal object ImaginaryKoishStackRegistry {

    private val CACHE: Object2ObjectOpenHashMap<Key, ImaginaryKoishStack> = Object2ObjectOpenHashMap(16)

    @InitFun
    fun init() {
        realizeAndStore()
    }

    @ReloadFun
    fun reload() {
        realizeAndStore()
    }

    fun has(material: Material): Boolean {
        return has(material.key())
    }

    fun has(id: Key): Boolean {
        return CACHE.containsKey(id)
    }

    fun get(material: Material): ImaginaryKoishStack? {
        return get(material.key())
    }

    fun get(id: Key): ImaginaryKoishStack? {
        return CACHE[id]
    }

    private fun realizeAndStore() {
        // CACHE.clear() // 由于 Registry 的 Entry 不会减少, 所以不需要手动 clear, 键名相同的会自动替换掉

        for (prototype in KoishRegistries.ITEM.filter { it.id.namespace() == Identifier.MINECRAFT_NAMESPACE }) {
            val id = prototype.id
            val stack = VanillaNekoItemRealizer.realize(prototype)
            CACHE[id] = stack
        }
    }

}

/**
 * Common implementations related to [NekoStack].
 */
internal object KoishStackImplementations {

    private const val KOISH_FIELD = "wakame"
    private const val ID_FIELD = "id"
    private const val VARIANT_FIELD = "sid"

    fun isNetworkRewrite(itemstack: MojangStack): Boolean {
        return itemstack.isNetworkRewrite
    }

    fun setNetworkRewrite(itemstack: MojangStack, networkRewrite: Boolean) {
        itemstack.isNetworkRewrite = networkRewrite
    }

    fun getItemStack(itemstack: MojangStack): ItemStack {
        return itemstack.wrapToBukkit()
    }

    fun getItemType(itemstack: MojangStack): Material {
        return CraftItemType.minecraftToBukkit(itemstack.item)
    }

    fun getId(itemstack: MojangStack): Key? {
        return getNbt(itemstack)
            ?.getString(ID_FIELD)
            ?.takeIf(String::isNotEmpty)
            ?.runCatching(Key::key)
            ?.getOrNull()
    }

    fun getIdOrThrow(itemstack: MojangStack): Key {
        return requireNotNull(getId(itemstack)) { "Cannot find 'id' field on item NBT" }
    }

    fun setId(itemstack: MojangStack, id: Key) {
        editNbt(itemstack) { compound -> compound.putString(ID_FIELD, id.asString()) }
    }

    fun getVariant(itemstack: MojangStack): Int {
        // 如果不存在 NBT 标签, 默认返回 0
        return getNbt(itemstack)?.getInt(BaseBinaryKeys.VARIANT) ?: 0
    }

    fun setVariant(itemstack: MojangStack, variant: Int) {
        editNbt(itemstack) { compound -> if (variant == 0) compound.remove(VARIANT_FIELD) else compound.putInt(VARIANT_FIELD, variant) }
    }

    fun setItemModel(itemstack: MojangStack, key: Key) {
        itemstack.set(DataComponents.ITEM_MODEL, PaperAdventure.asVanilla(key))
    }

    fun getSlotGroup(itemstack: MojangStack): ItemSlotGroup {
        return getArchetypeOrThrow(itemstack).slotGroup
    }

    fun getArchetype(itemstack: MojangStack): NekoItem? {
        return getId(itemstack)?.let(KoishRegistries.ITEM::get)
    }

    fun getArchetypeOrThrow(itemstack: MojangStack): NekoItem {
        val id = getIdOrThrow(itemstack)
        return requireNotNull(KoishRegistries.ITEM[id]) { "Cannot find item prototype by id '$id'" }
    }

    fun getMutableDataContainer(itemstack: MojangStack): ItemComponentMap {
        return ItemComponentMap.mutable(itemstack)
    }

    fun getImmutableDataContainer(itemstack: MojangStack): ItemComponentMap {
        return ItemComponentMap.immutable(getMutableDataContainer(itemstack))
    }

    fun getTemplatesOrThrow(itemstack: MojangStack): ItemTemplateMap {
        return getArchetypeOrThrow(itemstack).templates
    }

    fun getBehaviorsOrThrow(itemstack: MojangStack): ItemBehaviorMap {
        return getArchetypeOrThrow(itemstack).behaviors
    }

    fun erase(itemstack: MojangStack) {
        itemstack.editNbt { tag -> tag.remove(KOISH_FIELD) }
    }

    fun wrap(itemstack: MojangStack, includeProxies: Boolean = true): NekoStack? {
        val koishCompound = getNbt(itemstack)
        if (koishCompound != null) {
            if (getArchetype(itemstack) != null) {
                // 存在萌芽 NBT, 并且存在对应的物品模板,
                // 那么这是一个合法的 CustomNekoStack.
                return CustomKoishStack(itemstack)
            } else {
                // 存在萌芽 NBT, 但是没有对应的物品模板,
                // 这意味着该物品定义已经从物品库中移除.
                erase(itemstack)
                return null
            }
        } else {
            // 如果没有萌芽 NBT, 并且没有对应的物品模板, 则看该物品是否存在一个原版物品的萌芽投影
            if (!includeProxies) return null
            val imaginary = ImaginaryKoishStackRegistry.get(CraftItemType.minecraftToBukkit(itemstack.item))
            if (imaginary == null) return null
            return VanillaKoishStack(imaginary, itemstack)
        }
    }

    fun editNbt(
        itemstack: MojangStack,
        applier: (koishCompound: CompoundTag) -> Unit,
    ) {
        itemstack.editNbt { rootCompound ->
            val koishCompound = rootCompound.getOrPut(KOISH_FIELD, ::CompoundTag).apply(applier)
            rootCompound.put(KOISH_FIELD, koishCompound)
        }
    }

    /**
     * 返回值只允许读, 如直接修改将破坏 NMS Data Component 关于不可变性的契约.
     */
    fun getNbt(itemstack: MojangStack): CompoundTag? {
        return itemstack.nbt?.getCompoundOrNull(KOISH_FIELD)
    }

    /**
     * 返回值只允许读, 如直接修改将破坏 NMS Data Component 关于不可变性的契约.
     */
    fun getNbtOrThrow(itemstack: MojangStack): CompoundTag {
        return getNbt(itemstack) ?: error("Cannot find NBT compound '$KOISH_FIELD' on item")
    }

}

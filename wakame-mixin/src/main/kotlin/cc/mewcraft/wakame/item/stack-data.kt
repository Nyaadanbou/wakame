@file:JvmName("KoishStackData")
@file:Suppress("VerboseNullabilityAndEmptiness")

package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.data.ItemDataContainer
import cc.mewcraft.wakame.item.data.ItemDataType
import cc.mewcraft.wakame.item.data.ItemDataTypes
import cc.mewcraft.wakame.item.datagen.ItemMetaContainer
import cc.mewcraft.wakame.item.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item.datagen.ItemMetaType
import cc.mewcraft.wakame.item.property.ItemPropContainer
import cc.mewcraft.wakame.item.property.ItemPropType
import cc.mewcraft.wakame.mixin.support.ExtraDataComponents
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.item.toNMS
import net.minecraft.world.item.Item
import org.bukkit.Material
import org.bukkit.craftbukkit.inventory.CraftItemType
import org.bukkit.inventory.ItemStack


// ------------------
// 用于访问 `org.bukkit.inventory.ItemStack` 上的 Koish 数据
// (关于注释和文档, 请参考本文件后半部分的 MojangStack 的扩展函数)
// ------------------

/* Type */

val Material.itemProxy: KoishItemProxy? get() = BuiltInRegistries.ITEM_PROXY[key()]
fun Material.hasItemProxy(): Boolean = itemProxy != null
val ItemStack.typeId: Identifier get() = toNMS().typeId
val ItemStack.koishTypeId: Identifier? get() = toNMS().koishTypeId
val ItemStack.isKoish: Boolean get() = toNMS().isKoish
val ItemStack.isExactKoish: Boolean get() = toNMS().isExactKoish
val ItemStack.isProxyKoish: Boolean get() = toNMS().isProxyKoish
val ItemStack.koishItem: KoishItem? get() = toNMS().koishItem
fun ItemStack.dataContainer(includeProxy: Boolean): ItemDataContainer? = toNMS().dataContainer(includeProxy)
fun ItemStack.metaContainer(): ItemMetaContainer? = toNMS().metaContainer()
fun ItemStack.propContainer(): ItemPropContainer? = toNMS().propContainer()

/* Property */

fun <T> ItemStack.getProp(type: ItemPropType<out T>): T? = toNMS().getProp(type)
fun <T> ItemStack.hasProp(type: ItemPropType<T>): Boolean = toNMS().hasProp(type)
fun <T> ItemStack.getPropOrDefault(type: ItemPropType<out T>, fallback: T): T? = toNMS().getPropOrDefault(type, fallback)

@Deprecated("Use getProp instead", ReplaceWith("hasProp(type)"))
fun <T> ItemStack.hasProperty(type: ItemPropType<T>): Boolean = hasProp(type)
@Deprecated("Use hasProp instead", ReplaceWith("getProp(type)"))
fun <T> ItemStack.getProperty(type: ItemPropType<out T>): T? = getProp(type)
@Deprecated("Use getPropOrDefault instead", ReplaceWith("getPropOrDefault(type, fallback)"))
fun <T> ItemStack.getPropertyOrDefault(type: ItemPropType<out T>, fallback: T): T? = toNMS().getPropOrDefault(type, fallback)

/* ItemData */

fun ItemStack.hasData(type: ItemDataType<*>): Boolean = toNMS().hasData(type)
fun <T> ItemStack.getData(type: ItemDataType<out T>): T? = toNMS().getData(type)
fun <T> ItemStack.getDataOrDefault(type: ItemDataType<out T>, fallback: T): T = toNMS().getDataOrDefault(type, fallback)
fun <T> ItemStack.setData(type: ItemDataType<in T>, value: T): T? = toNMS().setData(type, value)
fun <T> ItemStack.removeData(type: ItemDataType<out T>): T? = toNMS().removeData(type)

/* ItemMeta */

fun <U : ItemMetaEntry<V>, V> ItemStack.getMeta(type: ItemMetaType<U, V>): U? = toNMS().getMeta(type)
fun <U : ItemMetaEntry<V>, V> ItemStack.hasMeta(type: ItemMetaType<U, V>): Boolean = toNMS().hasMeta(type)
fun <U : ItemMetaEntry<V>, V> ItemStack.getMetaOrDefault(type: ItemMetaType<U, V>, fallback: U): U = toNMS().getMetaOrDefault(type, fallback)

@get:JvmName("isNetworkRewrite")
@set:JvmName("setNetworkRewrite")
var ItemStack.isNetworkRewrite: Boolean
    get() = toNMS().isNetworkRewrite
    set(value) {
        toNMS().isNetworkRewrite = value
    }

// ------------------
// 用于访问 `net.minecraft.world.item.ItemStack` 上的 Koish 数据
// ------------------

/* Type */

/**
 * 获取该物品类型的套皮物品的实例.
 */
val Item.itemProxy: KoishItemProxy?
    get() = CraftItemType.minecraftToBukkit(this).itemProxy

/**
 * 判断该物品类型是否存在套皮物品的实例.
 */
fun Item.hasItemProxy(): Boolean =
    CraftItemType.minecraftToBukkit(this).hasItemProxy()

/**
 * 获取该物品堆叠的物品类型的 [Identifier].
 *
 * - 如果该物品堆叠是一个 *Koish 物品* , 则命名空间为 `koish`
 * - 如果该物品堆叠是一个 *原版(或 Koish 套皮)物品*, 则命名空间为 `minecraft`
 * - 如果该物品堆叠来自其他物品系统, 将被当作是一个 *原版物品*, 命名空间为 `minecraft` // TODO 支持 ItemRef
 */
val MojangStack.typeId: Identifier
    // 最终版本
    //get() = get(ExtraDataComponents.ITEM_KEY)?.id ?: CraftItemType.minecraftToBukkit(item).key()
    // FIXME 临时版本, 用于过渡
    get() = get(ExtraDataComponents.ITEM_KEY)?.id ?: get(ExtraDataComponents.DATA_CONTAINER)?.get(ItemDataTypes.ID)?.id ?: CraftItemType.minecraftToBukkit(item).key()

/**
 * 获取该物品堆叠的物品类型的 [Identifier].
 *
 * - 只有当该物品堆叠属于 Koish 物品库时才返回非空值.
 * - 不包含套皮物品/原版物品 - 这种情况会直接返回空值.
 */
val MojangStack.koishTypeId: Identifier?
    // 最终版本
    //get() = get(ExtraDataComponents.ITEM_KEY)?.id
    // FIXME 临时版本, 用于过渡
    get() = get(ExtraDataComponents.ITEM_KEY)?.id ?: get(ExtraDataComponents.DATA_CONTAINER)?.get(ItemDataTypes.ID)?.id

/**
 * 方便函数, 其行为等同于判断 [koishItem] 是否为空.
 * @see koishItem
 */
val MojangStack.isKoish: Boolean
    get() = koishItem != null

/**
 * 方便函数, 其行为等同于判断 [exactKoishItem] 是否为空.
 * @see exactKoishItem
 */
val MojangStack.isExactKoish: Boolean
    get() = exactKoishItem != null

/**
 * 方便函数, 其行为等同于判断该物品堆叠的物品类型是否存在套皮物品实例.
 * @see proxyKoishItem
 */
val MojangStack.isProxyKoish: Boolean
    get() = item.itemProxy != null

/**
 * 获取该物品堆叠的 *Koish 物品类型*.
 *
 * 返回非空的情况:
 * - 该物品堆叠是*Koish 物品*
 * - 该物品堆叠是*Koish 套皮物品*
 *
 * 返回空的情况:
 * - 其余所有情况
 *
 * *绝大多数情况下无需使用该函数.*
 */
val MojangStack.koishItem: KoishItem?
    // 最终版本
    //get() {
    //    val itemType = get(ExtraDataComponents.ITEM_KEY)?.itemType
    //    if (itemType == null || itemType.isEmpty()) {
    //        this.count = 0
    //        return null
    //    }
    //    return itemType
    //}
    // FIXME 临时版本, 用于过渡
    get() {
        val itemType = get(ExtraDataComponents.ITEM_KEY)?.itemType ?: get(ExtraDataComponents.DATA_CONTAINER)?.get(ItemDataTypes.ID)?.itemType ?: item.itemProxy
        if (itemType == null || itemType.isEmpty()) {
            this.count = 0
            return null
        }
        return itemType
    }

/**
 * 获取该物品堆叠的 *Koish 物品类型*.
 *
 * 返回非空的情况:
 * - 该物品堆叠是*Koish 物品*
 *
 * 返回空的情况:
 * - 该物品堆叠是*Koish 套皮物品*
 * - 该物品堆叠是*原版物品*
 * - 其余所有情况
 *
 * *绝大多数情况下无需使用该函数.*
 */
val MojangStack.exactKoishItem: KoishItem?
    // 最终版本
    //get() {
    //    val itemType = get(ExtraDataComponents.ITEM_KEY)?.itemType
    //    if (itemType == null || itemType.isEmpty()) {
    //        this.count = 0
    //        return null
    //    }
    //    return itemType
    //}
    // FIXME 临时版本
    get() {
        val itemType = get(ExtraDataComponents.ITEM_KEY)?.itemType ?: get(ExtraDataComponents.DATA_CONTAINER)?.get(ItemDataTypes.ID)?.itemType
        if (itemType == null || itemType.isEmpty()) {
            this.count = 0
            return null
        }
        return itemType
    }

/**
 * 获取该物品堆叠的套皮物品的实例.
 */
val MojangStack.proxyKoishItem: KoishItemProxy?
    get() = item.itemProxy

/**
 * 获取该物品堆叠的持久化数据容器 [ItemDataContainer].
 * 参数 [includeProxy] 可以控制是否包含套皮物品的容器.
 *
 * *绝大多数情况下无需使用该函数.*
 *
 * @param includeProxy 是否包含套皮物品的容器
 * @return 如果该物品堆叠是 Koish 物品, 则返回其数据容器
 *
 * @see hasData
 * @see getData
 * @see getDataOrDefault
 * @see setData
 * @see removeData
 */
fun MojangStack.dataContainer(includeProxy: Boolean): ItemDataContainer? =
    get(ExtraDataComponents.DATA_CONTAINER) ?: if (includeProxy) item.itemProxy?.data else null

/**
 * 获取该物品堆叠的持久化数据容器 [ItemMetaContainer].
 *
 * * *绝大多数情况下无需使用该函数.*
 */
fun MojangStack.metaContainer(): ItemMetaContainer? =
    koishItem?.dataConfig

/**
 * 获取该物品堆叠的属性数据容器 [ItemPropContainer].
 *
 * *绝大多数情况下无需使用该函数.*
 *
 * @return 如果该物品堆叠是 Koish 物品, 则返回其属性数据容器
 *
 * @see hasProp
 * @see getProp
 * @see getPropOrDefault
 */
fun MojangStack.propContainer(): ItemPropContainer? =
    koishItem?.properties

/* Property */

fun <T> MojangStack.getProp(type: ItemPropType<out T>): T? =
    propContainer()?.get(type)

fun <T> MojangStack.hasProp(type: ItemPropType<T>): Boolean =
    propContainer()?.has(type) == true

fun <T> MojangStack.getPropOrDefault(type: ItemPropType<out T>, fallback: T): T? =
    propContainer()?.getOrDefault(type, fallback)

/* ItemData */

fun MojangStack.hasData(type: ItemDataType<*>): Boolean =
    dataContainer(true)?.has(type) == true

fun <T> MojangStack.getData(type: ItemDataType<out T>): T? =
    dataContainer(true)?.get(type)

fun <T> MojangStack.getDataOrDefault(type: ItemDataType<out T>, fallback: T): T =
    dataContainer(true)?.getOrDefault(type, fallback) ?: fallback

/* ItemMeta */

fun <U : ItemMetaEntry<V>, V> MojangStack.getMeta(type: ItemMetaType<U, V>): U? =
    metaContainer()?.get(type)

fun <U : ItemMetaEntry<V>, V> MojangStack.hasMeta(type: ItemMetaType<U, V>): Boolean =
    metaContainer()?.has(type) == true

fun <U : ItemMetaEntry<V>, V> MojangStack.getMetaOrDefault(type: ItemMetaType<U, V>, fallback: U): U =
    metaContainer()?.getOrDefault(type, fallback) ?: fallback

/**
 * 向物品堆叠写入 Koish 数据 [T].
 *
 * 警告: 该函数无法(也不应该)修改套皮物品的数据.
 * 如果该物品堆叠是套皮物品, 该函数没有实际效果.
 *
 * @see setData
 */
fun <T> MojangStack.setData(type: ItemDataType<in T>, value: T): T? {
    val builder = dataContainer(false)?.toBuilder() ?: return null
    val oldVal = builder.set(type, value)
    set(ExtraDataComponents.DATA_CONTAINER, builder.build())
    return oldVal
}

/**
 * 向物品堆叠写入 Koish 数据 [Unit].
 *
 * @see setData
 */
fun MojangStack.setData(type: ItemDataType<Unit>): Boolean {
    return setData(type, Unit) != null
}

/**
 * 移除物品堆叠上的 Koish 数据 [T].
 *
 * 警告: 该函数无法(也不应该)修改套皮物品的数据.
 * 如果该物品堆叠是套皮物品, 该函数没有实际效果.
 */
fun <T> MojangStack.removeData(type: ItemDataType<out T>): T? {
    val builder = dataContainer(false)?.toBuilder() ?: return null
    val oldVal = builder.remove(type)
    set(ExtraDataComponents.DATA_CONTAINER, builder.build())
    return oldVal
}

/**
 * 设置该物品堆叠是否应该在网络发包时重写.
 */
@get:JvmName("isNetworkRewrite")
@set:JvmName("setNetworkRewrite")
var MojangStack.isNetworkRewrite: Boolean
    get() {
        return !hasData(ItemDataTypes.BYPASS_NETWORK_REWRITE)
    }
    set(value) {
        if (value) {
            removeData(ItemDataTypes.BYPASS_NETWORK_REWRITE)
        } else {
            setData(ItemDataTypes.BYPASS_NETWORK_REWRITE)
        }
    }

/**
 * 设置该物品堆叠存储于 ItemOrExact.Exact 中时, 是否在配方书移动物品时只匹配 Koish 唯一标识符.
 */
@get:JvmName("onlyCompareIdInRecipeBook")
@set:JvmName("onlyCompareIdInRecipeBook")
var MojangStack.onlyCompareIdInRecipeBook: Boolean
    get() {
        return !hasData(ItemDataTypes.ONLY_COMPARE_ID_IN_RECIPE_BOOK)
    }
    set(value) {
        if (value) {
            removeData(ItemDataTypes.ONLY_COMPARE_ID_IN_RECIPE_BOOK)
        } else {
            setData(ItemDataTypes.ONLY_COMPARE_ID_IN_RECIPE_BOOK)
        }
    }

// -----------------
// 内部实现
// -----------------
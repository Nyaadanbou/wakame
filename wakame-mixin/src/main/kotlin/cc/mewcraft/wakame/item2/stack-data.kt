@file:JvmName("KoishStackData")

package cc.mewcraft.wakame.item2

import cc.mewcraft.wakame.item2.config.datagen.ItemMetaContainer
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaType
import cc.mewcraft.wakame.item2.config.property.ItemPropertyContainer
import cc.mewcraft.wakame.item2.config.property.ItemPropertyType
import cc.mewcraft.wakame.item2.data.ItemDataContainer
import cc.mewcraft.wakame.item2.data.ItemDataType
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.mixin.support.ExtraDataComponents
import cc.mewcraft.wakame.registry2.BuiltInRegistries
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

val ItemStack.typeId: Identifier get() = toNMS().typeId
val ItemStack.koishTypeId: Identifier? get() = toNMS().koishTypeId
val ItemStack.isKoish: Boolean get() = toNMS().isKoish
val ItemStack.isExactKoish: Boolean get() = toNMS().isExactKoish
val ItemStack.koishItem: KoishItem? get() = toNMS().koishItem
fun ItemStack.dataContainer(includeProxy: Boolean): ItemDataContainer? = toNMS().dataContainer(includeProxy)
fun ItemStack.dataConfig(): ItemMetaContainer? = toNMS().dataConfig()
fun ItemStack.propertyContainer(): ItemPropertyContainer? = toNMS().propertyContainer()
val Material.koishProxy: KoishItemProxy? get() = BuiltInRegistries.ITEM_PROXY[key()]

/* Property */

fun <T> ItemStack.getProp(type: ItemPropertyType<out T>): T? = toNMS().getProp(type)
fun <T> ItemStack.hasProp(type: ItemPropertyType<T>): Boolean = toNMS().hasProp(type)
fun <T> ItemStack.getPropOrDefault(type: ItemPropertyType<out T>, fallback: T): T? = toNMS().getPropOrDefault(type, fallback)

@Deprecated("Use getProp instead", ReplaceWith("hasProp(type)"))
fun <T> ItemStack.hasProperty(type: ItemPropertyType<T>): Boolean = hasProp(type)
@Deprecated("Use hasProp instead", ReplaceWith("getProp(type)"))
fun <T> ItemStack.getProperty(type: ItemPropertyType<out T>): T? = getProp(type)
@Deprecated("Use getPropOrDefault instead", ReplaceWith("getPropOrDefault(type, fallback)"))
fun <T> ItemStack.getPropertyOrDefault(type: ItemPropertyType<out T>, fallback: T): T? = toNMS().getPropOrDefault(type, fallback)

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
 * 获取该物品堆叠的物品类型的 [Identifier].
 *
 * - 如果该物品堆叠是一个 *Koish 物品* , 则命名空间为 [cc.mewcraft.wakame.util.KOISH_NAMESPACE]
 * - 如果该物品堆叠是一个 *原版(或 Koish 套皮)物品*, 则命名空间为 [cc.mewcraft.wakame.util.MINECRAFT_NAMESPACE]
 * - 如果该物品堆叠来自其他物品系统, 将被当作是一个 *原版物品*, 命名空间为 [cc.mewcraft.wakame.util.MINECRAFT_NAMESPACE] // TODO 支持 ItemRef
 */
val MojangStack.typeId: Identifier
    get() = dataContainer(false)?.get(ItemDataTypes.ID)?.id ?: CraftItemType.minecraftToBukkit(item).key()

/**
 * 获取该物品堆叠的物品类型的 [Identifier].
 *
 * - 只有当该物品堆叠属于 Koish 物品库时才返回非空值.
 * - 不包含套皮物品/原版物品 - 这种情况会直接返回空值.
 */
val MojangStack.koishTypeId: Identifier?
    get() = dataContainer(false)?.get(ItemDataTypes.ID)?.id

/**
 * 方便函数, 其行为等同于判断 [koishItem] 是否为空.
 * @see koishItem
 */
val MojangStack.isKoish: Boolean
    get() = dataContainer(true)?.has(ItemDataTypes.ID) == true

val MojangStack.isExactKoish: Boolean
    get() = dataContainer(false)?.has(ItemDataTypes.ID) == true

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
    get() = dataContainer(true)?.get(ItemDataTypes.ID)?.itemType

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
    get() = dataContainer(false)?.get(ItemDataTypes.ID)?.itemType

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
    get(ExtraDataComponents.DATA_CONTAINER) ?: if (includeProxy) item.koishProxy?.data else null

/**
 * 获取该物品堆叠的持久化数据容器 [ItemMetaContainer].
 *
 * * *绝大多数情况下无需使用该函数.*
 */
fun MojangStack.dataConfig(): ItemMetaContainer? =
    koishItem?.dataConfig

/**
 * 获取该物品堆叠的属性数据容器 [ItemPropertyContainer].
 *
 * *绝大多数情况下无需使用该函数.*
 *
 * @return 如果该物品堆叠是 Koish 物品, 则返回其属性数据容器
 *
 * @see hasProperty
 * @see getProperty
 * @see getPropertyOrDefault
 */
fun MojangStack.propertyContainer(): ItemPropertyContainer? =
    koishItem?.properties

/**
 * 获取该物品类型的套皮物品的实例.
 *
 * *绝大多数情况下无需使用该函数.*
 */
val Item.koishProxy: KoishItemProxy?
    get() = CraftItemType.minecraftToBukkit(this).koishProxy

/* Property */

fun <T> MojangStack.getProp(type: ItemPropertyType<out T>): T? =
    propertyContainer()?.get(type)

fun <T> MojangStack.hasProp(type: ItemPropertyType<T>): Boolean =
    propertyContainer()?.has(type) == true

fun <T> MojangStack.getPropOrDefault(type: ItemPropertyType<out T>, fallback: T): T? =
    propertyContainer()?.getOrDefault(type, fallback)

@Deprecated("Use getProp instead", ReplaceWith("getProp(type)"))
fun <T> MojangStack.getProperty(type: ItemPropertyType<out T>): T? =
    getProp(type)

@Deprecated("Use hasProp instead", ReplaceWith("hasProp(type)"))
fun <T> MojangStack.hasProperty(type: ItemPropertyType<T>): Boolean =
    hasProp(type)

@Deprecated("Use getPropOrDefault instead", ReplaceWith("getPropOrDefault(type, fallback)"))
fun <T> MojangStack.getPropertyOrDefault(type: ItemPropertyType<out T>, fallback: T): T? =
    getPropOrDefault(type, fallback)

/* ItemData */

fun MojangStack.hasData(type: ItemDataType<*>): Boolean =
    dataContainer(true)?.has(type) == true

fun <T> MojangStack.getData(type: ItemDataType<out T>): T? =
    dataContainer(true)?.get(type)

fun <T> MojangStack.getDataOrDefault(type: ItemDataType<out T>, fallback: T): T =
    dataContainer(true)?.getOrDefault(type, fallback) ?: fallback

/* ItemMeta */

fun <U : ItemMetaEntry<V>, V> MojangStack.getMeta(type: ItemMetaType<U, V>): U? =
    dataConfig()?.get(type)

fun <U : ItemMetaEntry<V>, V> MojangStack.hasMeta(type: ItemMetaType<U, V>): Boolean =
    dataConfig()?.has(type) == true

fun <U : ItemMetaEntry<V>, V> MojangStack.getMetaOrDefault(type: ItemMetaType<U, V>, fallback: U): U =
    dataConfig()?.getOrDefault(type, fallback) ?: fallback

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

// -----------------
// 内部实现
// -----------------
@file:JvmName("KoishStackData")

package cc.mewcraft.wakame.item2

import cc.mewcraft.wakame.item2.config.property.ItemPropertyType
import cc.mewcraft.wakame.item2.data.ItemDataContainer
import cc.mewcraft.wakame.item2.data.ItemDataType
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.mixin.support.DataComponentsPatch
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.item.toNMS
import net.minecraft.world.item.Item
import org.bukkit.Material
import org.bukkit.craftbukkit.inventory.CraftItemType
import org.bukkit.inventory.ItemStack


// ------------------
// 用于访问 `org.bukkit.inventory.ItemStack` 上的自定义数据
// (关于注释和文档, 请参考下面的 MojangStack 的扩展函数)
// ------------------

//// Type

val ItemStack.typeId: Identifier get() = toNMS().typeId
val ItemStack.isKoish: Boolean get() = toNMS().isKoish
val ItemStack.koishItem: KoishItem? get() = toNMS().koishItem
fun ItemStack.koishData(includeProxy: Boolean): ItemDataContainer? = toNMS().koishData(includeProxy)
val Material.koishProxy: KoishItemProxy? get() = KoishRegistries2.ITEM_PROXY[key()]

//// Property

fun <T> ItemStack.hasProperty(type: ItemPropertyType<T>): Boolean = toNMS().hasProperty(type)
fun <T> ItemStack.getProperty(type: ItemPropertyType<out T>): T? = toNMS().getProperty(type)

//// ItemData

fun ItemStack.hasData(type: ItemDataType<*>): Boolean = toNMS().hasData(type)
fun <T> ItemStack.getData(type: ItemDataType<out T>): T? = toNMS().getData(type)
fun <T> ItemStack.getDataOrDefault(type: ItemDataType<out T>, fallback: T): T? = toNMS().getDataOrDefault(type, fallback)
fun <T> ItemStack.setData(type: ItemDataType<in T>, value: T): T? = toNMS().setData(type, value)
fun <T> ItemStack.removeData(type: ItemDataType<out T>): T? = toNMS().removeData(type)

// ------------------
// 用于访问 `net.minecraft.world.item.ItemStack` 上的自定义数据
// ------------------

//// Type

/**
 * 获取该物品堆叠的物品类型的 [Identifier].
 *
 * - 如果该物品堆叠是一个 *Koish 物品* , 则命名空间为 [cc.mewcraft.wakame.util.KOISH_NAMESPACE]
 * - 如果该物品堆叠是一个 *原版(或 Koish 套皮)物品*, 则命名空间为 [cc.mewcraft.wakame.util.MINECRAFT_NAMESPACE]
 * - 如果该物品堆叠来自其他物品系统, 将被当作是一个 *原版物品*, 命名空间为 [cc.mewcraft.wakame.util.MINECRAFT_NAMESPACE]
 */
val MojangStack.typeId: Identifier
    get() = koishData(false)?.get(ItemDataTypes.ID)?.id ?: CraftItemType.minecraftToBukkit(item).key()

val MojangStack.isKoish: Boolean
    get() = koishItem != null

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
    get() = koishData(true)?.get(ItemDataTypes.ID)?.itemType

/**
 * 获取该物品堆叠的持久化数据容器 [ItemDataContainer].
 * 参数 [includeProxy] 可以控制是否包含套皮物品的容器.
 *
 * *绝大多数情况下无需使用该函数.*
 *
 * @see hasData
 * @see getData
 * @see getDataOrDefault
 * @see setData
 * @see removeData
 */
fun MojangStack.koishData(includeProxy: Boolean): ItemDataContainer? =
    get(DataComponentsPatch.DATA_CONTAINER) ?: if (includeProxy) item.koishProxy?.data else null

/**
 * 获取该物品类型的套皮物品的实例.
 *
 * *绝大多数情况下无需使用该函数.*
 */
val Item.koishProxy: KoishItemProxy?
    get() = CraftItemType.minecraftToBukkit(this).koishProxy

//// Property

fun <T> MojangStack.getProperty(type: ItemPropertyType<out T>): T? =
    koishItem?.properties?.get(type)

fun <T> MojangStack.hasProperty(type: ItemPropertyType<T>): Boolean =
    koishItem?.properties?.has(type) == true

//// ItemData

fun MojangStack.hasData(type: ItemDataType<*>): Boolean =
    koishData(true)?.has(type) == true

fun <T> MojangStack.getData(type: ItemDataType<out T>): T? =
    koishData(true)?.get(type)

fun <T> MojangStack.getDataOrDefault(type: ItemDataType<out T>, fallback: T): T? =
    koishData(true)?.getOrDefault(type, fallback)

/**
 * 向物品堆叠写入 Koish 数据 [T].
 *
 * 警告: 该函数无法(也不应该)修改套皮物品的数据.
 * 如果该物品堆叠是套皮物品, 该函数将没有实际效果.
 */
fun <T> MojangStack.setData(type: ItemDataType<in T>, value: T): T? {
    val builder = koishData(false)?.toBuilder() ?: return null
    val oldVal = builder.set(type, value)
    set(DataComponentsPatch.DATA_CONTAINER, builder.build())
    return oldVal
}

/**
 * 移除物品堆叠上的 Koish 数据 [T].
 *
 * 警告: 该函数无法(也不应该)修改套皮物品的数据.
 * 如果该物品堆叠是套皮物品, 该函数将没有实际效果.
 */
fun <T> MojangStack.removeData(type: ItemDataType<out T>): T? {
    val builder = koishData(false)?.toBuilder() ?: return null
    val oldVal = builder.remove(type)
    set(DataComponentsPatch.DATA_CONTAINER, builder.build())
    return oldVal
}

// -----------------
// 内部实现
// -----------------

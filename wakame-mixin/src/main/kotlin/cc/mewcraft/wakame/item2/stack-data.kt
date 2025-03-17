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
import cc.mewcraft.wakame.util.item.unwrapToMojang
import net.minecraft.world.item.Item
import org.bukkit.Material
import org.bukkit.craftbukkit.inventory.CraftItemType
import org.bukkit.inventory.ItemStack


// ------------------
// 用于访问 `org.bukkit.inventory.ItemStack` 上的自定义数据
// ------------------

//// Type

val ItemStack.isKoish: Boolean get() = unwrapToMojang().isKoish
val ItemStack.koish: KoishItem? get() = unwrapToMojang().koish
val ItemStack.typeId: Identifier? get() = unwrapToMojang().typeId
internal val ItemStack.dataContainer: ItemDataContainer? get() = unwrapToMojang().dataContainer
val Material.koishProxy: KoishItemProxy? get() = KoishRegistries2.ITEM_PROXY[key()]

//// Property

fun <T> ItemStack.hasProperty(type: ItemPropertyType<T>): Boolean = unwrapToMojang().hasProperty(type)
fun <T> ItemStack.getProperty(type: ItemPropertyType<out T>): T? = unwrapToMojang().getProperty(type)

//// ItemData

fun ItemStack.hasData(type: ItemDataType<*>): Boolean = unwrapToMojang().hasData(type)
fun <T> ItemStack.getData(type: ItemDataType<out T>): T? = unwrapToMojang().getData(type)
fun <T> ItemStack.getDataOrDefault(type: ItemDataType<out T>, fallback: T): T? = unwrapToMojang().getDataOrDefault(type, fallback)
fun <T> ItemStack.setData(type: ItemDataType<in T>, value: T): T? = unwrapToMojang().setData(type, value)
fun <T> ItemStack.removeData(type: ItemDataType<out T>): T? = unwrapToMojang().removeData(type)

// ------------------
// 用于访问 `net.minecraft.world.item.ItemStack` 上的自定义数据
// ------------------

//// Type

val MojangStack.isKoish: Boolean
    get() = koish != null

/**
 * 获取该物品堆叠的 *Koish 物品类型*.
 *
 * 对于 *自定义物品堆叠* 和 *套皮物品堆叠*, 该函数不会返回 `null`.
 */
val MojangStack.koish: KoishItem?
    get() = dataContainer(true)?.get(ItemDataTypes.ID)?.itemType

/**
 * 获取该物品堆叠的类型的 [Identifier].
 *
 * - 如果该物品堆叠是一个 *自定义物品* , 则命名空间为 `koish`
 * - 如果该物品堆叠是一个 *原版(或套皮)物品*, 则命名空间为 `minecraft`
 */
val MojangStack.typeId: Identifier?
    get() = dataContainer(true)?.get(ItemDataTypes.ID)?.id

/**
 * 获取该物品堆叠的持久化数据容器.
 * 该函数对于套皮物品堆叠会返回 `null`.
 */
internal val MojangStack.dataContainer: ItemDataContainer?
    get() = get(DataComponentsPatch.ITEM_DATA_CONTAINER)

/**
 * 获取该物品堆叠的持久化数据容器.
 *
 * @param includeProxy 是否包含套皮物品堆叠的持久化数据容器
 */
private fun MojangStack.dataContainer(includeProxy: Boolean): ItemDataContainer? =
    get(DataComponentsPatch.ITEM_DATA_CONTAINER) ?: if (includeProxy) item.koishProxy?.data else null

/**
 * 获取该物品类型的套皮物品实例.
 */
val Item.koishProxy: KoishItemProxy?
    get() = KoishRegistries2.ITEM_PROXY[CraftItemType.minecraftToBukkit(this).key() /*获得一个 Item 的命名空间形式的 ID*/]

//// Property

fun <T> MojangStack.getProperty(type: ItemPropertyType<out T>): T? =
    koish?.properties?.get(type)

fun <T> MojangStack.hasProperty(type: ItemPropertyType<T>): Boolean =
    koish?.properties?.has(type) == true

//// ItemData

fun MojangStack.hasData(type: ItemDataType<*>): Boolean =
    dataContainer(true)?.has(type) == true

fun <T> MojangStack.getData(type: ItemDataType<out T>): T? =
    dataContainer(true)?.get(type)

fun <T> MojangStack.getDataOrDefault(type: ItemDataType<out T>, fallback: T): T? =
    dataContainer(true)?.getOrDefault(type, fallback)

/**
 * 向物品堆叠写入自定义数据 [T].
 * 对于套皮物品堆叠该函数相当于 `no-op`.
 */
fun <T> MojangStack.setData(type: ItemDataType<in T>, value: T): T? {
    val builder = dataContainer?.toBuilder() ?: return null
    //blockWriteToItemProxy(builder)
    val oldVal = builder.set(type, value)
    set(DataComponentsPatch.ITEM_DATA_CONTAINER, builder.build())
    return oldVal
}

/**
 * 移除物品堆叠上的自定义数据 [T].
 * 对于套皮物品堆叠该函数相当于 `no-op`.
 */
fun <T> MojangStack.removeData(type: ItemDataType<out T>): T? {
    val builder = dataContainer?.toBuilder() ?: return null
    //blockWriteToItemProxy(builder)
    val oldVal = builder.remove(type)
    set(DataComponentsPatch.ITEM_DATA_CONTAINER, builder.build())
    return oldVal
}

//private fun MojangStack.blockWriteToItemProxy(container: ItemDataContainer) {
//    val koishProxy = container[ItemDataTypes.ID]?.itemProxy
//    if (koishProxy != null) throw IllegalStateException("Cannot write koish data to an item proxy")
//}

// -----------------
// 内部实现
// -----------------

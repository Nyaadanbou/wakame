package cc.mewcraft.wakame.item2

import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.config.property.ItemPropertyType
import cc.mewcraft.wakame.item2.data.ItemDataContainer
import cc.mewcraft.wakame.item2.data.ItemDataType
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.mixin.support.DataComponentsPatch
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.item.unwrapToMojang
import org.bukkit.craftbukkit.inventory.CraftItemType
import org.bukkit.inventory.ItemStack

// ------------------
// 用于访问 `net.minecraft.world.item.ItemStack` 上的自定义数据
// ------------------

//// Base

val MojangStack.isKoish: Boolean
    get() = koishItem != null

val MojangStack.koishItem: KoishItem?
    get() = dataContainer?.get(ItemDataTypes.ID)?.koishItem

val MojangStack.dataContainer: ItemDataContainer?
    get() = get(DataComponentsPatch.ITEM_DATA_CONTAINER) ?: koishProxy?.data

val MojangStack.koishProxy: KoishItemProxy?
    get() = KoishRegistries2.ITEM_PROXY[id]

//// Behavior

fun MojangStack.hasBehavior(behavior: ItemBehavior): Boolean =
    koishItem?.behaviors?.has(behavior) == true

//// Property

fun <T> MojangStack.getProperty(type: ItemPropertyType<out T>): T? =
    koishItem?.properties?.get(type)

fun <T> MojangStack.hasProperty(type: ItemPropertyType<T>): Boolean =
    koishItem?.properties?.has(type) == true

//// ItemData

fun MojangStack.hasData(type: ItemDataType<*>): Boolean =
    dataContainer?.has(type) == true

fun <T> MojangStack.getData(type: ItemDataType<out T>): T? =
    dataContainer?.get(type)

fun <T> MojangStack.getDataOrDefault(type: ItemDataType<out T>, fallback: T): T? =
    dataContainer?.getOrDefault(type, fallback)

fun <T> MojangStack.setData(type: ItemDataType<in T>, value: T): T? {
    blockWriteToProxy()

    val builder = dataContainer?.toBuilder() ?: return null
    val oldValue = builder.set(type, value)
    set(DataComponentsPatch.ITEM_DATA_CONTAINER, builder.build())
    return oldValue
}

fun <T> MojangStack.removeData(type: ItemDataType<out T>): T? {
    blockWriteToProxy()

    val builder = dataContainer?.toBuilder() ?: return null
    val oldValue = builder.remove(type)
    set(DataComponentsPatch.ITEM_DATA_CONTAINER, builder.build())
    return oldValue
}

private fun MojangStack.blockWriteToProxy() {
    if (koishProxy != null) throw IllegalStateException("Cannot write data on ${KoishItemProxy::class.simpleName}")
}

// ------------------
// 用于访问 `org.bukkit.inventory.ItemStack` 上的自定义数据
// ------------------

//// Base

val ItemStack.isKoish: Boolean get() = unwrapToMojang().isKoish
val ItemStack.koishItem: KoishItem? get() = unwrapToMojang().koishItem
val ItemStack.dataContainer: ItemDataContainer? get() = unwrapToMojang().dataContainer
val ItemStack.koishProxy: KoishItemProxy? get() = unwrapToMojang().koishProxy

//// Behavior

fun ItemStack.hasBehavior(behavior: ItemBehavior): Boolean = unwrapToMojang().hasBehavior(behavior)

//// Property

fun <T> ItemStack.hasProperty(type: ItemPropertyType<T>): Boolean = unwrapToMojang().hasProperty(type)
fun <T> ItemStack.getProperty(type: ItemPropertyType<out T>): T? = unwrapToMojang().getProperty(type)

//// ItemData

fun ItemStack.hasData(type: ItemDataType<*>): Boolean = unwrapToMojang().hasData(type)
fun <T> ItemStack.getData(type: ItemDataType<out T>): T? = unwrapToMojang().getData(type)
fun <T> ItemStack.getDataOrDefault(type: ItemDataType<out T>, fallback: T): T? = unwrapToMojang().getDataOrDefault(type, fallback)
fun <T> ItemStack.setData(type: ItemDataType<in T>, value: T): T? = unwrapToMojang().setData(type, value)
fun <T> ItemStack.removeData(type: ItemDataType<out T>): T? = unwrapToMojang().removeData(type)

// -----------------
// 方便函数
// -----------------

// 获得一个 ItemStack 的字符串形式的命名空间 ID
private val MojangStack.id: Identifier
    get() = CraftItemType.minecraftToBukkit(item).key()

package cc.mewcraft.wakame.item2

import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.behavior.ItemBehaviorContainer
import cc.mewcraft.wakame.item2.config.property.GlobalPropertyType
import cc.mewcraft.wakame.item2.data.ItemDataContainer
import cc.mewcraft.wakame.item2.data.ItemDataType
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.mixin.support.DataComponentsPatch
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.item.unwrapToMojang
import org.bukkit.inventory.ItemStack

// 代表一个物品类型(由配置文件动态创建)
class KoishItem(
    val id: Identifier,
    val base: ItemBase, // TODO #350: 可作为 GlobalProperty
    val slot: ItemSlot, // TODO #350: 可作为 GlobalProperty
    val hidden: Boolean, // TODO #350: 可作为 GlobalProperty
    val behaviors: ItemBehaviorContainer,
) {

}

// KoishItem 作为物品的类型会生成许多实例, 这些实例存在的形式有两种:
// 1) NMS ItemStack 形式, 也就是实际存在于游戏世界内的物品堆叠
// 2) KoishItemProxy 形式, 也就是一个物品堆叠(通常是原版)的代理
// 无论是哪一种形式, 我们都需要读取其相关数据, 只不过方式不一样:
// 1) 如果是 NMS ItemStack 形式, 则是从其 DataComponentMap 读取相关数据
// 2) 如果是 KoishItemProxy 形式, 则是从其 ItemDataContainer 读取相关数据 (具体看 KoishItemProxy 的定义)

// 表示一个原版套皮物品
// 一个套皮物品需要一个最基本的物品类型 (KoishItem) 以及在一开始就确定好的数据 (ItemDataContainer)
class KoishItemProxy(
    val type: KoishItem,
    val data: ItemDataContainer,
) {
    // ------------------
    // 方便函数
    // ------------------

    val id: Identifier get() = type.id
    val base: ItemBase get() = type.base
    val slot: ItemSlot get() = type.slot
    val hidden: Boolean get() = type.hidden
}

// 而对于一个 NMS ItemStack, 我们并不是必须为其抽象出一个 class 或 interface.
// 我们只需要能够访问物品堆叠上的 ItemDataContainer 即可访问所有自定义添加的数据,
// 并且这个过程中不需要涉及任何数据结构的转换.

// ------------------
// 用于访问 MojangStack 上的自定义数据
// ------------------

//// Base

// TODO #350: 考虑原版套皮物品
val MojangStack.koishItem: KoishItem?
    get() = getData(ItemDataTypes.ID)?.koishItem

// TODO #350: 考虑原版套皮物品
val MojangStack.dataContainer: ItemDataContainer?
    get() = get(DataComponentsPatch.ITEM_DATA_CONTAINER)


//// Behavior

fun MojangStack.hasBehavior(behavior: ItemBehavior): Boolean = TODO("#350")

//// Property

fun <T> MojangStack.getProperty(type: GlobalPropertyType<out T>): T? = TODO("#350")

fun <T> MojangStack.hasProperty(type: GlobalPropertyType<T>): Boolean = getProperty(type) != null

//// ItemData

// TODO #350: 考虑原版套皮物品
fun MojangStack.hasData(type: ItemDataType<*>): Boolean = this@hasData.dataContainer?.has(type) == true

// TODO #350: 考虑原版套皮物品
fun <T> MojangStack.getData(type: ItemDataType<out T>): T? = this@getData.dataContainer?.get(type)

// TODO #350: 考虑原版套皮物品
fun <T> MojangStack.getDataOrDefault(type: ItemDataType<out T>, fallback: T): T? = this@getDataOrDefault.dataContainer?.getOrDefault(type, fallback)

// TODO #350: 考虑原版套皮物品
fun <T> MojangStack.setData(type: ItemDataType<in T>, value: T): T? = this@setData.dataContainer?.set(type, value)

// TODO #350: 考虑原版套皮物品
fun <T> MojangStack.removeData(type: ItemDataType<out T>): T? = this@removeData.dataContainer?.remove(type)

// ------------------
// 用于访问 ItemStack 上的自定义数据
// ------------------

//// Base

val ItemStack.koishItem: KoishItem? get() = unwrapToMojang().koishItem

val ItemStack.dataContainer: ItemDataContainer? get() = unwrapToMojang().dataContainer

//// Behavior

fun ItemStack.hasBehavior(behavior: ItemBehavior): Boolean = unwrapToMojang().hasBehavior(behavior)

//// Property

fun <T> ItemStack.hasProperty(type: GlobalPropertyType<T>): Boolean = unwrapToMojang().hasProperty(type)

fun <T> ItemStack.getProperty(type: GlobalPropertyType<out T>): T? = unwrapToMojang().getProperty(type)

//// ItemData

fun ItemStack.hasData(type: ItemDataType<*>): Boolean = unwrapToMojang().hasData(type)

fun <T> ItemStack.getData(type: ItemDataType<out T>): T? = unwrapToMojang().getData(type)

fun <T> ItemStack.getDataOrDefault(type: ItemDataType<out T>, fallback: T): T? = unwrapToMojang().getDataOrDefault(type, fallback)

fun <T> ItemStack.setData(type: ItemDataType<in T>, value: T): T? = unwrapToMojang().setData(type, value)

fun <T> ItemStack.removeData(type: ItemDataType<out T>): T? = unwrapToMojang().removeData(type)

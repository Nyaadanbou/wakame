@file:JvmName("KoishItemType")

package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.behavior.ItemBehaviorContainer
import cc.mewcraft.wakame.item.data.ItemDataContainer
import cc.mewcraft.wakame.item.datagen.ItemMetaContainer
import cc.mewcraft.wakame.item.property.ItemPropContainer
import cc.mewcraft.wakame.item.property.ItemPropType
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.adventure.toSimpleString
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream


// ------------
// 物品类型
// ------------

// KoishItem 作为物品的[类型]会生成许多实例, 这些实例存在的形式有两种:
// 1) NMS ItemStack 形式, 也就是实际存在于游戏世界内的物品堆叠
// 2) KoishItemProxy 形式, 也就是一个物品堆叠(通常是原版)的代理
// 无论是哪一种形式, 我们都需要读取其相关数据, 只不过方式不一样:
// 1) 如果是 NMS ItemStack 形式, 则从其 DataComponentMap 读取相关数据
// 2) 如果是 KoishItemProxy 形式, 则从其 ItemDataContainer 读取相关数据 (具体看 KoishItemProxy 的定义)
//
// 需要注意的是, 对于 NMS ItemStack 形式的 KoishItem “实例”, 我们不需要专门定一个 class 去表示.
// 这是因为在新的框架下, NMS ItemStack 上已经存在所有需要的数据, 并且这些数据都是直接的, 不需要再转换.
// 我们只需要能够访问物品堆叠上的 ItemDataContainer 即可访问所有自定义添加的数据,
// 并且这个过程中不需要涉及任何数据结构的转换. 相比基于 NBT 的实现, 这简直快到飞起!

/**
 * 代表一个物品类型(由配置文件创建).
 *
 * @see cc.mewcraft.wakame.registry.BuiltInRegistries.ITEM 包含本类型实例的注册表
 */
open class KoishItem(
    val id: Identifier,
    val dataConfig: ItemMetaContainer,
    val properties: ItemPropContainer,
    val behaviors: ItemBehaviorContainer,
) : Examinable {

    override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
        ExaminableProperty.of("id", id),
    )

    override fun toString(): String = toSimpleString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KoishItem) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}

/**
 * 该物品类型的名字, 可用于展示给玩家.
 *
 * 该物品类型的配置文件必须指定了 [ItemPropertyTypes.NAME], 否则将使用物品 ID 作为返回值.
 */
val KoishItem.name: Component get() = properties.getOrDefault(ItemPropTypes.NAME, Component.text(id.asString()))
fun <T> KoishItem.hasProperty(type: ItemPropType<T>): Boolean = properties.has(type)
fun <T> KoishItem.getProperty(type: ItemPropType<out T>): T? = properties[type]
fun <T> KoishItem.getPropertyOrDefault(type: ItemPropType<T>, default: T): T = properties.getOrDefault(type, default)

/**
 * 表示一个套皮物品(由配置文件创建).
 *
 * 一个套皮物品需要一个最基本的物品类型 (即 [KoishItem], 这也是为什么这个类继承自 [KoishItem])
 * 以及在一开始就确定好的自定义数据 (即 [ItemDataContainer], 对应这个类的 [data] 字段).
 *
 * @see cc.mewcraft.wakame.registry.BuiltInRegistries.ITEM_PROXY 包含本类型实例的注册表
 */
class KoishItemProxy(
    id: Identifier,
    dataConfig: ItemMetaContainer,
    properties: ItemPropContainer,
    behaviors: ItemBehaviorContainer,
    val data: ItemDataContainer,
) : KoishItem(id, dataConfig, properties, behaviors) {

    override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
        ExaminableProperty.of("id", id),
    )

    override fun toString(): String = toSimpleString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KoishItemProxy) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}

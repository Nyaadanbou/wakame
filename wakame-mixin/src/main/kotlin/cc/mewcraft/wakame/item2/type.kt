package cc.mewcraft.wakame.item2

import cc.mewcraft.wakame.item2.behavior.ItemBehaviorContainer
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaContainer
import cc.mewcraft.wakame.item2.config.property.ItemPropertyContainer
import cc.mewcraft.wakame.item2.data.ItemDataContainer
import cc.mewcraft.wakame.util.Identifier

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

// 代表一个物品类型(由配置文件动态创建)
class KoishItem(
    val id: Identifier,
    val dataConfig: ItemMetaContainer,
    val properties: ItemPropertyContainer,
    val behaviors: ItemBehaviorContainer,
)

// 表示一个原版套皮物品
// 一个套皮物品需要一个最基本的物品类型 (KoishItem) 以及在一开始就确定好的数据 (ItemDataContainer)
class KoishItemProxy(
    val type: KoishItem,
    val data: ItemDataContainer,
)
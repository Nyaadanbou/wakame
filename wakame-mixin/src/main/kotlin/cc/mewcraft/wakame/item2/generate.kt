@file:JvmName("KoishStackGeneratorSupport")

package cc.mewcraft.wakame.item2

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.item2.config.datagen.Context
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaContainer
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaType
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.config.property.impl.ItemBase
import cc.mewcraft.wakame.item2.data.ItemDataContainer
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.item2.data.impl.ItemId
import cc.mewcraft.wakame.mixin.support.ExtraDataComponents
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.item.toBukkit
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.inventory.ItemStack
import kotlin.time.measureTimedValue


// ------------
// 物品生成
// ------------

/**
 * 本单例负责从物品类型 [KoishItem] 生成一个物品实例 [ItemStack].
 *
 * 生成的用途分为两种:
 * 1. 生成出来的 [ItemStack] 直接放置在游戏世界内, 例如合成和战利品箱.
 * 2. 生成出来的 [ItemStack] 充当原版套皮物品的实例.
 *
 * 对于第一种, 我们需要生成一个完整的 [ItemStack], 里面包含了 [ItemDataContainer].
 * 对于第二种, 我们只需要生成的 [ItemDataContainer], 其余信息可以丢掉 (GC).
 *
 * 也就是说, 第一种包括了第二种, 因此可以共用一个实现.
 *
 * ### 对于终端用户 (编写物品配置文件的人)
 * 如果编写的物品配置文件是用于套皮物品, 务必确保配置文件所生成的数据不具有*随机性*.
 */
object KoishStackGenerator {

    /**
     * 基于上下文 [Context] 从物品类型 [KoishItem] 生成一个新的 [ItemStack] 实例.
     *
     * @param type 物品类型
     * @param context 生成物品的上下文
     * @return 新生成的 [ItemStack]
     */
    fun generate(type: KoishItem, context: Context): ItemStack {
        val result = measureTimedValue { generate0(type, context) }
        LOGGER.info(Component.text("Generated item in ${result.duration.inWholeMilliseconds}ms").color(NamedTextColor.DARK_GRAY))
        return result.value
    }

    private fun generate0(type: KoishItem, context: Context): ItemStack {
        val dataContainer = ItemDataContainer.builder()

        // 写入基础信息, 每个自定义物品都有
        dataContainer[ItemDataTypes.ID] = ItemId.of(type.id)
        dataContainer[ItemDataTypes.VERSION] = 0 // FIXME #350: 实现数据迁移系统
        dataContainer[ItemDataTypes.VARIANT] = 0

        // 测试写入类型为 Unit 的数据
        dataContainer[ItemDataTypes.BYPASS_NETWORK_REWRITE] = Unit

        // 直接操作 MojangStack 以提高生成物品的速度
        val itembase = type.properties.getOrDefault(ItemPropertyTypes.BASE, ItemBase.EMPTY)
        val itemstack = itembase.createMojang()

        // 在把 ItemStack 传递到 ItemMetaEntry 之前, 需要先将 ItemDataContainer 写入到 ItemStack.
        // 否则按照目前的实现, 简单的使用 ItemStack.setData 是无法将数据写入到 ItemStack 的,
        // 因为 ItemStack.setData 只有在 ItemDataContainer 存在时才能写入数据
        itemstack.set(ExtraDataComponents.DATA_CONTAINER, dataContainer.build())

        // 获取 ItemData 的“配置文件” (ItemMetaContainer)
        val dataConfig = type.dataConfig

        // 从 ItemMetaContainer 生成数据, 然后写入到物品堆叠上
        for (metaType in BuiltInRegistries.ITEM_META_TYPE) {
            makePersistentDataThenWrite(metaType, dataConfig, itemstack, context)
        }

        return itemstack.toBukkit()
    }

    private fun <U, V> makePersistentDataThenWrite(
        metaType: ItemMetaType<U, V>,
        metaContainer: ItemMetaContainer,
        itemstack: MojangStack,
        context: Context,
    ) {
        val entry = metaContainer[metaType]
        if (entry != null) {
            val result = entry.make(context)
            if (result.isPresent()) {
                val value = result.unwrap()
                entry.write(value, itemstack)
            }
        }
    }

}
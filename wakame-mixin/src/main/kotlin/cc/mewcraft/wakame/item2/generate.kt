package cc.mewcraft.wakame.item2

import cc.mewcraft.wakame.item2.config.datagen.Context
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaContainer
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaType
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.data.ItemDataContainer
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.item2.data.impl.ItemId
import cc.mewcraft.wakame.mixin.support.DataComponentsPatch
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.item.wrapToBukkit
import net.minecraft.world.item.Items
import org.bukkit.inventory.ItemStack


// ------------
// 物品生成
// ------------

/**
 * 本单例负责从物品类型 [KoishItem] 生成一个物品实例 [ItemStack].
 *
 * 生成的用途分为两种:
 * 1. 生成出来的 [ItemStack] 将直接放置在游戏世界内, 例如合成和战利品箱.
 * 2. 生成出来的 [ItemStack] 将充当原版套皮物品的实例.
 *
 * 对于第一种, 我们需要生成一个完整的 [ItemStack], 里面包含了 [ItemDataContainer].
 * 对于第二种, 我们只需要生成的 [ItemDataContainer], 其余信息可以丢掉 (GC).
 *
 * 也就是说, 第一种包括了第二种, 因此可以共用一个实现.
 */
object ItemStackGenerator {

    /**
     * 基于上下文 [Context] 从物品类型 [KoishItem] 生成一个新的 [ItemStack] 实例.
     *
     * @param type 物品类型
     * @param context 生成物品的上下文
     * @return 新生成的 [ItemStack]
     */
    fun generate(type: KoishItem, context: Context): ItemStack {
        val dataContainer = ItemDataContainer.builder()
        // FIXME #350: 生成 ItemDataContainer

        // 写入基础信息, 每个自定义物品都有
        dataContainer[ItemDataTypes.ID] = ItemId(type.id)
        dataContainer[ItemDataTypes.VERSION] = 0 // FIXME #350: 实现数据迁移系统

        // 直接操作 MojangStack 以提高生成物品的速度
        val base = type.properties[ItemPropertyTypes.BASE]
        val itemstack = MojangStack(Items.STONE) // FIXME #350: 获取物品的底模

        // 在把 ItemStack 传递到 ItemMetaEntry 之前, 需要先将 ItemDataContainer 写入到 ItemStack.
        // 否则按照目前的实现, 简单的使用 ItemStack.setData 是无法将数据写入到 ItemStack 的,
        // 因为 ItemStack.setData 只有在 ItemDataContainer 存在时才能写入数据
        itemstack.set(DataComponentsPatch.ITEM_DATA_CONTAINER, dataContainer.build())

        // 获取 ItemData 的“配置文件” (ItemMetaContainer)
        val dataConfig = type.dataConfig

        // 从 ItemMetaContainer 生成 ItemData, 并写入到物品堆叠上
        for (metaType in KoishRegistries2.ITEM_META_TYPE) {
            makeItemDataThenWrite(metaType, dataConfig, itemstack, context)
        }

        return itemstack.wrapToBukkit()
    }

    private fun <U, V> makeItemDataThenWrite(
        metaType: ItemMetaType<U, V>,
        metaContainer: ItemMetaContainer,
        itemstack: MojangStack,
        context: Context,
    ) {
        val metaEntry = metaContainer[metaType]
        if (metaEntry != null) {
            val result = metaEntry.make(context)
            if (result.isPresent()) {
                val value = result.unwrap()
                metaEntry.write(value, itemstack)
            }
        }
    }

}
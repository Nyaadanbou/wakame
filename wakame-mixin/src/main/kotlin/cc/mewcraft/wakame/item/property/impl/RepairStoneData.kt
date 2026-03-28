package cc.mewcraft.wakame.item.property.impl

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.serializer.DispatchingSerializer
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

/**
 * 修复石的数据结构.
 *
 * 修复石有两种类型:
 * - [Constant]: 修复固定的耐久度
 * - [Percentage]: 按百分比修复耐久度 (基于最大耐久)
 */
sealed interface RepairStoneData {

    companion object {

        @JvmField
        val SERIALIZER: SimpleSerializer<RepairStoneData> = DispatchingSerializer.createPartial<String, RepairStoneData>(
            mapOf(
                "constant" to Constant::class,
                "percentage" to Percentage::class,
            )
        )
    }

    /**
     * 根据物品的最大耐久度, 计算修复量.
     *
     * @param maxDamage 物品的最大耐久度
     * @return 修复的耐久度数值
     */
    fun computeRepairAmount(maxDamage: Int): Int

    /**
     * 修复固定的耐久度.
     *
     * @param value 修复的耐久度数值
     */
    @ConfigSerializable
    data class Constant(
        @Setting("value")
        val value: Int,
    ) : RepairStoneData {
        override fun computeRepairAmount(maxDamage: Int): Int {
            return value
        }
    }

    /**
     * 按百分比修复耐久度 (基于最大耐久).
     *
     * @param value 修复的百分比, 范围 0.0 ~ 1.0 (例如 0.2 表示修复 20%)
     */
    @ConfigSerializable
    data class Percentage(
        @Setting("value")
        val value: Double,
    ) : RepairStoneData {
        override fun computeRepairAmount(maxDamage: Int): Int {
            return (maxDamage * value).toInt()
        }
    }
}

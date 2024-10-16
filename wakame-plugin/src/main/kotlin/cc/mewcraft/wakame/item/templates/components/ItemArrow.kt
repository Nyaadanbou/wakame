package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.bukkit.entity.AbstractArrow.PickupStatus
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get


// 开发日记: 2024/6/25 小米
// 这是模板类型, 也就是物品组件在配置文件中的封装.
// 实现需要定义模板的数据结构, 以及模板的(反)序列化函数.
// 开发日记: 2024/9/1 芙兰
// 经讨论, 箭矢作为一种弹药, 可堆叠性很重要,
// 故箭矢应该没有任何差异化的 nbt.
// 现在这是第一个只靠模板的组件.
data class ItemArrow(
    /**
     * 可穿透的实体数.
     */
    val pierceLevel: Int,
    /**
     * 箭是否能被捡起.
     */
    val pickupStatus: PickupStatus,
    /**
     * 箭矢本身的着火时间.
     */
    val fireTicks: Int,
    /**
     * 击中实体造成的着火时间.
     */
    val hitFireTicks: Int,
    /**
     * 击中实体造成的冰冻时间. 原版细雪的冰冻效果.
     * 当实体在细雪中时每刻增加`1`, 离开细雪则每刻减少`2`.
     */
    val hitFrozenTicks: Int,
    /**
     * 击中实体造成的发光时间.
     * 仅在箭矢是光灵箭时有效.
     */
    val glowTicks: Int,
) : ItemTemplate<Nothing> {
    override val componentType: ItemComponentType<Nothing> = ItemComponentTypes.EMPTY

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<Nothing> {
        return ItemGenerationResult.empty()
    }

    companion object : ItemTemplateBridge<ItemArrow> {
        override fun codec(id: String): ItemTemplateType<ItemArrow> {
            return TemplateType(id)
        }
    }

    private data class TemplateType(
        override val id: String,
    ) : ItemTemplateType<ItemArrow> {
        override val type: TypeToken<ItemArrow> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   pierce_level: <int>
         *   pick_up_status: <enum>
         *   fire_ticks: <int>
         *   hit_fire_ticks: <int>
         *   hit_frozen_ticks: <int>
         *   glow_ticks: <int>
         * ```
         */
        override fun decode(node: ConfigurationNode): ItemArrow {
            val pierceLevel = node.node("pierce_level").getInt(0)
            require(pierceLevel >= 0) { "Arrow pierce level should not less than 0" }
            val pickupStatus = node.node("pick_up_status").get<PickupStatus>(PickupStatus.ALLOWED)
            val fireTicks = node.node("fire_ticks").getInt(0)
            require(fireTicks >= 0) { "Arrow fire ticks should not less than 0" }
            val hitFireTicks = node.node("hit_fire_ticks").getInt(0)
            require(hitFireTicks >= 0) { "Arrow hit fire ticks should not less than 0" }
            val hitFrozenTicks = node.node("hit_frozen_ticks").getInt(0)
            require(hitFrozenTicks >= 0) { "Arrow hit frozen ticks should not less than 0" }
            val glowTick = node.node("glow_ticks").getInt(0)
            require(glowTick >= 0) { "Arrow glow ticks should not less than 0" }

            return ItemArrow(
                pierceLevel = pierceLevel,
                pickupStatus = pickupStatus,
                fireTicks = fireTicks,
                hitFireTicks = hitFireTicks,
                hitFrozenTicks = hitFrozenTicks,
                glowTicks = glowTick
            )
        }
    }
}
package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.examination.Examinable
import org.bukkit.entity.AbstractArrow.PickupStatus
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get


// 开发日记: 2024/6/25 小米
// 这是文件列表里的第一个物品组件,
// 因此添加了更多代码注释, 请留意.

// 开发日记: 2024/9/1 芙兰
// 经讨论, 箭矢作为一种弹药，可堆叠性很重要
// 故箭矢没有任何差异化的nbt
// 现在这是第一个只靠模板的组件
interface ItemArrow : Examinable {
    companion object : ItemComponentBridge<Unit> {
        override fun codec(id: String): ItemComponentType<Unit> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<Unit> {
        override fun read(holder: ItemComponentHolder): Unit? {
            return if (holder.hasTag()) Unit else null
        }

        override fun write(holder: ItemComponentHolder, value: Unit) {
            holder.editTag()
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }
    }

    /*
    // 开发日记: 2024/6/24 小米
    // companion object 将作为组件配置文件的入口,
    // 这些包括了物品提示框渲染的配置文件, 以及未来可能需要的其他东西
    companion object : ItemComponentBridge<ItemArrow>, ItemComponentMeta {

        override fun codec(id: String): ItemComponentType<ItemArrow> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }

        override val configPath: String = ItemConstants.ARROW
        override val tooltipKey: Key = ItemConstants.createKey { ARROW }

        private val config: ItemComponentConfig = ItemComponentConfig.provide(this)
        private val tooltip: ItemComponentConfig.SingleTooltip = config.SingleTooltip()
    }

    // 开发日记: 2024/6/25
    // 这是物品组件的快照类型.
    // 每次从物品上读取一个物品组件信息,
    // 都会读取到一个物品组件的不可变快照.
    // 调用者需要通过快照来详细的读取物品组件上储存的信息.
    // 需要注意, 该类型还需要实现 TooltipsProvider 接口,
    // 否则其他系统将无法得知如何将该物品组件显示在物品提示框里.
    override fun provideTooltipLore(): LoreLine {
        if (!config.showInTooltip) {
            return LoreLine.noop()
        }
        return LoreLine.simple(tooltipKey, listOf(tooltip.render(Placeholder.component("pierce_level", Component.text(pierceLevel.toInt())))))
    }

    // 开发日记: 2024/6/25
    // 这是编码器, 定义了如何在游戏中读取/写入/移除物品上的组件信息.
    // 根据物品组件的具体情况, 这里的实现会稍有不同.
    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemArrow> {
        override fun read(holder: ItemComponentHolder): ItemArrow? {
            val tag = holder.getTag() ?: return null
            val pierceLevel = tag.getByte(TAG_PIERCE_LEVEL)
            return ItemArrow(pierceLevel = pierceLevel)
        }

        override fun write(holder: ItemComponentHolder, value: ItemArrow) {
            holder.editTag { tag ->
                val pierceLevel = value.pierceLevel
                tag.putByte(TAG_PIERCE_LEVEL, pierceLevel)
            }
        }

        // 开发日记 2024/6/29
        // 由于 ItemComponentHolder 已重写,
        // 对于原来 holder 为 NBT 的物品组件,
        // 也必须要正确实现该函数.
        // 具体来说就是调用一下 holder.removeTag()
        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }

        // 开发日记: 2024/6/24 小米
        // Codec 的 companion object 一般就写 NBT 标签的 key 就行.
        // 如果有其他的常量也可以写在这里, 具体看情况.
        private companion object {
            const val TAG_PIERCE_LEVEL = "pierce_level"
        }
    }
     */

    // 开发日记: 2024/6/25
    // 这是模板类型, 也就是物品组件在配置文件中的封装.
    // 实现需要定义模板的数据结构, 以及模板的(反)序列化函数.
    data class Template(
        /**
         * 可穿透的实体数.
         */
        val pierceLevel: Int,
        /**
         * 箭是否能被捡起.
         * 0表示不可以被玩家捡起.
         * 1表示可以被玩家在生存或创造模式中捡起.
         * 2表示仅可以被创造模式的玩家捡起.
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
         * 击中实体造成的冰冻时间.
         * 原版细雪的冰冻效果.
         * (当实体在细雪中时每刻增加1, 离开细雪则每刻减少2)
         */
        val hitFrozenTicks: Int,
        /**
         * 击中实体造成的发光时间.
         * 仅在箭矢是光灵箭时有效.
         */
        val glowTicks: Int,
    ) : ItemTemplate<Unit> {
        override val componentType: ItemComponentType<Unit> = ItemComponentTypes.ARROW

        override fun generate(context: GenerationContext): GenerationResult<Unit> {
            return GenerationResult.of(Unit)
        }
    }

    private data class TemplateType(
        override val id: String,
    ) : ItemTemplateType<Template> {
        override val type: TypeToken<Template> = typeTokenOf()

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
        override fun decode(node: ConfigurationNode): Template {
            val pierceLevel = node.node("pierce_level").getInt(0).apply {
                require(this >= 0) { "Arrow pierce level should not less than 0" }
            }
            val pickupStatus = node.node("pick_up_status").get<PickupStatus>(PickupStatus.ALLOWED)
            val fireTicks = node.node("fire_ticks").getInt(0).apply {
                require(this >= 0) { "Arrow fire ticks should not less than 0" }
            }
            val hitFireTicks = node.node("hit_fire_ticks").getInt(0).apply {
                require(this >= 0) { "Arrow hit fire ticks should not less than 0" }
            }
            val hitFrozenTicks = node.node("hit_frozen_ticks").getInt(0).apply {
                require(this >= 0) { "Arrow hit frozen ticks should not less than 0" }
            }
            val glowTick = node.node("glow_ticks").getInt(0).apply {
                require(this >= 0) { "Arrow glow ticks should not less than 0" }
            }
            return Template(
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
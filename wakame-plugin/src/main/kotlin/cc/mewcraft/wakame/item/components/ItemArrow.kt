package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentMeta
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toStableByte
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode

// 开发日记: 2024/6/25 小米
// 这是文件列表里的第一个物品组件,
// 因此添加了更多代码注释, 请留意.
data class ItemArrow(
    /**
     * 可穿透的实体数.
     */
    val pierceLevel: Byte,
) : Examinable, TooltipProvider.Single {

    // 开发日记: 2024/6/24 小米
    // companion object 将作为组件配置文件的入口,
    // 这些包括了物品提示框渲染的配置文件, 以及未来可能需要的其他东西
    companion object : ItemComponentBridge<ItemArrow>, ItemComponentMeta {
        fun of(pierceLevel: Int): ItemArrow {
            return ItemArrow(pierceLevel.toStableByte())
        }

        override fun codec(id: String): ItemComponentType<ItemArrow> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }

        override val configPath: String = ItemComponentConstants.ARROW
        override val tooltipKey: Key = ItemComponentConstants.createKey { ARROW }

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

    // 开发日记: 2024/6/25
    // 这是模板类型, 也就是物品组件在配置文件中的封装.
    // 实现需要定义模板的数据结构, 以及模板的(反)序列化函数.
    data class Template(
        val pierceLevel: RandomizedValue,
    ) : ItemTemplate<ItemArrow> {
        override val componentType: ItemComponentType<ItemArrow> = ItemComponentTypes.ARROW

        override fun generate(context: GenerationContext): GenerationResult<ItemArrow> {
            val pierceLevel = pierceLevel.calculate().toStableByte()
            return GenerationResult.of(ItemArrow(pierceLevel))
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
         *   pierce_level: <randomized_value>
         * ```
         */
        override fun decode(node: ConfigurationNode): Template {
            val pierceLevel = node.node("pierce_level").krequire<RandomizedValue>()
            return Template(pierceLevel)
        }
    }
}
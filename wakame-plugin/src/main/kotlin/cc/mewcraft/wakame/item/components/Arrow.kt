package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipsProvider
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.GenerationContext
import cc.mewcraft.wakame.item.component.GenerationResult
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentTemplate
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toStableByte
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

// 开发日记: 2024/6/25 小米
// 这是文件列表里的第一个 物品组件,
// 因此添加了更多注释解释代码.
// 请留意.

interface Arrow : Examinable, TooltipsProvider {

    /**
     * 可穿透的实体数.
     */
    val pierceLevel: Int


    // 开发日记: 2024/6/25
    // 这是物品组件的快照类型.
    // 每次从物品上读取一个物品组件信息,
    // 都会读取到一个物品组件的不可变快照.
    // 调用者需要通过快照来详细的读取物品组件上储存的信息.
    // 需要注意, 该类型还需要实现 TooltipsProvider 接口,
    // 否则其他系统将无法得知如何将该物品组件显示在物品提示框里.
    data class Value(
        override val pierceLevel: Int,
    ) : Arrow {
        override fun provideDisplayLore(): LoreLine {
            if (!showInTooltip) {
                return LoreLine.noop()
            }
            return LoreLine.simple(key, listOf(tooltips.render(Placeholder.component("pierce_level", Component.text(pierceLevel)))))
        }

        // 开发日记: 2024/6/24 小米
        // companion object 将作为组件配置文件的入口,
        // 这些包括了物品提示框渲染的配置文件, 以及未来可能需要的其他东西
        companion object : ItemComponentConfig(ItemComponentConstants.ARROW) {
            val key: Key = ItemComponentConstants.createKey { ARROW }
            val tooltips: SingleTooltip = SingleTooltip()
        }
    }

    // 开发日记: 2024/6/25
    // 这是编码器, 定义了如何在游戏中读取/写入/移除物品上的组件信息.
    // 根据物品组件的具体情况, 这里的实现会稍有不同.
    data class Codec(
        override val id: String,
    ) : ItemComponentType.Valued<Arrow, ItemComponentHolder.NBT> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.NBT

        override fun read(holder: ItemComponentHolder.NBT): Arrow? {
            val pierceLevel = holder.tag.getByte(PIERCE_LEVEL)
            return Value(pierceLevel.toInt())
        }

        override fun write(holder: ItemComponentHolder.NBT, value: Arrow) {
            val pierceLevel = value.pierceLevel
            holder.tag.putByte(PIERCE_LEVEL, pierceLevel.toStableByte())
        }

        override fun remove(holder: ItemComponentHolder.NBT) {
            // no-op
        }

        // 开发日记: 2024/6/24 小米
        // Codec 的 companion object 一般就写 NBT 标签的 key 就行.
        // 如果有其他的常量也可以写在这里, 具体看情况.
        companion object {
            const val PIERCE_LEVEL = "pierce_level"
        }
    }

    // 开发日记: 2024/6/25
    // 这是模板类型, 也就是物品组件在配置文件中的封装.
    // 实现需要定义模板的数据结构, 以及模板的(反)序列化函数.
    data class Template(
        val pierceLevel: RandomizedValue,
    ) : ItemComponentTemplate<Arrow> {
        override fun generate(context: GenerationContext): GenerationResult<Arrow> {
            val pierceLevel = pierceLevel.calculate().toInt()
            return GenerationResult.of(Value(pierceLevel))
        }

        companion object : ItemComponentTemplate.Serializer<Template> {
            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                val pierceLevel = node.node("pierce_level").krequire<RandomizedValue>()
                return Template(pierceLevel)
            }
        }
    }
}
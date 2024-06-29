package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

// 开发日记 2024/6/28
// CustomName 有几个需要思考的问题:
// 1. 在物品上存什么;
// 2. 从物品上获取时返回什么;
// 3. 在后台模板上存什么;
//
// 第一个问题, 存什么是要看具体需求的, 也就是分析到底要不要.
// wakame 的物品组件都是“自己的物品组件”,
// 这句的话意思是, 组件在*概念上*都是 wakame 自己的,
// 但在组件的实现方式上是多种多样的 —— 可以基于 NBT/Item/Complex.
//
// 第二个问题, 从物品上获取时返回什么.
// 这个也是看具体需求 —— 我们需要最原始的数据, 还是处理过后的数据?
//
// 第三个问题, 从后台模板上存什么.
// 这个只有一个要求 —— 模板上储存的数据可以生成所有需要的物品数据.

// TODO 完成组件: CustomName

interface CustomName : Examinable {

    val raw: String
    val cooked: Component

    // TODO 2024/6/28 CustomName 需要更合适的 ItemComponentHolder
    //  因为渲染一个 CustomName 需要物品的 Rarity 组件, 而目前的
    //  Holder 并不能很方便的直接获取其他的组件信息.

    data class Value(
        override val raw: String,
        override val cooked: Component,
    ) : CustomName

    data class Codec(
        override val id: String,
    ) : ItemComponentType<CustomName> {
        override fun read(holder: ItemComponentHolder): CustomName? {
            val tag = holder.getTag() ?: return null
            val raw = tag.getString(TAG_VALUE)
            val cooked = Component.empty()
            return Value(raw = raw, cooked = cooked)
        }

        override fun write(holder: ItemComponentHolder, value: CustomName) {
            val tag = holder.getTagOrCreate()
            tag.putString(TAG_VALUE, value.raw)
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }

        private companion object {
            const val TAG_VALUE = "raw"
        }
    }

    data class Template(
        /**
         * A MiniMessage string.
         */
        val customName: String?,
    ) : ItemTemplate<CustomName> {
        override fun generate(context: GenerationContext): GenerationResult<CustomName> {
            if (customName == null) {
                return GenerationResult.empty()
            }
            val raw = customName
            val cooked = Component.empty()
            return GenerationResult.of(Value(raw = raw, cooked = cooked))
        }

        companion object : ItemTemplateType<Template> {
            override val typeToken: TypeToken<Template> = typeTokenOf()

            /**
             * ## Node structure
             * ```yaml
             * <node>: <string>
             * ```
             */
            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                return Template(node.string)
            }
        }
    }
}

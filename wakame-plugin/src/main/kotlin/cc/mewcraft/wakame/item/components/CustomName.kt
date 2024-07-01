package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentInjections
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
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
// 回答第一个: 存 raw string 和 rich string
// raw 是配置文件里的原始 MiniMessage 字符串,
// rich 是根据 raw 生成出来的 text component.
//
// 第二个问题, 从物品上获取时返回什么.
// 这个也是看具体需求 —— 我们需要最原始的数据, 还是处理过后的数据?
// 回答第二个: 有什么就返回什么.
//
// 第三个问题, 从后台模板上存什么.
// 这个只有一个要求 —— 模板上储存的数据可以生成所有需要的物品数据.
// 回答第三个: 存原始的 raw 即可.

interface CustomName : Examinable {

    /**
     * 原始字符串, 格式为 MiniMessage.
     *
     * 这部分是直接存在 NBT 里的原始字符串.
     */
    val raw: String

    /**
     * 原始字符串 [raw] 经解析之后的产生的 [Component].
     *
     * 这部分是直接存在原版物品组件 `minecraft:custom_name` 上的.
     * 因此, 如果玩家有修改过物品的名字, 那么该文本可能与 [raw]
     * 一开始生成出来的富文本有所差别.
     */
    val rich: Component

    data class Value(
        override val raw: String,
        override val rich: Component,
    ) : CustomName {
        constructor(rich: Component) : this("", rich)
    }

    data class Codec(
        override val id: String,
    ) : ItemComponentType<CustomName> {
        override fun read(holder: ItemComponentHolder): CustomName? {
            val tag = holder.getTag() ?: return null

            // 2024/6/29
            // 设计上 custom_name 和 item_name 都不经过发包系统处理,
            // 因此这里有什么就读取什么. 整体上做到简单, 一致, 无例外.

            // 获取 raw string
            val raw = tag.getString(TAG_VALUE)
            // 获取 rich string
            val rich = holder.item.itemMeta?.displayName() ?: Component.empty()

            return Value(raw = raw, rich = rich)
        }

        override fun write(holder: ItemComponentHolder, value: CustomName) {
            // 2024/6/29
            // 设计上 custom_name 和 item_name 都不经过发包系统处理,
            // 因此这里有什么就写入什么. 整体上做到简单, 一致, 无例外.

            // 将 raw 写入到 NBT
            val tag = holder.getTagOrCreate()
            if (value.raw.isNotBlank()) {
                // 只有当 raw 不为空字符串时才更新 NBT
                tag.putString(TAG_VALUE, value.raw)
            }

            // 将 rich 写入到原版物品组件 `minecraft:custom_name`
            val item = holder.item
            item.editMeta {
                it.displayName(value.rich)
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
            holder.item.editMeta { it.displayName(null) }
        }

        private companion object : ItemComponentConfig(ItemComponentConstants.CUSTOM_NAME) {
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

            // 开发日记 2024/6/29
            // 根据设计, custom_name 和 item_name 都不经过发包系统处理.
            // 因此, 生成 custom_name 的时候就需要把 CustomName.rich
            // 根据上下文也生成好, 不能写 Component.empty.

            val resolver = TagResolver.builder()
            val rarity = context.rarity
            if (rarity != null) {
                resolver.tag("rarity") { queue: ArgumentQueue, ctx: Context ->
                    val arg = queue.popOr("Tag <rarity:_> must have an argument. Available arguments: 'name', 'style'").lowerValue()
                    when (arg) {
                        "name" -> {
                            Tag.selfClosingInserting(rarity.displayName)
                        }

                        "style" -> {
                            Tag.styling(*rarity.styles)
                        }

                        else -> throw ctx.newException("Unknown argument. Available arguments: 'name', 'style'", queue)
                    }
                }
            }
            val raw = customName
            val rich = ItemComponentInjections.mini.deserialize(customName, resolver.build())

            return GenerationResult.of(Value(raw = raw, rich = rich))
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

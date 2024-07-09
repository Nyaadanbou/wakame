package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentInjections
import cc.mewcraft.wakame.item.component.ItemComponentMeta
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.examination.Examinable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

data class ItemName(
    /**
     * 原始字符串, 格式为 MiniMessage.
     *
     * 这部分是直接存在 NBT 里的原始字符串.
     */
    val raw: String,

    /**
     * 原始字符串 [raw] 经解析之后的产生的 [Component].
     *
     * 这部分是直接存在原版物品组件 `minecraft:item_name` 上的.
     * 因此, 如果之后有修改过该组件的值, 那么该文本可能与 [raw]
     * 一开始生成出来的富文本有所差别.
     */
    val rich: Component,
) : Examinable {

    /**
     * 用于直接设置 `minecraft:item_name`.
     */
    constructor(
        rich: Component,
    ) : this(
        miniMessage.serialize(rich), rich
    )

    companion object : ItemComponentBridge<ItemName>, ItemComponentMeta, KoinComponent {
        override fun codec(id: String): ItemComponentType<ItemName> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }

        override val configPath: String = ItemComponentConstants.ITEM_NAME
        override val tooltipKey: Key = ItemComponentConstants.createKey { ITEM_NAME }

        private val miniMessage: MiniMessage by inject()
        private val config: ItemComponentConfig = ItemComponentConfig.provide(this)
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemName> {
        override fun read(holder: ItemComponentHolder): ItemName? {
            val tag = holder.getTag() ?: return null

            // 2024/6/29
            // 设计上 custom_name 和 item_name 都不经过发包系统处理,
            // 因此这里有什么就读取什么. 整体上做到简单, 一致, 无例外.

            // 获取 raw string
            val raw = tag.getString(TAG_VALUE)
            // 获取 rich string
            val rich = holder.item.itemMeta?.itemName() ?: Component.empty()

            return ItemName(raw = raw, rich = rich)
        }

        override fun write(holder: ItemComponentHolder, value: ItemName) {
            // 2024/6/29
            // 设计上 custom_name 和 item_name 都不经过发包系统处理,
            // 因此这里有什么就写入什么. 整体上做到简单, 一致, 无例外.

            // 将 raw 写入到 NBT
            val tag = holder.getTagOrCreate()
            if (value.raw.isNotBlank()) {
                // 只有当 raw 不为空字符串时才更新 NBT
                tag.putString(TAG_VALUE, value.raw)
            }

            // 将 rich 写入到原版物品组件 `minecraft:item_name`
            val item = holder.item
            item.editMeta {
                it.itemName(value.rich)
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
            holder.item.editMeta { it.itemName(null) }
        }

        private companion object {
            const val TAG_VALUE = "raw"
        }
    }

    data class Template(
        /**
         * A MiniMessage string.
         */
        val itemName: String?,
    ) : ItemTemplate<ItemName> {
        override val componentType: ItemComponentType<ItemName> = ItemComponentTypes.ITEM_NAME

        override fun generate(context: GenerationContext): GenerationResult<ItemName> {
            if (itemName == null) {
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
            val raw = itemName
            val rich = ItemComponentInjections.mini.deserialize(itemName, resolver.build())

            return GenerationResult.of(ItemName(raw = raw, rich = rich))
        }
    }

    private data class TemplateType(
        override val id: String
    ) : ItemTemplateType<Template> {
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
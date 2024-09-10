package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.spongepowered.configurate.ConfigurationNode
import cc.mewcraft.wakame.item.components.CustomName as CustomNameData


data class CustomName(
    val customName: String?,
) : ItemTemplate<CustomNameData> {
    override val componentType: ItemComponentType<CustomNameData> = ItemComponentTypes.CUSTOM_NAME

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<CustomNameData> {
        if (customName == null) {
            return ItemGenerationResult.empty()
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
        val rich = ItemComponentInjections.miniMessage.deserialize(customName, resolver.build())

        return ItemGenerationResult.of(CustomNameData(raw = raw, rich = rich))
    }

    companion object : ItemTemplateBridge<CustomName> {
        override fun codec(id: String): ItemTemplateType<CustomName> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<CustomName> {
        override val type: TypeToken<CustomName> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>: <string>
         * ```
         */
        override fun decode(node: ConfigurationNode): CustomName {
            return CustomName(node.string)
        }
    }
}
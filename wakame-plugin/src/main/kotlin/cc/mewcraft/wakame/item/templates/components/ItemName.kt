package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemTemplateBridge
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.ConfigurationNode


data class ItemName(
    override val plainName: String, // 纯文本
    override val fancyName: String, // MM格式
) : NameLike() {
    override val componentType: ItemComponentType<Component> = ItemComponentTypes.ITEM_NAME

    companion object : ItemTemplateBridge<ItemName> {
        override fun codec(id: String): ItemTemplateType<ItemName> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemName> {
        override val type: TypeToken<ItemName> = typeTokenOf()

        /**
         * ## Node structure 1
         * ```yaml
         * <node>: <string>
         * ```
         *
         * ## Node structure 2
         * ```yaml
         * <node>:
         *   plain: <string>
         *   fancy: <string>
         * ```
         */
        override fun decode(node: ConfigurationNode): ItemName {
            val scalar = node.string
            if (scalar != null) {
                // assume it's a scalar
                return ItemName(plainName = scalar, fancyName = scalar)
            } else {
                // some other format
                val plain = node.node("plain").krequire<String>()
                val fancy = node.node("fancy").string ?: plain
                return ItemName(plainName = plain, fancyName = fancy)
            }
        }
    }
}
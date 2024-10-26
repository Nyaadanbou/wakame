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


data class CustomName(
    override val plainName: String,
    override val fancyName: String,
) : NameLike() {
    override val componentType: ItemComponentType<Component> = ItemComponentTypes.CUSTOM_NAME

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
        override fun decode(node: ConfigurationNode): CustomName {
            val scalar = node.string
            if (scalar != null) {
                // assume it's a scalar
                return CustomName(plainName = scalar, fancyName = scalar)
            } else {
                // some other format
                val plain = node.node("plain").krequire<String>()
                val fancy = node.node("fancy").string ?: plain
                return CustomName(plainName = plain, fancyName = fancy)
            }
        }
    }
}
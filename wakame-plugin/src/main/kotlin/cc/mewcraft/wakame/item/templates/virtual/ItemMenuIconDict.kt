package cc.mewcraft.wakame.item.templates.virtual

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.template.ItemGenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateBridge
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.MenuIconDictionary
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode

data class ItemMenuIconDict(
    val delegate: MenuIconDictionary,
) : ItemTemplate<Nothing> {
    override val componentType: ItemComponentType<Nothing> = ItemComponentTypes.EMPTY

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<Nothing> {
        return ItemGenerationResult.empty()
    }

    companion object : ItemTemplateBridge<ItemMenuIconDict> {
        override fun codec(id: String): ItemTemplateType<ItemMenuIconDict> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemMenuIconDict> {
        override val type: TypeToken<ItemMenuIconDict> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   key1: "value1"
         *   key2: "value2"
         * ```
         */
        override fun decode(node: ConfigurationNode): ItemMenuIconDict {
            return ItemMenuIconDict(node.krequire<MenuIconDictionary>())
        }
    }
}

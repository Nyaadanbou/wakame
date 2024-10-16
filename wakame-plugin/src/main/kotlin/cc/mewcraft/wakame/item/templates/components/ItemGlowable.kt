package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode


data object ItemGlowable : ItemTemplate<Nothing>, ItemTemplateBridge<ItemGlowable> {
    override val componentType: ItemComponentType<Nothing> = ItemComponentTypes.EMPTY

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<Nothing> {
        return ItemGenerationResult.empty()
    }

    override fun codec(id: String): ItemTemplateType<ItemGlowable> {
        return Codec(id)
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemGlowable> {
        override val type: TypeToken<ItemGlowable> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>: {}
         * ```
         */
        override fun decode(node: ConfigurationNode): ItemGlowable {
            return ItemGlowable
        }
    }
}
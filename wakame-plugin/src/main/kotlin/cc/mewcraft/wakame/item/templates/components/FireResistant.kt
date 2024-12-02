package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.template.ItemGenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateBridge
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode


data object FireResistant : ItemTemplate<Boolean>, ItemTemplateBridge<FireResistant> {
    override val componentType: ItemComponentType<Boolean> = ItemComponentTypes.FIRE_RESISTANT

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<Boolean> {
        return ItemGenerationResult.of(true)
    }

    override fun codec(id: String): ItemTemplateType<FireResistant> {
        return Codec(id)
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<FireResistant> {
        override val type: TypeToken<FireResistant> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>: {}
         * ```
         */
        override fun decode(node: ConfigurationNode): FireResistant {
            return FireResistant
        }
    }
}
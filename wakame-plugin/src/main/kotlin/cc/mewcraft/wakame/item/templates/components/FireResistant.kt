package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import cc.mewcraft.wakame.item.components.FireResistant as FireResistantData


data object FireResistant : ItemTemplate<FireResistantData>, ItemTemplateBridge<FireResistant> {
    override val componentType: ItemComponentType<FireResistantData> = ItemComponentTypes.FIRE_RESISTANT

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<FireResistantData> {
        return ItemGenerationResult.of(FireResistantData.instance())
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
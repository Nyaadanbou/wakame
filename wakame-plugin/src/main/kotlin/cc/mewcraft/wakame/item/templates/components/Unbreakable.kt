package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ShownInTooltip
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import cc.mewcraft.wakame.item.components.Unbreakable as UnbreakableData

data class Unbreakable(
    override val showInTooltip: Boolean,
) : ItemTemplate<UnbreakableData>, ShownInTooltip {
    override val componentType: ItemComponentType<UnbreakableData> = ItemComponentTypes.UNBREAKABLE

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<UnbreakableData> {
        return ItemGenerationResult.of(UnbreakableData.of(showInTooltip))
    }

    companion object : ItemTemplateBridge<Unbreakable> {
        override fun codec(id: String): ItemTemplateType<Unbreakable> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<Unbreakable> {
        override val type: TypeToken<Unbreakable> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   show_in_tooltip: <boolean>
         * ```
         */
        override fun decode(node: ConfigurationNode): Unbreakable {
            val showInTooltip = node.node("show_in_tooltip").getBoolean(true)
            return Unbreakable(showInTooltip)
        }
    }
}

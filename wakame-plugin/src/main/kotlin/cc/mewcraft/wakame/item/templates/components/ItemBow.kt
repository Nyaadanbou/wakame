package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode

data object ItemBow : ItemTemplate<Nothing>, ItemTemplateBridge<ItemBow> {
    override val componentType: ItemComponentType<Nothing> = ItemComponentTypes.EMPTY

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<Nothing> {
        return ItemGenerationResult.empty()
    }

    override fun codec(id: String): ItemTemplateType<ItemBow> {
        return Codec(id)
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemBow> {
        override val type: TypeToken<ItemBow> = typeTokenOf()

        override fun decode(node: ConfigurationNode): ItemBow {
            return ItemBow
        }
    }
}
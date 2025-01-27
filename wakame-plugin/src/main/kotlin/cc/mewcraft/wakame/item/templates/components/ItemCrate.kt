package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import cc.mewcraft.wakame.item.components.ItemCrate as ItemCrateData

data class ItemCrate(
    /**
     * 盲盒的唯一标识.
     */
    val identity: String,
) : ItemTemplate<ItemCrateData> {
    override val componentType: ItemComponentType<ItemCrateData> = ItemComponentTypes.CRATE

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<ItemCrateData> {
        return ItemGenerationResult.of(ItemCrateData(identity = identity))
    }

    companion object : ItemTemplateBridge<ItemCrate> {
        override fun codec(id: String): ItemTemplateType<ItemCrate> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemCrate> {
        override val type: TypeToken<ItemCrate> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   key: "foo:bar"
         * ```
         */
        override fun decode(node: ConfigurationNode): ItemCrate {
            val raw = node.node("id").require<String>()
            return ItemCrate(raw)
        }
    }
}

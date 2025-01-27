package cc.mewcraft.wakame.item.templates.virtual

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.SlotDisplayDictData
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode

data class ItemSlotDisplayDict(
    val delegate: SlotDisplayDictData,
) : ItemTemplate<Nothing> {
    override val componentType: ItemComponentType<Nothing> = ItemComponentTypes.EMPTY

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<Nothing> {
        return ItemGenerationResult.empty()
    }

    /**
     * @see SlotDisplayDictData.get
     */
    operator fun get(key: String): String {
        return delegate[key] ?: error("dict key not found: $key")
    }

    companion object : ItemTemplateBridge<ItemSlotDisplayDict> {
        override fun codec(id: String): ItemTemplateType<ItemSlotDisplayDict> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemSlotDisplayDict> {
        override val type: TypeToken<ItemSlotDisplayDict> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   key1: "value1"
         *   key2: "value2"
         * ```
         */
        override fun decode(node: ConfigurationNode): ItemSlotDisplayDict {
            return ItemSlotDisplayDict(node.require<SlotDisplayDictData>())
        }
    }
}

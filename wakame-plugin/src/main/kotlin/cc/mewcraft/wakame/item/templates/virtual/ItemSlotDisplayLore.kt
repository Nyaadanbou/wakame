package cc.mewcraft.wakame.item.templates.virtual

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.*
import cc.mewcraft.wakame.util.SlotDisplayLoreData.LineConfig
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection

data class ItemSlotDisplayLore(
    val delegate: SlotDisplayLoreData,
) : ItemTemplate<Nothing> {
    override val componentType: ItemComponentType<Nothing> = ItemComponentTypes.EMPTY

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<Nothing> {
        return ItemGenerationResult.empty()
    }

    /**
     * @see SlotDisplayLoreData.resolve
     */
    fun resolve(config: LineConfig): List<Component> {
        return delegate.resolve(config)
    }

    /**
     * @see SlotDisplayLoreData.resolve
     */
    fun resolve(dict: SlotDisplayDictData = SlotDisplayDictData(), dsl: LineConfig.Builder.() -> Unit): List<Component> {
        return delegate.resolve(dict, dsl)
    }

    companion object : ItemTemplateBridge<ItemSlotDisplayLore> {
        override fun codec(id: String): ItemTemplateType<ItemSlotDisplayLore> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemSlotDisplayLore> {
        override val type: TypeToken<ItemSlotDisplayLore> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   - "line 1 (MiniMessage string)"
         *   - "line 2 (MiniMessage string)"
         * ```
         *
         * @see cc.mewcraft.wakame.util.SlotDisplayLoreData
         * @see cc.mewcraft.wakame.util.SlotDisplayLoreDataSerializer
         */
        override fun decode(node: ConfigurationNode): ItemSlotDisplayLore {
            return ItemSlotDisplayLore(node.require<SlotDisplayLoreData>())
        }

        override fun childrenCodecs(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()
                .register<SlotDisplayLoreData>(SlotDisplayLoreDataSerializer)
                .build()
        }
    }
}

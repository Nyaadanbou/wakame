package cc.mewcraft.wakame.item.templates.virtual

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.*
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.spongepowered.configurate.ConfigurationNode

data class ItemSlotDisplayName(
    val delegate: SlotDisplayNameData,
) : ItemTemplate<Nothing> {
    override val componentType: ItemComponentType<Nothing> = ItemComponentTypes.EMPTY

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<Nothing> {
        return ItemGenerationResult.empty()
    }

    /**
     * @see SlotDisplayNameData.resolve
     */
    fun resolve(tagResolver: TagResolver): Component {
        return delegate.resolve(tagResolver)
    }

    /**
     * @see SlotDisplayNameData.resolve
     */
    fun resolve(dict: SlotDisplayDictData = SlotDisplayDictData(), dsl: SlotDisplayData.PlaceholderBuilder.() -> Unit): Component {
        return delegate.resolve(dict, dsl)
    }

    companion object : ItemTemplateBridge<ItemSlotDisplayName> {
        override fun codec(id: String): ItemTemplateType<ItemSlotDisplayName> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemSlotDisplayName> {
        override val type: TypeToken<ItemSlotDisplayName> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>: "item name (MiniMessage string)"
         * ```
         */
        override fun decode(node: ConfigurationNode): ItemSlotDisplayName {
            return ItemSlotDisplayName(node.require<SlotDisplayNameData>())
        }
    }
}

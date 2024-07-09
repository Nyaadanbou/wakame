package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentMeta
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.typeTokenOf
import com.google.common.collect.ImmutableMultimap
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.bukkit.inventory.ItemFlag
import org.spongepowered.configurate.ConfigurationNode

data class ItemAttributeModifiers(
    val showInTooltip: Boolean,
) : Examinable {

    companion object : ItemComponentBridge<ItemAttributeModifiers>, ItemComponentMeta {
        override fun codec(id: String): ItemComponentType<ItemAttributeModifiers> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }

        override val configPath: String = ItemComponentConstants.ATTRIBUTE_MODIFIERS
        override val tooltipKey: Key = ItemComponentConstants.createKey { ATTRIBUTE_MODIFIERS }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemAttributeModifiers> {
        override fun read(holder: ItemComponentHolder): ItemAttributeModifiers? {
            val im = holder.item.itemMeta ?: return null
            if (im.hasAttributeModifiers()) {
                val showInTooltip = !im.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                return ItemAttributeModifiers(showInTooltip)
            }
            return null
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta {
                it.attributeModifiers = null
            }
        }

        override fun write(holder: ItemComponentHolder, value: ItemAttributeModifiers) {
            holder.item.editMeta {
                it.attributeModifiers = ImmutableMultimap.of()
                if (value.showInTooltip) {
                    it.removeItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                } else {
                    it.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                }
            }
        }
    }

    data class Template(
        val showInTooltip: Boolean,
    ) : ItemTemplate<ItemAttributeModifiers> {
        override val componentType: ItemComponentType<ItemAttributeModifiers> = ItemComponentTypes.ATTRIBUTE_MODIFIERS

        override fun generate(context: GenerationContext): GenerationResult<ItemAttributeModifiers> {
            return GenerationResult.of(ItemAttributeModifiers(showInTooltip))
        }
    }

    private data class TemplateType(
        override val id: String,
    ) : ItemTemplateType<Template> {
        override val type: TypeToken<Template> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   show_in_tooltip: <boolean>
         * ```
         */
        override fun decode(node: ConfigurationNode): Template {
            val showInTooltip = node.node("show_in_tooltip").getBoolean(true)
            return Template(showInTooltip)
        }
    }
}
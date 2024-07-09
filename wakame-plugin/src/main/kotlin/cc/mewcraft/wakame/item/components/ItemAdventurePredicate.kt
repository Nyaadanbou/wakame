package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.examination.Examinable
import org.bukkit.inventory.ItemFlag
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

data class ItemAdventurePredicate(
    val showInTooltip: Boolean,
) : Examinable {

    companion object : ItemComponentBridge<ItemAdventurePredicate> {
        override fun codec(id: String): ItemComponentType<ItemAdventurePredicate> {
            return when (id) {
                ItemComponentConstants.CAN_BREAK -> CodecForCanBreak(id)
                ItemComponentConstants.CAN_PLACE_ON -> CodecForCanPlaceOn(id)
                else -> throw IllegalArgumentException("Unknown codec id: '$id'")
            }
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }
    }

    private data class CodecForCanBreak(
        override val id: String,
    ) : ItemComponentType<ItemAdventurePredicate> {
        override fun read(holder: ItemComponentHolder): ItemAdventurePredicate? {
            val im = holder.item.itemMeta ?: return null
            val showInTooltip = !im.hasItemFlag(ItemFlag.HIDE_DESTROYS)
            return ItemAdventurePredicate(showInTooltip)
        }

        override fun write(holder: ItemComponentHolder, value: ItemAdventurePredicate) {
            holder.item.editMeta { im ->
                if (value.showInTooltip) {
                    im.removeItemFlags(ItemFlag.HIDE_DESTROYS)
                } else {
                    im.addItemFlags(ItemFlag.HIDE_DESTROYS)
                }
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta { im ->
                im.removeItemFlags(ItemFlag.HIDE_DESTROYS)
            }
        }
    }

    private data class CodecForCanPlaceOn(
        override val id: String,
    ) : ItemComponentType<ItemAdventurePredicate> {
        override fun read(holder: ItemComponentHolder): ItemAdventurePredicate? {
            val im = holder.item.itemMeta ?: return null
            val showInTooltip = !im.hasItemFlag(ItemFlag.HIDE_PLACED_ON)
            return ItemAdventurePredicate(showInTooltip)
        }

        override fun write(holder: ItemComponentHolder, value: ItemAdventurePredicate) {
            holder.item.editMeta { im ->
                if (value.showInTooltip) {
                    im.removeItemFlags(ItemFlag.HIDE_PLACED_ON)
                } else {
                    im.addItemFlags(ItemFlag.HIDE_PLACED_ON)
                }
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta { im ->
                im.removeItemFlags(ItemFlag.HIDE_PLACED_ON)
            }
        }
    }

    data class Template(
        override val componentType: ItemComponentType<ItemAdventurePredicate>,
        val showInTooltip: Boolean,
    ) : ItemTemplate<ItemAdventurePredicate> {
        override fun generate(context: GenerationContext): GenerationResult<ItemAdventurePredicate> {
            return GenerationResult.of(ItemAdventurePredicate(showInTooltip))
        }
    }

    private data class TemplateType(
        override val id: String,
    ) : ItemTemplateType<Template> {
        override val typeToken: TypeToken<Template> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   show_in_tooltip: <boolean>
         * ```
         */
        override fun deserialize(type: Type, node: ConfigurationNode): Template {
            val showInTooltip = node.node("show_in_tooltip").getBoolean(true)
            return when (id) {
                ItemComponentConstants.CAN_BREAK -> Template(ItemComponentTypes.CAN_BREAK,showInTooltip)
                ItemComponentConstants.CAN_PLACE_ON -> Template(ItemComponentTypes.CAN_PLACE_ON,showInTooltip)
                else -> throw IllegalArgumentException("Unknown template id: '$id'")
            }
        }
    }

}
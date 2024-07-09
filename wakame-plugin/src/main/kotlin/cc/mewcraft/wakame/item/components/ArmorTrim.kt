package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.editMeta
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.examination.Examinable
import org.bukkit.inventory.meta.ArmorMeta
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

data class ArmorTrim(
    val showInTooltip: Boolean,
) : Examinable {

    companion object : ItemComponentBridge<ArmorTrim> {
        override fun codec(id: String): ItemComponentType<ArmorTrim> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ArmorTrim> {
        override fun read(holder: ItemComponentHolder): ArmorTrim? {
            val im = holder.item.itemMeta as? ArmorMeta ?: return null
            val showInTooltip = !im.hasItemFlag(org.bukkit.inventory.ItemFlag.HIDE_ARMOR_TRIM)
            return ArmorTrim(showInTooltip)
        }

        override fun write(holder: ItemComponentHolder, value: ArmorTrim) {
            holder.item.editMeta<ArmorMeta> {
                if (value.showInTooltip) {
                    it.removeItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ARMOR_TRIM)
                } else {
                    it.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ARMOR_TRIM)
                }
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta<ArmorMeta> {
                it.trim = null
            }
        }
    }

    data class Template(
        val showInTooltip: Boolean,
    ) : ItemTemplate<ArmorTrim> {
        override val componentType: ItemComponentType<ArmorTrim> = ItemComponentTypes.TRIM

        override fun generate(context: GenerationContext): GenerationResult<ArmorTrim> {
            return GenerationResult.of(ArmorTrim(showInTooltip))
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
            return Template(showInTooltip)
        }
    }
}
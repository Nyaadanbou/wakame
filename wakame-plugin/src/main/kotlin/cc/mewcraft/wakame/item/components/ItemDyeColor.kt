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
import cc.mewcraft.wakame.util.editMeta
import cc.mewcraft.wakame.util.javaTypeOf
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.bukkit.Color
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

data class ItemDyeColor(
    val rgb: Int,
    val showInTooltip: Boolean,
) : Examinable {

    companion object : ItemComponentBridge<ItemDyeColor>, ItemComponentMeta {
        override fun codec(id: String): ItemComponentType<ItemDyeColor> {
            return Codec(id)
        }

        override fun templateType(): ItemTemplateType<*> {
            return TemplateType
        }

        override val configPath: String = ItemComponentConstants.DYED_COLOR
        override val tooltipKey: Key = ItemComponentConstants.createKey { DYED_COLOR }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemDyeColor> {
        override fun read(holder: ItemComponentHolder): ItemDyeColor? {
            val im = holder.item.itemMeta as? LeatherArmorMeta ?: return null
            val rgb = im.color.asRGB()
            val showInTooltip = !im.hasItemFlag(ItemFlag.HIDE_DYE)
            return ItemDyeColor(rgb, showInTooltip)
        }

        override fun write(holder: ItemComponentHolder, value: ItemDyeColor) {
            holder.item.editMeta<LeatherArmorMeta> {
                it.setColor(Color.fromRGB(value.rgb))
                if (value.showInTooltip) {
                    it.removeItemFlags(ItemFlag.HIDE_DYE)
                } else {
                    it.addItemFlags(ItemFlag.HIDE_DYE)
                }
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta<LeatherArmorMeta> {
                it.setColor(null)
            }
        }
    }

    data class Template(
        val rgb: Int,
        val showInTooltip: Boolean,
    ) : ItemTemplate<ItemDyeColor> {
        override val componentType: ItemComponentType<ItemDyeColor> = ItemComponentTypes.DYED_COLOR

        override fun generate(context: GenerationContext): GenerationResult<ItemDyeColor> {
            return GenerationResult.of(ItemDyeColor(rgb, showInTooltip))
        }
    }

    private data object TemplateType : ItemTemplateType<Template> {
        override val typeToken: TypeToken<Template> = typeTokenOf()

        override fun deserialize(type: Type, node: ConfigurationNode): Template {
            val rgb = node.node("rgb").int.takeIf { it in 0x000000..0xFFFFFF } ?: throw SerializationException(node, javaTypeOf<Int>(), "RGB value out of range")
            val showInTooltip = node.node("show_in_tooltip").getBoolean(true)
            return Template(
                rgb = rgb,
                showInTooltip = showInTooltip
            )
        }
    }
}

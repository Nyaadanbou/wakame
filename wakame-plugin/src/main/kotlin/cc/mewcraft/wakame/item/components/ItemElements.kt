package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.GenerationContext
import cc.mewcraft.wakame.item.component.GenerationResult
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.random.Pool
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

interface ItemElements : Examinable, TooltipProvider {

    val elements: List<Element>

    data class Value(
        override val elements: List<Element>,
    ) : ItemElements {
        override fun provideDisplayLore(): LoreLine {
            if (!showInTooltip) {
                return LoreLine.noop()
            }
            return LoreLine.simple(tooltipKey, tooltipText.render(elements, Element::displayName))
        }

        companion object : ItemComponentConfig(ItemComponentConstants.ELEMENTS) {
            val tooltipKey: TooltipKey = ItemComponentConstants.createKey { ELEMENTS }
            val tooltipText: MergedTooltip = MergedTooltip()
        }
    }

    class Codec(
        override val id: String,
    ) : ItemComponentType<ItemElements, ItemComponentHolder.NBT> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.NBT

        override fun read(holder: ItemComponentHolder.NBT): ItemElements? {
            TODO("Not yet implemented")
        }

        override fun write(holder: ItemComponentHolder.NBT, value: ItemElements) {
            TODO("Not yet implemented")
        }

        override fun remove(holder: ItemComponentHolder.NBT) {
            TODO("Not yet implemented")
        }
    }

    data class Template(
        val elementSelector: Pool<Element, SchemaGenerationContext>,
    ) : ItemTemplate<ItemElements> {
        override fun generate(context: GenerationContext): GenerationResult<ItemElements> {
            TODO("Not yet implemented")
        }

        companion object : ItemTemplate.Serializer<Template> {
            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                TODO("Not yet implemented")
            }
        }
    }
}
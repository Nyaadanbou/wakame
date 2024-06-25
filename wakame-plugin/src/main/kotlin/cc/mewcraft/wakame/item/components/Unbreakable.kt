package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.component.GenerationContext
import cc.mewcraft.wakame.item.component.GenerationResult
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.ItemTemplate
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

interface Unbreakable : Examinable, TooltipProvider {

    val showInTooltip: Boolean

    data class Value(
        override val showInTooltip: Boolean,
    ) : Unbreakable

    class Codec(
        override val id: String,
    ) : ItemComponentType<Unbreakable, ItemComponentHolder.Item> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.ITEM

        override fun read(holder: ItemComponentHolder.Item): Unbreakable? {
            TODO("Not yet implemented")
        }

        override fun write(holder: ItemComponentHolder.Item, value: Unbreakable) {
            TODO("Not yet implemented")
        }

        override fun remove(holder: ItemComponentHolder.Item) {
            TODO("Not yet implemented")
        }
    }

    data class Template(
        val showInTooltip: Boolean,
    ) : ItemTemplate<Value> {
        companion object : ItemTemplate.Serializer<Template> {
            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                TODO("Not yet implemented")
            }
        }

        override fun generate(context: GenerationContext): GenerationResult<Value> {
            TODO("Not yet implemented")
        }
    }
}
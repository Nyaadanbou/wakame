package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

interface CustomName : Examinable {

    class Codec(
        override val id: String,
    ) : ItemComponentType<Component, ItemComponentHolder.Item> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.ITEM

        override fun read(holder: ItemComponentHolder.Item): Component? {
            return holder.item.itemMeta.displayName()
        }

        override fun write(holder: ItemComponentHolder.Item, value: Component) {
            holder.item.editMeta { it.displayName(value) }
        }

        override fun remove(holder: ItemComponentHolder.Item) {
            holder.item.editMeta { it.displayName(null) }
        }
    }

    data class Template(
        val customName: String,
    ) : ItemTemplate<CustomName> {
        override fun generate(context: GenerationContext): GenerationResult<CustomName> {
            TODO("Not yet implemented")
        }

        companion object : ItemTemplateType<Template> {
            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                TODO("Not yet implemented")
            }
        }
    }
}

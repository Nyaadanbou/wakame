package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

interface ItemName : Examinable {
    data class Template(
        val itemName: String,
    ) : ItemTemplate<Component> {
        override fun generate(context: GenerationContext): GenerationResult<Component> {
            TODO("Not yet implemented")
        }

        companion object : ItemTemplateType<Template> {
            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                TODO("Not yet implemented")
            }
        }
    }
}
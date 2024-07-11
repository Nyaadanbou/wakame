package cc.mewcraft.wakame.item.components

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
import org.spongepowered.configurate.ConfigurationNode

interface ItemBow {
    companion object : ItemComponentBridge<Unit> {
        override fun codec(id: String): ItemComponentType<Unit> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<Unit> {
        override fun read(holder: ItemComponentHolder): Unit? {
            return if (holder.hasTag()) Unit else null
        }

        override fun write(holder: ItemComponentHolder, value: Unit) {
            holder.putTag()
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }
    }

    data object Template : ItemTemplate<Unit> {
        override val componentType: ItemComponentType<Unit> = ItemComponentTypes.BOW

        override fun generate(context: GenerationContext): GenerationResult<Unit> {
            return GenerationResult.of(Unit)
        }
    }

    private data class TemplateType(
        override val id: String,
    ) : ItemTemplateType<Template> {
        override val type: TypeToken<Template> = typeTokenOf()

        override fun decode(node: ConfigurationNode): Template {
            return Template
        }
    }
}
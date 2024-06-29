package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

// TODO 完成组件: ItemTracks

interface ItemTracks : Examinable {

    /* data */ class Value() : ItemTracks {

    }

    data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemTracks> {
        override fun read(holder: ItemComponentHolder): ItemTracks? {
            val tag = holder.getTag() ?: return null
            return Value()
        }

        override fun write(holder: ItemComponentHolder, value: ItemTracks) {
            val tag = holder.getTagOrCreate()
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }
    }

    data object Template : ItemTemplate<ItemTracks>, ItemTemplateType<Template> {
        override val typeToken: TypeToken<Template> = typeTokenOf()

        override fun generate(context: GenerationContext): GenerationResult<ItemTracks> {
            return GenerationResult.of(Value())
        }

        override fun deserialize(type: Type, node: ConfigurationNode): Template {
            return this
        }
    }
}
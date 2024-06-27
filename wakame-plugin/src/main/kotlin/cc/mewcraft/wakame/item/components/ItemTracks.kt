package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

// TODO 完成组件: ItemTracks

interface ItemTracks : Examinable {

    /* data */ class Value() : ItemTracks {

    }

    data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemTracks, ItemComponentHolder.NBT> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.NBT

        override fun read(holder: ItemComponentHolder.NBT): ItemTracks {
            return Value()
        }

        override fun write(holder: ItemComponentHolder.NBT, value: ItemTracks) {

        }

        override fun remove(holder: ItemComponentHolder.NBT) {
            // no-op
        }
    }

    data object Template : ItemTemplate<ItemTracks>, ItemTemplateType<Template> {
        override fun generate(context: GenerationContext): GenerationResult<ItemTracks> {
            return GenerationResult.of(Value())
        }

        override fun deserialize(type: Type, node: ConfigurationNode): Template {
            return this
        }
    }
}
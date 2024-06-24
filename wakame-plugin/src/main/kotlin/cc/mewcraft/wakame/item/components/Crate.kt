package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.GenerationContext
import cc.mewcraft.wakame.item.component.GenerationResult
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentTemplate
import cc.mewcraft.wakame.item.component.ItemComponentType
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

interface Crate : Examinable {

    /**
     * 盲盒的唯一标识.
     */
    val key: Key

    data class Value(
        override val key: Key,
    ) : Crate

    class Codec(
        override val id: String,
    ) : ItemComponentType<Crate, ItemComponentHolder.NBT> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.NBT

        override fun read(holder: ItemComponentHolder.NBT): Crate? {
            TODO("Not yet implemented")
        }

        override fun write(holder: ItemComponentHolder.NBT, value: Crate) {
            TODO("Not yet implemented")
        }

        override fun remove(holder: ItemComponentHolder.NBT) {
            TODO("Not yet implemented")
        }
    }

    data class Template(
        val key: Key,
    ) : ItemComponentTemplate<Crate> {
        override fun generate(context: GenerationContext): GenerationResult<Crate> {
            TODO("Not yet implemented")
        }

        companion object : ItemComponentTemplate.Serializer<Template> {
            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                TODO("Not yet implemented")
            }
        }
    }
}
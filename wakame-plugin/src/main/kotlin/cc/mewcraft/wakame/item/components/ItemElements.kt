package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.component.GenerationContext
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentTemplate
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.random.Pool
import net.kyori.examination.Examinable

interface ItemElements : Examinable {

    class Value(val elementList: List<Element>) : ItemElements

    class Codec(override val id: String) : ItemComponentType.Valued<ItemElements, ItemComponentHolder.NBT> {
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

    class Template(val elementSelector: Pool<Element, SchemaGenerationContext>) : ItemComponentTemplate<ItemElements> {
        override fun generate(context: GenerationContext): cc.mewcraft.wakame.item.component.GenerationResult<ItemElements> {
            TODO("Not yet implemented")
        }
    }
}
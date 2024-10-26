package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.getByteArrayOrNull
import cc.mewcraft.wakame.util.toSimpleString
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import xyz.xenondevs.commons.collections.mapToByteArray
import java.util.stream.Stream


data class ItemElements(
    /**
     * 所有的元素.
     */
    val elements: Set<Element>,
) : Examinable {

    companion object : ItemComponentBridge<ItemElements> {
        /**
         * 该组件的配置文件.
         */
        private val config: ItemComponentConfig = ItemComponentConfig.provide(ItemConstants.ELEMENTS)

        /**
         * 构建一个 [ItemElements] 的实例.
         */
        fun of(elements: Collection<Element>): ItemElements {
            return ItemElements(ObjectArraySet(elements))
        }

        /**
         * 构建一个 [ItemElements] 的实例.
         */
        fun of(vararg elements: Element): ItemElements {
            return of(elements.toList())
        }

        override fun codec(id: String): ItemComponentType<ItemElements> {
            return Codec(id)
        }
    }

    override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
        ExaminableProperty.of("elements", elements),
    )

    override fun toString(): String {
        return toSimpleString()
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemElements> {

        override fun read(holder: ItemComponentHolder): ItemElements? {
            val elementSet = holder.getTag()
                ?.getByteArrayOrNull(TAG_VALUE)
                ?.mapTo(ObjectArraySet(2), ElementRegistry::getBy)
                ?: return null
            return ItemElements(elements = elementSet)
        }

        override fun write(holder: ItemComponentHolder, value: ItemElements) {
            require(value.elements.isNotEmpty()) { "The set of elements must not be empty" }
            holder.editTag { tag ->
                val byteArray = value.elements.mapToByteArray(Element::binaryId)
                tag.putByteArray(TAG_VALUE, byteArray)
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }

        private companion object {
            const val TAG_VALUE = "raw"
        }
    }
}

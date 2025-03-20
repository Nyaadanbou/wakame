package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.element.ElementType
import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.adventure.toSimpleString
import cc.mewcraft.wakame.util.data.getByteArrayOrNull
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import xyz.xenondevs.commons.collections.mapToByteArray
import java.util.stream.Stream


data class ItemElements(
    /**
     * 所有的元素.
     */
    val elements: Set<RegistryEntry<ElementType>>,
) : Examinable {

    companion object : ItemComponentBridge<ItemElements> {
        /**
         * 该组件的配置文件.
         */
        private val config: ItemComponentConfig = ItemComponentConfig.provide(ItemConstants.ELEMENTS)

        /**
         * 构建一个 [ItemElements] 的实例.
         */
        fun of(elements: Collection<RegistryEntry<ElementType>>): ItemElements {
            return ItemElements(ObjectArraySet(elements))
        }

        /**
         * 构建一个 [ItemElements] 的实例.
         */
        fun of(vararg elements: RegistryEntry<ElementType>): ItemElements {
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
            val elementSet = holder.getNbt()
                ?.getByteArrayOrNull(TAG_VALUE)
                ?.mapTo(ObjectArraySet(4)) { KoishRegistries.ELEMENT.getEntryOrThrow(it.toInt()) }
                ?: return null
            return ItemElements(elementSet)
        }

        override fun write(holder: ItemComponentHolder, value: ItemElements) {
            require(value.elements.isNotEmpty()) { "The set of elements must not be empty" }
            holder.editNbt { tag ->
                val integerIdByteArray = value.elements.mapToByteArray { KoishRegistries.ELEMENT.getRawIdOrThrow(it.value).toByte() }
                tag.putByteArray(TAG_VALUE, integerIdByteArray)
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeNbt()
        }

        private companion object {
            const val TAG_VALUE = "raw"
        }
    }
}

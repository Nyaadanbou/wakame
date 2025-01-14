package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.kizami.KizamiType
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.getByteArrayOrNull
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import net.kyori.examination.Examinable
import xyz.xenondevs.commons.collections.mapToByteArray


data class ItemKizamiz(
    /**
     * 所有的铭刻.
     */
    val kizamiz: Set<RegistryEntry<KizamiType>>,
) : Examinable {

    companion object : ItemComponentBridge<ItemKizamiz> {
        /**
         * 该组件的配置文件.
         */
        private val config = ItemComponentConfig.provide(ItemConstants.KIZAMIZ)

        /**
         * 构建一个 [ItemKizamiz] 的实例.
         */
        fun of(kizamiz: Collection<RegistryEntry<KizamiType>>): ItemKizamiz {
            return ItemKizamiz(ObjectArraySet(kizamiz))
        }

        /**
         * 构建一个 [ItemKizamiz] 的实例.
         */
        fun of(vararg kizamiz: RegistryEntry<KizamiType>): ItemKizamiz {
            return of(kizamiz.toList())
        }

        override fun codec(id: String): ItemComponentType<ItemKizamiz> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemKizamiz> {
        override fun read(holder: ItemComponentHolder): ItemKizamiz? {
            val tag = holder.getTag() ?: return null
            val kizamiSet = tag.getByteArrayOrNull(TAG_VALUE)
                ?.mapTo(ObjectArraySet(4)) { KoishRegistries.KIZAMI.getEntryOrThrow(it.toInt()) }
                ?: return null
            return ItemKizamiz(kizamiz = kizamiSet)
        }

        override fun write(holder: ItemComponentHolder, value: ItemKizamiz) {
            require(value.kizamiz.isNotEmpty()) { "The set of kizami must be not empty" }
            holder.editTag { tag ->
                val byteArray = value.kizamiz.mapToByteArray { it.value.integerId.toByte() }
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
package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.kizami2.Kizami
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.data.ListTag
import cc.mewcraft.wakame.util.data.NbtUtils
import cc.mewcraft.wakame.util.data.getListOrNull
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import net.kyori.examination.Examinable
import net.minecraft.nbt.StringTag


data class ItemKizamiz(
    /**
     * 所有的铭刻.
     */
    val kizamiz: Set<RegistryEntry<Kizami>>,
) : Examinable {

    companion object : ItemComponentBridge<ItemKizamiz> {
        /**
         * 该组件的配置文件.
         */
        private val config = ItemComponentConfig.provide(ItemConstants.KIZAMIZ)

        /**
         * 构建一个 [ItemKizamiz] 的实例.
         */
        fun of(kizamiz: Collection<RegistryEntry<Kizami>>): ItemKizamiz {
            return ItemKizamiz(ObjectArraySet(kizamiz))
        }

        /**
         * 构建一个 [ItemKizamiz] 的实例.
         */
        fun of(vararg kizamiz: RegistryEntry<Kizami>): ItemKizamiz {
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
            val tag = holder.getNbt() ?: return null
            val values = tag.getListOrNull(TAG_VALUE, NbtUtils.TAG_STRING)
                ?.mapTo(ObjectArraySet(8)) { KoishRegistries2.KIZAMI.getEntryOrThrow(it.asString) }
                ?: return null
            return ItemKizamiz(values)
        }

        override fun write(holder: ItemComponentHolder, value: ItemKizamiz) {
            require(value.kizamiz.isNotEmpty()) { "The set of kizami must be not empty" }
            holder.editNbt { tag ->
                val ids = value.kizamiz.map { it.unwrap().key().asString() }
                tag.put(TAG_VALUE, ListTag {
                    ids.forEach { add(StringTag.valueOf(it)) }
                })
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
package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.item.ItemMetaKeys
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.registry.KizamiRegistry
import cc.mewcraft.wakame.util.getByteArrayOrNull
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import me.lucko.helper.nbt.ShadowTagType
import net.kyori.adventure.key.Key

/**
 * 物品的铭刻。
 */
@JvmInline
value class BKizamiMeta(
    private val accessor: ItemMetaAccessor,
) : BinaryItemMeta<Set<Kizami>> {
    override val key: Key
        get() = ItemMetaKeys.KIZAMI
    override val exists: Boolean
        get() = accessor.rootOrNull?.contains(ItemMetaKeys.KIZAMI.value(), ShadowTagType.BYTE_ARRAY) ?: false

    override fun getOrNull(): Set<Kizami>? {
        return accessor.rootOrNull
            ?.getByteArrayOrNull(key.value())
            ?.mapTo(ObjectArraySet(4)) {
                KizamiRegistry.getBy(it)
            }
    }

    override fun set(value: Set<Kizami>) {
        require(value.isNotEmpty()) { "Set<Kizami> must be not empty" }
        val byteArray = value.map { it.binaryId }.toByteArray()
        accessor.rootOrCreate.putByteArray(key.value(), byteArray)
    }

    fun set(value: Collection<Kizami>) {
        set(value.toHashSet())
    }

    override fun remove() {
        accessor.rootOrNull?.remove(key.value())
    }
}

internal fun BKizamiMeta?.getOrEmpty(): Set<Kizami> {
    return this?.getOrNull() ?: emptySet()
}
package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.item.ItemMetaKeys
import cc.mewcraft.wakame.item.scheme.meta.Durability
import cc.mewcraft.wakame.util.CompoundShadowTag
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.toStableShort
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key

internal class BDurabilityMeta(
    private val holder: ItemMetaAccessorImpl,
) : BinaryItemMeta<Durability> {
    override val key: Key = ItemMetaKeys.DURABILITY

    /**
     * Gets damage.
     */
    fun damage(): Int {
        return holder.rootOrNull?.getCompoundOrNull(key.value())?.getInt(DAMAGE_TAG) ?: 0
    }

    /**
     * Sets damage.
     *
     * @throws IllegalStateException
     */
    fun damage(value: Int) {
        val compound = holder.rootOrNull?.getCompoundOrNull(key.value())
        if (compound != null) {
            compound.putShort(DAMAGE_TAG, value.toStableShort())
        } else throw IllegalStateException("Can't set 'damage' for empty durability")
    }

    /**
     * Gets threshold.
     *
     * @throws IllegalStateException
     */
    fun threshold(): Int {
        return holder.rootOrNull?.getCompoundOrNull(key.value())?.getInt(THRESHOLD_TAG) ?: throw IllegalStateException("Can't get 'threshold' for empty durability")
    }

    /**
     * Sets threshold.
     *
     * @throws IllegalStateException
     */
    fun threshold(value: Int) {
        val compound = holder.rootOrNull?.getCompoundOrNull(key.value())
        if (compound != null) {
            compound.putShort(THRESHOLD_TAG, value.toStableShort())
        } else {
            throw IllegalStateException("Can't set 'threshold' for empty durability")
        }
    }

    override fun getOrNull(): Durability? {
        return holder.rootOrNull?.getCompoundOrNull(key.value())?.let { compound ->
            val threshold = compound.getInt(THRESHOLD_TAG)
            val damage = compound.getInt(DAMAGE_TAG)
            Durability(threshold, damage)
        }
    }

    override fun set(value: Durability) {
        holder.rootOrCreate.put(key.value(), CompoundShadowTag {
            putShort(THRESHOLD_TAG, value.threshold.toStableShort())
            putShort(DAMAGE_TAG, value.damage.toStableShort())
        })
    }

    override fun remove() {
        holder.rootOrNull?.remove(key.value())
    }

    companion object : ItemMetaCompanion {
        private const val THRESHOLD_TAG = "threshold"
        private const val DAMAGE_TAG = "damage"

        override operator fun contains(compound: CompoundShadowTag): Boolean {
            return compound.contains(ItemMetaKeys.DURABILITY.value(), ShadowTagType.COMPOUND)
        }
    }
}
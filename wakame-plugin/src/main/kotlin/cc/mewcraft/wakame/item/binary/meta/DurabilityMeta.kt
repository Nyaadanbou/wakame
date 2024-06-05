package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.item.ItemMetaConstants
import cc.mewcraft.wakame.item.schema.meta.Durability
import cc.mewcraft.wakame.util.CompoundShadowTag
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.toStableShort
import me.lucko.helper.nbt.ShadowTagType
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

@JvmInline
value class BDurabilityMeta(
    private val accessor: ItemMetaAccessor,
) : BinaryItemMeta<Durability> {
    override val key: Key
        get() = ItemMetaConstants.createKey { DURABILITY }

    override val exists: Boolean
        get() = accessor.rootOrNull?.contains(ItemMetaConstants.DURABILITY, ShadowTagType.COMPOUND) ?: false

    /**
     * Gets damage.
     */
    fun damage(): Int {
        return accessor.rootOrNull?.getCompoundOrNull(key.value())?.getInt(DAMAGE_TAG) ?: 0
    }

    /**
     * Sets damage.
     *
     * @throws IllegalStateException
     */
    fun damage(value: Int) {
        val compound = accessor.rootOrNull?.getCompoundOrNull(key.value())
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
        return accessor.rootOrNull?.getCompoundOrNull(key.value())?.getInt(THRESHOLD_TAG) ?: throw IllegalStateException("Can't get 'threshold' for empty durability")
    }

    /**
     * Sets threshold.
     *
     * @throws IllegalStateException
     */
    fun threshold(value: Int) {
        val compound = accessor.rootOrNull?.getCompoundOrNull(key.value())
        if (compound != null) {
            compound.putShort(THRESHOLD_TAG, value.toStableShort())
        } else {
            throw IllegalStateException("Can't set 'threshold' for empty durability")
        }
    }

    override fun getOrNull(): Durability? {
        return accessor.rootOrNull?.getCompoundOrNull(key.value())?.let { compound ->
            val threshold = compound.getInt(THRESHOLD_TAG)
            val damage = compound.getInt(DAMAGE_TAG)
            Durability(threshold, damage)
        }
    }

    override fun set(value: Durability) {
        accessor.rootOrCreate.put(key.value(), CompoundShadowTag {
            putShort(THRESHOLD_TAG, value.threshold.toStableShort())
            putShort(DAMAGE_TAG, value.damage.toStableShort())
        })
    }

    override fun remove() {
        accessor.rootOrNull?.remove(key.value())
    }

    override fun provideDisplayLore(): LoreLine {
        val durability = get()
        val key = ItemMetaSupport.getLineKey(this) ?: return LoreLine.noop()
        val text = ItemMetaSupport.mini().deserialize(
            tooltips.single,
            Placeholder.component("threshold", text(durability.threshold)),
            Placeholder.component("damage", text(durability.damage)),
            Placeholder.component("percent", text(durability.damagePercent))
        )
        return LoreLine.simple(key, listOf(text))
    }

    private companion object : ItemMetaConfig(
        ItemMetaConstants.DURABILITY
    ) {
        const val THRESHOLD_TAG = "threshold"
        const val DAMAGE_TAG = "damage"

        val tooltips: SingleTooltips = SingleTooltips()
    }
}
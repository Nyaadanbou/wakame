package cc.mewcraft.wakame.item.binary.cell.curse

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.item.CurseKeys
import cc.mewcraft.wakame.item.binary.cell.curse.EntityKillsCurse
import cc.mewcraft.wakame.item.binary.cell.curse.PeakDamageCurse
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.cell.curse.SchemaCurse
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.registry.EntityReferenceRegistry
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import cc.mewcraft.wakame.item.binary.cell.curse.EntityKillsCurse as BEntityKillsCurse
import cc.mewcraft.wakame.item.binary.cell.curse.PeakDamageCurse as BPeakDamageCurse

object BinaryCurseFactory {

    /**
     * Creates an empty curse.
     */
    fun empty(): BinaryCurse = EmptyBinaryCurse

    /**
     * Creates a curse from a NBT source.
     *
     * @param compound the compound tag
     * @return a new instance
     */
    fun decode(compound: CompoundShadowTag): BinaryCurse {
        if (compound.isEmpty) {
            return empty()
        }

        val id = compound.getString(NekoTags.Cell.CURSE_KEY)
        val ret: BinaryCurse = when (id) {
            CurseKeys.ENTITY_KILLS.value() -> {
                val index = EntityReferenceRegistry.INSTANCES[compound.getString(EntityKillsCurse.INDEX_TAG_NAME)]
                val count = compound.getInt(EntityKillsCurse.COUNT_TAG_NAME)
                BEntityKillsCurse(index, count)
            }

            CurseKeys.PEAK_DAMAGE.value() -> {
                val element = ElementRegistry.getBy(compound.getByte(PeakDamageCurse.ELEMENT_TAG_NAME))
                val amount = compound.getInt(PeakDamageCurse.AMOUNT_TAG_NAME)
                BPeakDamageCurse(element, amount)
            }

            else -> empty()
        }

        return ret
    }

    /**
     * Creates a curse from a schema source.
     */
    fun generate(context: SchemaGenerationContext, schemaCurse: SchemaCurse): BinaryCurse {
        return schemaCurse.generate(context)
    }

}
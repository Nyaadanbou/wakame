package cc.mewcraft.wakame.item.binary.cell.curse

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.item.CurseBinaryKeys
import cc.mewcraft.wakame.item.CurseConstants
import cc.mewcraft.wakame.item.binary.cell.curse.type.BinaryEmptyCurse
import cc.mewcraft.wakame.item.binary.cell.curse.type.BinaryEntityKillsCurse
import cc.mewcraft.wakame.item.binary.cell.curse.type.BinaryPeakDamageCurse
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.cell.curse.SchemaCurse
import cc.mewcraft.wakame.util.Key
import me.lucko.helper.shadows.nbt.CompoundShadowTag

object BinaryCurseFactory {

    /**
     * Creates an empty curse.
     */
    fun empty(): BinaryCurse = BinaryEmptyCurse()

    /**
     * Creates a curse from a NBT source.
     *
     * @param compound the compound tag
     * @return a new instance
     */
    fun wrap(compound: CompoundShadowTag): BinaryCurse {
        if (compound.isEmpty) {
            // It's an empty binary curse,
            // just return the singleton.
            return empty()
        }

        val id = compound.getString(CurseBinaryKeys.CURSE_IDENTIFIER)
        val key = Key(id)
        require(key.namespace() == Namespaces.CURSE)
        val ret = when (key.value()) {
            CurseConstants.ENTITY_KILLS -> BinaryEntityKillsCurse(compound)
            CurseConstants.PEAK_DAMAGE -> BinaryPeakDamageCurse(compound)
            else -> throw IllegalArgumentException("Failed to parse NBT tag: ${compound.asString()}")
        }

        return ret
    }

    /**
     * Reifies a [SchemaCurse] with given [context].
     */
    fun reify(schema: SchemaCurse, context: SchemaGenerationContext): BinaryCurse {
        return schema.reify(context)
    }

}
package cc.mewcraft.wakame.item.scheme.curse

import cc.mewcraft.wakame.item.CurseKeys
import cc.mewcraft.wakame.item.SchemeData
import cc.mewcraft.wakame.item.binary.curse.BinaryCurse
import cc.mewcraft.wakame.item.binary.curse.EntityKillsCurse
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.reference.EntityReference
import cc.mewcraft.wakame.util.NumericValue
import cc.mewcraft.wakame.util.toStableInt
import net.kyori.adventure.key.Key

@SchemeData
data class EntityKillsCurse(
    private val count: NumericValue,
    private val index: EntityReference,
) : SchemeCurse {

    override fun generate(context: SchemeGenerationContext): BinaryCurse {
        val randomCount = count.calculate(context.itemLevel).toStableInt()
        return EntityKillsCurse(index, randomCount)
    }

    override val key: Key = CurseKeys.ENTITY_KILLS
}
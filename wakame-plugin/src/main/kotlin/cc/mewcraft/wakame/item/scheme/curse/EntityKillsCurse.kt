package cc.mewcraft.wakame.item.scheme.curse

import cc.mewcraft.wakame.item.CurseKeys
import cc.mewcraft.wakame.item.SchemeData
import cc.mewcraft.wakame.item.binary.curse.BinaryCurse
import cc.mewcraft.wakame.item.binary.curse.EntityKillsCurse
import cc.mewcraft.wakame.reference.EntityReference
import cc.mewcraft.wakame.util.NumericValue
import cc.mewcraft.wakame.util.toStableInt
import net.kyori.adventure.key.Key

@SchemeData
class EntityKillsCurse(
    private val index: EntityReference,
    private val count: NumericValue,
) : SchemeCurse {

    override fun generate(scalingFactor: Int): BinaryCurse {
        val randomCount = count.calculate(scalingFactor).toStableInt()
        return EntityKillsCurse(index, randomCount)
    }

    override val key: Key = CurseKeys.ENTITY_KILLS
}
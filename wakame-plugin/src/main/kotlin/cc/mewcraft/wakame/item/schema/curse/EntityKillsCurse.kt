package cc.mewcraft.wakame.item.schema.curse

import cc.mewcraft.wakame.item.CurseKeys
import cc.mewcraft.wakame.item.SchemaData
import cc.mewcraft.wakame.item.binary.curse.BinaryCurse
import cc.mewcraft.wakame.item.binary.curse.EntityKillsCurse
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.reference.EntityReference
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.toStableInt
import net.kyori.adventure.key.Key

@SchemaData
data class EntityKillsCurse(
    private val count: RandomizedValue,
    private val index: EntityReference,
) : SchemaCurse {

    override fun generate(context: SchemaGenerationContext): BinaryCurse {
        val randomCount = count.calculate(context.level).toStableInt()
        return EntityKillsCurse(index, randomCount)
    }

    override val key: Key = CurseKeys.ENTITY_KILLS
}
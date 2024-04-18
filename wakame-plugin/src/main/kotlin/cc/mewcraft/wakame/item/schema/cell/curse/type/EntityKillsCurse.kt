package cc.mewcraft.wakame.item.schema.cell.curse.type

import cc.mewcraft.wakame.item.CurseConstants
import cc.mewcraft.wakame.item.binary.cell.curse.BinaryCurse
import cc.mewcraft.wakame.item.binary.cell.curse.type.BinaryEntityKillsCurse
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.cell.curse.SchemaCurse
import cc.mewcraft.wakame.reference.EntityReference
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toStableInt
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

fun SchemaEntityKillsCurse(node: ConfigurationNode): SchemaEntityKillsCurse {
    val count = node.node("count").krequire<RandomizedValue>()
    val index = node.node("index").krequire<EntityReference>()
    return SchemaEntityKillsCurse(index, count)
}

/**
 * 代表一个实体击杀的蓝图诅咒。
 *
 * @property count 击杀数量
 * @property index 实体种类
 */
data class SchemaEntityKillsCurse(
    private val index: EntityReference,
    private val count: RandomizedValue,
) : SchemaCurse {
    override val key: Key = CurseConstants.createKey { ENTITY_KILLS }
    override fun reify(context: SchemaGenerationContext): BinaryCurse {
        val randomCount = count.calculate(context.level).toStableInt()
        return BinaryEntityKillsCurse(index, randomCount)
    }
}
package cc.mewcraft.wakame.item.schema.cell.curse

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.CurseConstants
import cc.mewcraft.wakame.item.binary.cell.curse.BinaryCurse
import cc.mewcraft.wakame.item.binary.cell.curse.BinaryCurseFactory
import cc.mewcraft.wakame.item.binary.cell.curse.EntityKillsCurse
import cc.mewcraft.wakame.item.binary.cell.curse.PeakDamageCurse
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.reference.EntityReference
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.toStableInt
import net.kyori.adventure.key.Key

/**
 * 代表一个空的蓝图诅咒。
 */
data object EmptySchemaCurse : SchemaCurse {
    override fun generate(context: SchemaGenerationContext): BinaryCurse = BinaryCurseFactory.empty()
    override val key: Key = Key(Namespaces.CURSE, "empty")
}

/**
 * 代表一个实体击杀的蓝图诅咒。
 *
 * @property count 击杀数量
 * @property index 实体种类
 */
data class EntityKillsCurse(
    private val count: RandomizedValue,
    private val index: EntityReference,
) : SchemaCurse {
    override fun generate(context: SchemaGenerationContext): BinaryCurse {
        val randomCount = count.calculate(context.level).toStableInt()
        return EntityKillsCurse(index, randomCount)
    }

    override val key: Key = CurseConstants.createKey { ENTITY_KILLS }
}

/**
 * 代表一个最高伤害的蓝图诅咒。
 *
 * @property amount 伤害数量
 * @property element 伤害类型
 */
data class PeakDamageCurse(
    private val amount: RandomizedValue,
    private val element: Element,
) : SchemaCurse {
    override fun generate(context: SchemaGenerationContext): BinaryCurse {
        val randomAmount = amount.calculate(context.level).toStableInt()
        return PeakDamageCurse(element, randomAmount)
    }

    override val key: Key = CurseConstants.createKey { PEAK_DAMAGE }
}
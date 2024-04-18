package cc.mewcraft.wakame.item.schema.cell.curse.type

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.CurseConstants
import cc.mewcraft.wakame.item.binary.cell.curse.BinaryCurse
import cc.mewcraft.wakame.item.binary.cell.curse.type.BinaryPeakDamageCurse
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.cell.curse.SchemaCurse
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toStableInt
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

fun SchemaPeakDamageCurse(node: ConfigurationNode): SchemaPeakDamageCurse {
    val amount = node.node("amount").krequire<RandomizedValue>()
    val element = node.node("element").krequire<Element>()
    return SchemaPeakDamageCurse(element, amount)
}

/**
 * 代表一个最高伤害的蓝图诅咒。
 *
 * @property amount 伤害数量
 * @property element 伤害类型
 */
data class SchemaPeakDamageCurse(
    private val element: Element,
    private val amount: RandomizedValue,
) : SchemaCurse {
    override val key: Key = CurseConstants.createKey { PEAK_DAMAGE }
    override fun reify(context: SchemaGenerationContext): BinaryCurse {
        val randomAmount = amount.calculate(context.level).toStableInt()
        return BinaryPeakDamageCurse(element, randomAmount)
    }
}
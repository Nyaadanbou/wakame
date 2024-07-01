package cc.mewcraft.wakame.item.components.cell.template.curses

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.CurseConstants
import cc.mewcraft.wakame.item.components.cell.Curse
import cc.mewcraft.wakame.item.components.cell.curses.CursePeakDamage
import cc.mewcraft.wakame.item.components.cell.template.TemplateCurse
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toStableInt
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

/**
 * 构建一个 [TemplateCursePeakDamage].
 */
fun TemplateCursePeakDamage(node: ConfigurationNode): TemplateCursePeakDamage {
    val amount = node.node("amount").krequire<RandomizedValue>()
    val element = node.node("element").krequire<Element>()
    return TemplateCursePeakDamage(element, amount)
}

/**
 * 代表一个最高伤害的蓝图诅咒。
 *
 * @property amount 伤害数量
 * @property element 伤害类型
 */
data class TemplateCursePeakDamage(
    private val element: Element,
    private val amount: RandomizedValue,
) : TemplateCurse {
    override val key: Key = CurseConstants.createKey { PEAK_DAMAGE }
    override fun generate(context: GenerationContext): Curse {
        val level = context.levelOrThrow
        val amount = this.amount.calculate(level).toStableInt()
        return CursePeakDamage(element, amount)
    }
}
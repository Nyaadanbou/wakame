package cc.mewcraft.wakame.item.scheme.curse

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.CurseKeys
import cc.mewcraft.wakame.item.SchemeData
import cc.mewcraft.wakame.item.binary.curse.BinaryCurse
import cc.mewcraft.wakame.item.binary.curse.PeakDamageCurse
import cc.mewcraft.wakame.util.NumericValue
import cc.mewcraft.wakame.util.toStableInt
import net.kyori.adventure.key.Key

@SchemeData
class PeakDamageCurse(
    private val amount: NumericValue,
    private val element: Element,
) : SchemeCurse {

    override fun generate(scalingFactor: Int): BinaryCurse {
        val randomAmount = amount.calculate(scalingFactor).toStableInt()
        return PeakDamageCurse(element, randomAmount)
    }

    override val key: Key = CurseKeys.PEAK_DAMAGE
}
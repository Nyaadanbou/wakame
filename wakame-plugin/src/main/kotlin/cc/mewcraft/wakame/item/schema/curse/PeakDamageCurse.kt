package cc.mewcraft.wakame.item.schema.curse

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.CurseKeys
import cc.mewcraft.wakame.item.SchemaData
import cc.mewcraft.wakame.item.binary.curse.BinaryCurse
import cc.mewcraft.wakame.item.binary.curse.PeakDamageCurse
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.toStableInt
import net.kyori.adventure.key.Key

@SchemaData
data class PeakDamageCurse(
    private val amount: RandomizedValue,
    private val element: Element,
) : SchemaCurse {

    override fun generate(context: SchemaGenerationContext): BinaryCurse {
        val randomAmount = amount.calculate(context.level).toStableInt()
        return PeakDamageCurse(element, randomAmount)
    }

    override val key: Key = CurseKeys.PEAK_DAMAGE
}
package cc.mewcraft.wakame.item.scheme.curse

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.CurseKeys
import cc.mewcraft.wakame.reference.EntityReference
import cc.mewcraft.wakame.util.NumericValue
import cc.mewcraft.wakame.util.requireKt
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

object SchemeCurseFactory {
    fun schemeOf(node: ConfigurationNode): SchemeCurse {
        val ret: SchemeCurse = when (val key = node.node("key").requireKt<Key>()) {
            CurseKeys.ENTITY_KILLS -> {
                val count = node.node("count").requireKt<NumericValue>()
                val index = node.node("index").requireKt<EntityReference>()
                EntityKillsCurse(count, index)
            }

            CurseKeys.PEAK_DAMAGE -> {
                val amount = node.node("amount").requireKt<NumericValue>()
                val element = node.node("element").requireKt<Element>()
                PeakDamageCurse(amount, element)
            }

            else -> throw IllegalArgumentException("Can't recognize curse key $key")
        }

        return ret
    }
}
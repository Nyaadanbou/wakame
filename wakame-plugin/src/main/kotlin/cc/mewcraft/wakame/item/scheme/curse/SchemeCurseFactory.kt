package cc.mewcraft.wakame.item.scheme.curse

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.CurseKeys
import cc.mewcraft.wakame.reference.EntityReference
import cc.mewcraft.wakame.util.NumericValue
import cc.mewcraft.wakame.util.typedRequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

object SchemeCurseFactory {
    fun schemeOf(node: ConfigurationNode): SchemeCurse {
        val ret: SchemeCurse = when (val key = node.node("key").typedRequire<Key>()) {
            CurseKeys.ENTITY_KILLS -> {
                val index = node.node("index").typedRequire<EntityReference>() // FIXME impl serialization
                val count = node.node("count").typedRequire<NumericValue>()
                EntityKillsCurse(index, count)
            }

            CurseKeys.PEAK_DAMAGE -> {
                val amount = node.node("amount").typedRequire<NumericValue>()
                val element = node.node("element").typedRequire<Element>()
                PeakDamageCurse(amount, element)
            }

            else -> throw IllegalArgumentException("Can't recognize curse key $key")
        }

        return ret
    }
}
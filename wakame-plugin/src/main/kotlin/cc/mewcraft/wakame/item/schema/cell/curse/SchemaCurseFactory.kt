package cc.mewcraft.wakame.item.schema.cell.curse

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.CurseConstants
import cc.mewcraft.wakame.reference.EntityReference
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

object SchemaCurseFactory {
    /**
     * Creates an empty schema curse.
     */
    fun empty(): SchemaCurse = EmptySchemaCurse

    /**
     * Creates a [SchemaCurse] from given configuration nodes.
     */
    fun schemaOf(node: ConfigurationNode): SchemaCurse {
        val ret: SchemaCurse = when (val key = node.node("key").krequire<Key>()) {
            CurseConstants.createKey { ENTITY_KILLS } -> {
                val count = node.node("count").krequire<RandomizedValue>()
                val index = node.node("index").krequire<EntityReference>()
                EntityKillsCurse(count, index)
            }

            CurseConstants.createKey { PEAK_DAMAGE } -> {
                val amount = node.node("amount").krequire<RandomizedValue>()
                val element = node.node("element").krequire<Element>()
                PeakDamageCurse(amount, element)
            }

            else -> throw IllegalArgumentException("Can't recognize curse key $key")
        }

        return ret
    }
}
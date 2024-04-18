package cc.mewcraft.wakame.item.schema.cell.curse

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.item.CurseConstants
import cc.mewcraft.wakame.item.schema.cell.curse.type.SchemaEmptyCurse
import cc.mewcraft.wakame.item.schema.cell.curse.type.SchemaEntityKillsCurse
import cc.mewcraft.wakame.item.schema.cell.curse.type.SchemaPeakDamageCurse
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

object SchemaCurseFactory {

    /**
     * Creates a [SchemaCurse] from the [node].
     */
    fun create(node: ConfigurationNode): SchemaCurse {
        val ret = when (val key = node.node("key").krequire<Key>()) {
            GenericKeys.EMPTY -> SchemaEmptyCurse()
            CurseConstants.createKey { ENTITY_KILLS } -> SchemaEntityKillsCurse(node)
            CurseConstants.createKey { PEAK_DAMAGE } -> SchemaPeakDamageCurse(node)
            else -> throw IllegalArgumentException("Unknown curse: $key")
        }

        return ret
    }
}
package cc.mewcraft.wakame.user

import cc.mewcraft.wakame.ability2.combo.PlayerCombo
import cc.mewcraft.wakame.entity.attribute.AttributeMap
import cc.mewcraft.wakame.entity.player.*
import cc.mewcraft.wakame.kizami2.KizamiMap
import org.bukkit.entity.Player
import java.util.*


/**
 * A wakame player in Paper platform.
 *
 * @property player the [paper player][Player]
 */
class PaperUser(
    private val player: Player,
) : User<Player> {

    override fun <T> player(): T {
        return player as T // let the runtime check the type
    }

    override val uniqueId: UUID
        get() = player.uniqueId

    override val level: Int
        get() = player.koishLevel

    override val kizamiMap: KizamiMap
        get() = player.kizamiContainer

    override val attributeMap: AttributeMap
        get() = player.attributeContainer // moved to ecs

    override val combo: PlayerCombo
        get() = player.combo

    override val attackSpeed: AttackCooldownContainer
        get() = player.attackCooldownContainer

    override var isInventoryListenable: Boolean
        get() = player.isInventoryListenable
        set(value) {
            player.isInventoryListenable = value
        }

    override fun cleanup() {
        combo.cleanup()
    }

    override fun toString(): String {
        return "PaperUser(uniqueId=$uniqueId)"
    }
}

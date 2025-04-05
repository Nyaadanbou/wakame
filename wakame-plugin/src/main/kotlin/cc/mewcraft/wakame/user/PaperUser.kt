package cc.mewcraft.wakame.user

import cc.mewcraft.wakame.ability2.combo.PlayerCombo
import cc.mewcraft.wakame.entity.attribute.AttributeMap
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelManager
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelType
import cc.mewcraft.wakame.kizami.KizamiMap
import cc.mewcraft.wakame.player.attackspeed.AttackSpeed
import cc.mewcraft.wakame.util.adventure.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import java.util.*
import java.util.stream.Stream


/**
 * A wakame player in Paper platform.
 *
 * @property player the [paper player][Player]
 */
class PaperUser(
    private val player: Player,
) : User<Player>, Examinable {

    override fun <T> player(): T {
        return player as T // let the runtime check the type
    }

    override val uniqueId: UUID
        get() = player.uniqueId

    override val level: Int
        get() = PlayerLevelManager.getOrDefault(uniqueId, 1)

    // TODO #373: move to ecs
    override val kizamiMap: KizamiMap = KizamiMap(this)

    override val attributeMap: AttributeMap
        get() = player.attributeContainer // moved to ecs

    // TODO #373: move to ecs
    override val combo: PlayerCombo = PlayerCombo(this.uniqueId)

    // TODO #373: move to ecs
    override val attackSpeed: AttackSpeed = AttackSpeed(this)

    // TODO #373: move to ecs
    @Volatile
    override var isInventoryListenable: Boolean = false

    init {
        // FIXME 临时方案. 让服务端不安装 AdventureLevel 也能够正常读取玩家等级
        if (PlayerLevelManager.integration?.type == PlayerLevelType.VANILLA) {
            isInventoryListenable = true
        }
    }

    override fun cleanup() {
        combo.cleanup()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("uniqueId", uniqueId),
            ExaminableProperty.of("level", level)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

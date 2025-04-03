package cc.mewcraft.wakame.user

import cc.mewcraft.wakame.ability2.combo.PlayerCombo
import cc.mewcraft.wakame.attribute.AttributeMap
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

val Player.koishLevel: Int
    get() = toUser().level

val Player.kizamiContainer: KizamiMap
    get() = toUser().kizamiMap

val Player.attributeContainer: AttributeMap
    get() = toUser().attributeMap

val Player.combo: PlayerCombo
    get() = toUser().combo

val Player.attackSpeed: AttackSpeed
    get() = toUser().attackSpeed

val Player.isInventoryListenable: Boolean
    get() = toUser().isInventoryListenable

/**
 * Adapts the [Player] into [NekoPlayer][User].
 */
fun Player.toUser(): User<Player> {
    return PlayerAdapters.get<Player>().adapt(this)
}

/**
 * A wakame player in Paper platform.
 *
 * @property player the [paper player][Player]
 */
class PaperUser(
    override val player: Player,
) : User<Player>, Examinable {
    override val uniqueId: UUID
        get() = player.uniqueId

    override val level: Int
        get() = PlayerLevelManager.getOrDefault(uniqueId, 1)

    override val kizamiMap: KizamiMap = KizamiMap(this)

    override val attributeMap: AttributeMap = AttributeMap(this)

    override val combo: PlayerCombo = PlayerCombo(this.uniqueId)

    override val attackSpeed: AttackSpeed = AttackSpeed(this)

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

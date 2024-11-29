package cc.mewcraft.wakame.user

import cc.mewcraft.wakame.attribute.AttributeMap
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelManager
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelType
import cc.mewcraft.wakame.kizami.KizamiMap
import cc.mewcraft.wakame.player.attackspeed.AttackSpeed
import cc.mewcraft.wakame.resource.ResourceMap
import cc.mewcraft.wakame.skill.SkillMap
import cc.mewcraft.wakame.skill.state.SkillState
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID
import java.util.stream.Stream

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

    override val skillMap: SkillMap = SkillMap(this)

    override val resourceMap: ResourceMap = ResourceMap(this)

    override val skillState: SkillState<Player> = SkillState(this)

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
        skillMap.cleanup()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("player", player),
            ExaminableProperty.of("uniqueId", uniqueId),
            ExaminableProperty.of("level", level),
            ExaminableProperty.of("skillState", skillState),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * Adapts the [Player] into [NekoPlayer][User].
 */
fun Player.toUser(): User<Player> {
    return PlayerAdapters.get<Player>().adapt(this)
}

/**
 * The adapter for [Player].
 */
class PaperPlayerAdapter : KoinComponent, Listener, PlayerAdapter<Player> {
    private val userManager: UserManager<Player> by inject()

    override fun adapt(player: Player): User<Player> {
        return userManager.getUser(player)
    }

    override fun adapt(uniqueId: UUID): User<Player> {
        return userManager.getUser(uniqueId)
    }
}
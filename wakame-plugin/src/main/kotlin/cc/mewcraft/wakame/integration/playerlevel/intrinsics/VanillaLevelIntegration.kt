package cc.mewcraft.wakame.integration.playerlevel.intrinsics

import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelIntegration
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelType
import org.bukkit.Server
import java.util.UUID

/**
 * A [player level integration][PlayerLevelIntegration] that returns the
 * [vanilla experience level](https://minecraft.wiki/w/Experience).
 */
class VanillaLevelIntegration(
    private val server: Server,
) : PlayerLevelIntegration {

    override val type: PlayerLevelType = PlayerLevelType.VANILLA

    override fun get(uuid: UUID): Int? {
        return server.getPlayer(uuid)?.level
    }

}
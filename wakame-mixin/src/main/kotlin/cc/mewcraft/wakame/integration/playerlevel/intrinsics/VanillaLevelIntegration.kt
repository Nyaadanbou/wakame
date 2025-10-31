package cc.mewcraft.wakame.integration.playerlevel.intrinsics

import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelIntegration
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelType
import java.util.*

/**
 * A [player level integration][PlayerLevelIntegration] that returns the
 * [vanilla experience level](https://minecraft.wiki/w/Experience).
 */
object VanillaLevelIntegration : PlayerLevelIntegration {

    override val levelType: PlayerLevelType = PlayerLevelType.VANILLA
    override fun get(uuid: UUID): Int? = SERVER.getPlayer(uuid)?.level

}
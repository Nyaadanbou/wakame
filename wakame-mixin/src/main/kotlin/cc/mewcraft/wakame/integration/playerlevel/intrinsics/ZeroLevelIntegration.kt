package cc.mewcraft.wakame.integration.playerlevel.intrinsics

import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelIntegration
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelType
import java.util.*

/**
 * A [player level integration][PlayerLevelIntegration] that always returns 0.
 * It can be used when requested implementation is not available at runtime.
 */
internal object ZeroLevelIntegration : PlayerLevelIntegration {

    override val levelType: PlayerLevelType = PlayerLevelType.ZERO
    override fun get(uuid: UUID): Int = 0

}
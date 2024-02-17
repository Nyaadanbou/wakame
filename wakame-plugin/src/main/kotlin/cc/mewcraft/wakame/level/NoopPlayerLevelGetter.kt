package cc.mewcraft.wakame.level

import cc.mewcraft.wakame.annotation.InternalApi
import java.util.UUID

/**
 * A [player level getter][PlayerLevelGetter] that always returns 1. It
 * should be used instead when requested implementation is not available at
 * runtime.
 */
@InternalApi
internal object NoopPlayerLevelGetter : PlayerLevelGetter {
    override fun get(uuid: UUID): Int = 1
}
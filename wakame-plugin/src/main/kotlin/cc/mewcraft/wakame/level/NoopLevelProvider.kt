package cc.mewcraft.wakame.level

import java.util.UUID

/**
 * A [player level getter][PlayerLevelProvider] that always returns 0. It
 * should be used when requested implementation is not available at runtime.
 */
internal object NoopLevelProvider : PlayerLevelProvider {
    override fun get(uuid: UUID): Int = 0
}
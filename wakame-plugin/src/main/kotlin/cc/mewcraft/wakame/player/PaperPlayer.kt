package cc.mewcraft.wakame.player

import cc.mewcraft.wakame.attribute.AttributeMap
import cc.mewcraft.wakame.kizami.KizamiMap
import cc.mewcraft.wakame.level.PlayerLevelProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID
import org.bukkit.entity.Player as PaperPlayer

/**
 * A player in Paper platform.
 *
 * @property player the [paper player][PaperPlayer]
 */
class PaperPlayer(
    override val player: PaperPlayer,
) : Player, KoinComponent {
    private val playerLevelProvider: PlayerLevelProvider by inject(mode = LazyThreadSafetyMode.NONE)

    override val uniqueId: UUID
        get() = player.uniqueId
    override val level: Int
        get() = playerLevelProvider.getOrDefault(uniqueId, 1)
    override val kizamiMap: KizamiMap
        get() = TODO("Not yet implemented")
    override val attributeMap: AttributeMap
        get() = TODO("Not yet implemented")
}

/**
 * The adapter for [PaperPlayer].
 */
object PaperPlayerAdapter : PlayerAdapter<PaperPlayer> {
    override fun adapt(player: PaperPlayer): Player {
        return PaperPlayer(player)
    }
}

/**
 * Adapts the [PaperPlayer] into [NekoPlayer][Player].
 */
fun PaperPlayer.asNeko(): Player {
    return PlayerAdapters.get<PaperPlayer>().adapt(this)
}
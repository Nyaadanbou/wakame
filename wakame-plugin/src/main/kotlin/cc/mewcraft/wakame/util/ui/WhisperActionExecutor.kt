package cc.mewcraft.wakame.util.ui

import me.lucko.helper.cooldown.Cooldown
import me.lucko.helper.metadata.Metadata
import me.lucko.helper.metadata.MetadataKey
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

fun whisper(
    player: Player, gap: Long, key: MetadataKey<Cooldown>, action: Player.() -> Unit,
) {
    WhisperActionExecutor.execute(player, key, gap, action)
}

/**
 * 执行“悄悄话”动作.
 */
object WhisperActionExecutor {
    fun execute(player: Player, key: MetadataKey<Cooldown>, def: Long, action: Player.() -> Unit) {
        val cooldown = Metadata.provideForPlayer(player).getOrPut(key) { Cooldown.of(def, TimeUnit.SECONDS) }
        if (cooldown.test()) {
            action(player)
        }
    }
}
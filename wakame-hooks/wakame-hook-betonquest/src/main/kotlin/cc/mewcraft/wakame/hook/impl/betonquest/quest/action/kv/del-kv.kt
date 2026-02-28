package cc.mewcraft.wakame.hook.impl.betonquest.quest.action.kv

import cc.mewcraft.extracontexts.api.KeyValueStoreManager
import org.betonquest.betonquest.api.instruction.Argument
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.action.OnlineAction
import org.betonquest.betonquest.api.quest.action.OnlineActionAdapter
import org.betonquest.betonquest.api.quest.action.PlayerAction
import org.betonquest.betonquest.api.quest.action.PlayerActionFactory
import kotlin.jvm.optionals.getOrNull

/**
 * 删除玩家的 kv.
 *
 * 支持三种模式:
 * - 无参数: 删除玩家的所有 kv
 * - 一个参数: 删除玩家指定的 key
 * - 两个参数: 删除玩家前缀匹配的所有 key (第二个参数为前缀值)
 */
class DeleteKeyAction(
    private val key: Argument<String>?,
    private val prefix: Argument<String>?,
    private val logger: BetonQuestLogger,
) : OnlineAction {

    override fun execute(profile: OnlineProfile) {
        val playerId = profile.playerUUID
        val playerName = profile.player.name

        when {
            // 删除前缀匹配的 key
            prefix != null -> {
                val prefixValue = prefix.getValue(profile)
                KeyValueStoreManager.deleteWithPrefix(playerId, prefixValue)
                logger.info("Deleted all keys with prefix '$prefixValue' for player $playerName")
            }

            // 删除指定的 key
            key != null -> {
                val keyValue = key.getValue(profile)
                KeyValueStoreManager.delete(playerId, keyValue)
                logger.info("Deleted key '$keyValue' for player $playerName")
            }

            // 删除所有 key
            else -> {
                KeyValueStoreManager.delete(playerId)
                logger.info("Deleted all keys for player $playerName")
            }
        }
    }
}

class DeleteKeyActionFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerActionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerAction {
        val key = instruction.string().get("key").getOrNull()
        val prefix = instruction.string().get("prefix").getOrNull()

        // 验证参数组合
        if (key != null && prefix != null) {
            throw IllegalArgumentException("Cannot specify both 'key' and 'prefix' parameters")
        }

        val logger = loggerFactory.create(DeleteKeyAction::class.java)
        val onlineAction = DeleteKeyAction(key, prefix, logger)
        val adapter = OnlineActionAdapter(onlineAction)
        return adapter
    }
}


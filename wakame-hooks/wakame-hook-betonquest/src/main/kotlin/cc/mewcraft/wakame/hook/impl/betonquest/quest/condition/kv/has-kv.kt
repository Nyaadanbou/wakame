package cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.kv

import cc.mewcraft.extracontexts.api.KeyValueStoreManager
import org.betonquest.betonquest.api.instruction.Argument
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.condition.PlayerCondition
import org.betonquest.betonquest.api.quest.condition.PlayerConditionFactory
import org.betonquest.betonquest.api.quest.condition.online.OnlineCondition
import org.betonquest.betonquest.api.quest.condition.online.OnlineConditionAdapter
import kotlin.jvm.optionals.getOrNull

/**
 * 判断玩家是否拥有指定的 kv 键值对.
 *
 * 支持两种模式:
 * - 只指定 key: 检查 key 是否存在，忽略 value
 * - 同时指定 key 和 value: 检查 key 是否存在且 value 匹配
 */
class HasKeyValueCondition(
    private val key: Argument<String>,
    private val value: Argument<String>?,
    private val logger: BetonQuestLogger,
) : OnlineCondition {

    override fun check(profile: OnlineProfile): Boolean {
        val keyValue = key.getValue(profile)

        return when {
            // 同时检查 key 和 value
            value != null -> {
                val valueValue = value.getValue(profile)
                val storedValue = KeyValueStoreManager.get(profile.playerUUID, keyValue)
                val matches = storedValue == valueValue
                logger.debug("Checking if player ${profile.player.name} has kv '$keyValue'='$valueValue': $matches")
                matches
            }

            // 只检查 key 是否存在
            else -> {
                val exists = KeyValueStoreManager.exists(profile.playerUUID, keyValue)
                logger.debug("Checking if player ${profile.player.name} has key '$keyValue': $exists")
                exists
            }
        }
    }
}

class HasKeyValueFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerConditionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerCondition {
        val key = instruction.string().get()
        val value = instruction.string().get("value").getOrNull()
        val logger = loggerFactory.create(HasKeyValueCondition::class.java)
        val condition = HasKeyValueCondition(key, value, logger)
        val questPackage = instruction.getPackage()
        return OnlineConditionAdapter(condition, logger, questPackage)
    }
}


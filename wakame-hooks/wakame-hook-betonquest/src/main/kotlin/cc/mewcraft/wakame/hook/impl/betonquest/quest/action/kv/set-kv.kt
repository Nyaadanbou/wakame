package cc.mewcraft.wakame.hook.impl.betonquest.quest.action.kv

import cc.mewcraft.extracontexts.api.KeyValueStoreManager
import org.betonquest.betonquest.api.instruction.Argument
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.action.OnlineAction
import org.betonquest.betonquest.api.quest.action.PlayerAction
import org.betonquest.betonquest.api.quest.action.PlayerActionFactory
import org.betonquest.betonquest.api.quest.action.online.OnlineActionAdapter

/**
 * 设置玩家的 kv 键值对.
 */
class SetKeyValueAction(
    private val key: Argument<String>,
    private val value: Argument<String>,
    private val logger: BetonQuestLogger,
) : OnlineAction {

    override fun execute(profile: OnlineProfile) {
        val keyValue = key.getValue(profile)
        val valueValue = value.getValue(profile)
        KeyValueStoreManager.set(profile.playerUUID, keyValue, valueValue)
        logger.info("Set key '$keyValue' to '$valueValue' for player ${profile.player.name}")
    }
}

/**
 * @param loggerFactory the logger factory to create a logger for the actions
 */
class SetKeyValueActionFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerActionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerAction {
        val key = instruction.string().get()
        val value = instruction.string().get()
        val logger = loggerFactory.create(SetKeyValueAction::class.java)
        val onlineAction = SetKeyValueAction(key, value, logger)
        val questPackage = instruction.getPackage()
        val adapter = OnlineActionAdapter(onlineAction, logger, questPackage)
        return adapter
    }
}

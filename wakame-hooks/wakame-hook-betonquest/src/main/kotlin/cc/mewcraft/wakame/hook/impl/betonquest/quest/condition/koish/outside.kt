package cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.koish

import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.PrimaryServerThreadData
import org.betonquest.betonquest.api.quest.condition.PlayerCondition
import org.betonquest.betonquest.api.quest.condition.PlayerConditionFactory
import org.betonquest.betonquest.api.quest.condition.online.OnlineCondition
import org.betonquest.betonquest.api.quest.condition.online.OnlineConditionAdapter
import org.betonquest.betonquest.api.quest.condition.thread.PrimaryServerThreadPlayerCondition

/**
 * 检查玩家的头顶是否是天空 (头顶往上无限延伸只有空气方块).
 */
class Outside(
    private val logger: BetonQuestLogger,
) : OnlineCondition {

    override fun check(profile: OnlineProfile): Boolean {
        val player = profile.player
        val world = player.world

        val headBlock = player.eyeLocation.block
        val x = headBlock.x
        val z = headBlock.z
        val headY = headBlock.y

        val highestNonAirY = world.getHighestBlockYAt(x, z)

        // world.getHighestBlockYAt 返回该列中最高的非空气方块的 Y.
        // 若最高非空气方块在玩家头顶以下或正好是头顶所在位置,
        // 则说明头顶及其以上到世界高度上限之间全为空气,
        // 视为“在室外/露天”.
        return highestNonAirY <= headY
    }
}

/**
 * [Outside] 的工厂类.
 */
class OutsideFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
    private val data: PrimaryServerThreadData,
) : PlayerConditionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerCondition {
        val logger = loggerFactory.create(Outside::class.java)
        val condition = Outside(logger)
        val questPackage = instruction.getPackage()
        val eventAdapter = OnlineConditionAdapter(condition, logger, questPackage)
        return PrimaryServerThreadPlayerCondition(eventAdapter, data)
    }
}
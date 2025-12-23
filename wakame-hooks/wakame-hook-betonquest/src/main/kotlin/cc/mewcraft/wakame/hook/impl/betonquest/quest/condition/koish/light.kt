package cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.koish

import cc.mewcraft.wakame.hook.impl.betonquest.util.ComparisonOp
import cc.mewcraft.wakame.hook.impl.betonquest.util.FriendlyEnumParser
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.instruction.variable.Variable
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.PrimaryServerThreadData
import org.betonquest.betonquest.api.quest.condition.PlayerCondition
import org.betonquest.betonquest.api.quest.condition.PlayerConditionFactory
import org.betonquest.betonquest.api.quest.condition.online.OnlineCondition
import org.betonquest.betonquest.api.quest.condition.online.OnlineConditionAdapter
import org.betonquest.betonquest.api.quest.condition.thread.PrimaryServerThreadPlayerCondition

/**
 * 检测玩家所在位置的光照强度是否满足条件.
 *
 * 语法示例:
 * - light block >= 7
 * - light sky < 5
 */
class Light(
    private val type: Variable<Type>,
    private val operation: Variable<ComparisonOp>,
    private val value: Variable<Number>,
) : OnlineCondition {

    override fun check(profile: OnlineProfile): Boolean {
        val player = profile.player
        val location = player.location ?: return false
        val block = location.block

        val typeValue = type.getValue(profile)
        val op = operation.getValue(profile)
        val targetNumber = value.getValue(profile)

        val lightLevel = when (typeValue) {
            Type.MIXED -> block.lightLevel
            Type.BLOCK -> block.lightFromBlocks
            Type.SKY -> block.lightFromSky
        }.toDouble()


        val right = targetNumber.toDouble()
        val left = lightLevel

        return when (op) {
            ComparisonOp.LESS_THAN -> left < right
            ComparisonOp.LESS_THAN_OR_EQUAL -> left <= right
            ComparisonOp.EQUAL -> left == right
            ComparisonOp.GREATER_THAN_OR_EQUAL -> left >= right
            ComparisonOp.GREATER_THAN -> left > right
        }
    }

    enum class Type {
        /**
         * 综合光照强度.
         */
        MIXED,

        /**
         * 方块光照强度.
         */
        BLOCK,

        /**
         * 天空光照强度.
         */
        SKY,
    }
}

/**
 * [Light] 的工厂类.
 */
class LightFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
    private val data: PrimaryServerThreadData,
) : PlayerConditionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerCondition {
        val type = instruction.get(instruction.parsers.forEnum(Light.Type::class.java))
        val operation = instruction.get(FriendlyEnumParser<ComparisonOp>())
        val value = instruction.get(instruction.parsers.number())
        val logger = loggerFactory.create(Light::class.java)
        val condition = Light(type, operation, value)
        val questPackage = instruction.getPackage()
        val conditionAdapter = OnlineConditionAdapter(condition, logger, questPackage)
        return PrimaryServerThreadPlayerCondition(conditionAdapter, data)
    }
}
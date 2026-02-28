package cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.koish

import cc.mewcraft.wakame.hook.impl.betonquest.util.ComparisonOp
import cc.mewcraft.wakame.hook.impl.betonquest.util.FriendlyEnumParser
import org.betonquest.betonquest.api.instruction.Argument
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.condition.OnlineCondition
import org.betonquest.betonquest.api.quest.condition.OnlineConditionAdapter
import org.betonquest.betonquest.api.quest.condition.PlayerCondition
import org.betonquest.betonquest.api.quest.condition.PlayerConditionFactory

/**
 * 检测玩家所在位置的光照强度是否满足条件.
 *
 * 语法示例:
 * - light block >= 7
 * - light sky < 5
 */
class Light(
    private val type: Argument<Type>,
    private val operation: Argument<ComparisonOp>,
    private val value: Argument<Number>,
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
) : PlayerConditionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerCondition {
        val type = instruction.enumeration(Light.Type::class.java).get()
        val operation = instruction.parse(FriendlyEnumParser<ComparisonOp>()).get()
        val value = instruction.number().get()
        val condition = Light(type, operation, value)
        val adapter = OnlineConditionAdapter(condition)
        return adapter
    }
}
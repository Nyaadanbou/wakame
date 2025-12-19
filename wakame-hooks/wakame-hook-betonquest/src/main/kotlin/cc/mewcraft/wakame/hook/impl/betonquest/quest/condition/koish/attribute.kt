package cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.koish

import cc.mewcraft.wakame.entity.attribute.Attribute
import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.hook.impl.betonquest.util.ComparisonOp
import cc.mewcraft.wakame.hook.impl.betonquest.util.FriendlyEnumParser
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.instruction.argument.Argument
import org.betonquest.betonquest.api.instruction.variable.Variable
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.PrimaryServerThreadData
import org.betonquest.betonquest.api.quest.QuestException
import org.betonquest.betonquest.api.quest.condition.PlayerCondition
import org.betonquest.betonquest.api.quest.condition.PlayerConditionFactory
import org.betonquest.betonquest.api.quest.condition.online.OnlineCondition
import org.betonquest.betonquest.api.quest.condition.online.OnlineConditionAdapter
import org.betonquest.betonquest.api.quest.condition.thread.PrimaryServerThreadPlayerCondition

/**
 * 检查玩家的特定 [cc.mewcraft.wakame.entity.attribute.Attribute] 是否满足条件.
 */
class KoishAttribute(
    private val attribute: Variable<Attribute>,
    private val operation: Variable<ComparisonOp>,
    private val value: Variable<Number>,
    private val logger: BetonQuestLogger,
) : OnlineCondition {

    override fun check(profile: OnlineProfile): Boolean {
        val player = profile.player
        val attributeContainer = player.attributeContainer

        val attr = attribute.getValue(profile)

        val attributeValue = try {
            attributeContainer.getValue(attr)
        } catch (_: Exception) {
            logger.warn("Failed to get attribute value for ${attr.id} on player ${player.name}")
            return false
        }

        val op = operation.getValue(profile)
        val targetNumber = value.getValue(profile)

        val left = attributeValue
        val right = targetNumber.toDouble()

        return when (op) {
            ComparisonOp.LESS_THAN -> left < right
            ComparisonOp.LESS_THAN_OR_EQUAL -> left <= right
            ComparisonOp.EQUAL -> left == right
            ComparisonOp.GREATER_THAN_OR_EQUAL -> left >= right
            ComparisonOp.GREATER_THAN -> left > right
        }
    }
}

class AttributeFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
    private val data: PrimaryServerThreadData,
) : PlayerConditionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerCondition {
        val attribute = instruction.get { t -> Attributes.get(t) ?: throw QuestException("Can't find attribute with id: $t") }
        val operation = instruction.get(FriendlyEnumParser<ComparisonOp>())
        val value = instruction.get(Argument.NUMBER)
        val logger = loggerFactory.create(KoishAttribute::class.java)
        val condition = KoishAttribute(attribute, operation, value, logger)
        val questPackage = instruction.getPackage()
        val eventAdapter = OnlineConditionAdapter(condition, logger, questPackage)
        return PrimaryServerThreadPlayerCondition(eventAdapter, data)
    }
}
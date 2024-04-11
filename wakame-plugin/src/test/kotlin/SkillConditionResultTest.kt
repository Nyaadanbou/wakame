import cc.mewcraft.wakame.skill.SkillContext
import cc.mewcraft.wakame.skill.condition.Condition
import cc.mewcraft.wakame.skill.condition.SkillCondition
import cc.mewcraft.wakame.skill.condition.SkillConditionResult
import me.lucko.helper.text3.mini
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.slf4j.Logger

@Suppress("UnstableApiUsage")
class SkillConditionResultTest : KoinTest {
    private val player: Audience = DummyPlayer

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(testEnvironment())
            }
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            stopKoin()
        }
    }

    private fun testResult(result: SkillConditionResult): Boolean {
        if (result.test()) {
            result.cost()
            return true
        } else {
            result.notifyFailure(player)
            return false
        }
    }

    @Test
    fun `test builder`() {
        val result = SkillConditionResult(
            skillConditions = listOf(
                AlwaysTrueDummySkillCondition
            ),
        )
        val context = AnySkillContext("a")

        result.builder()
            .typedConditions<AlwaysTrueDummySkillCondition> { test(context) }
            .next()
            .withPriority(Condition.Priority.NORMAL)
            .withFailureNotification { it.sendMessage("AWA".mini) }
            .addConditionSideEffect<AlwaysTrueDummySkillCondition> { it.cost(context) }
            .build()

        assert(testResult(result))
    }

    @Test
    fun `test priority`() {
        val result = SkillConditionResult(
            skillConditions = listOf(
                AlwaysTrueDummySkillCondition,
                AlwaysFalseDummySkillCondition
            ),
        )
        val context = AnySkillContext("a")

        result.builder()
            .typedConditions(AlwaysTrueDummySkillCondition::class) { test(context) }
            .typedConditions(AlwaysFalseDummySkillCondition::class) { test(context) }
            .next()
            .withPriority(Condition.Priority.HIGH)
            .withFailureNotification { it.sendMessage("AWA1".mini) }
            .addConditionSideEffect<AlwaysTrueDummySkillCondition> { it.cost(context) }
            .buildAndNext()
            .createCondition { AlwaysFalseDummySkillCondition.test(context) }
            .next()
            .withPriority(Condition.Priority.LOW)
            .withFailureNotification { it.sendMessage("AWA2".mini) }
            .addConditionSideEffect(AlwaysFalseDummySkillCondition::class) { it.cost(context) }
            .build()

        assert(!testResult(result))
    }

    private object DummyPlayer : Audience, KoinTest {
        private val logger: Logger by inject()

        @Deprecated("Deprecated in Java")
        override fun sendMessage(source: Identity, message: Component, type: MessageType) {
            logger.info("send message $message")
        }
    }

    private object AlwaysTrueDummySkillCondition : SkillCondition<AnySkillContext>, KoinTest {
        private val logger: Logger by inject()
        override fun test(context: AnySkillContext): Boolean {
            logger.info("dummy condition 1 test ${context.any}")
            return true
        }

        override fun cost(context: AnySkillContext) {
            logger.info("dummy condition 1 cost ${context.any}")
        }
    }

    private object AlwaysFalseDummySkillCondition : SkillCondition<AnySkillContext>, KoinTest {
        private val logger: Logger by inject()
        override fun test(context: AnySkillContext): Boolean {
            logger.info("dummy condition 2 test ${context.any}")
            return false
        }

        override fun cost(context: AnySkillContext) {
            logger.info("dummy condition 2 cost ${context.any}")
        }
    }

    private data class AnySkillContext(val any: Any) : SkillContext
}
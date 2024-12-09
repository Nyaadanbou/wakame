import cc.mewcraft.wakame.skill2.condition.ConditionPhase
import cc.mewcraft.wakame.skill2.condition.SkillCondition
import cc.mewcraft.wakame.skill2.condition.SkillConditionGroup
import cc.mewcraft.wakame.skill2.condition.SkillConditionGroupImpl
import cc.mewcraft.wakame.skill2.condition.SkillConditionSession
import cc.mewcraft.wakame.skill2.context.SkillContext
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest

class SkillConditionResultTest : KoinTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(testEnv())
            }
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            stopKoin()
        }
    }

    private fun testGroup(group: SkillConditionGroup, context: SkillContext): SkillConditionSession {
        val session = group.newSession(ConditionPhase.CAST_POINT,)
        if (session.isSuccess) {
            session.onSuccess()
        } else {
            session.onFailure()
        }

        return session
    }

    @Test
    fun `test skill condition result`() {
        val mockContext = mockk<SkillContext>()
        val mockCondition = mockk<SkillCondition>(relaxed = true)
        val session = mockk<SkillConditionSession>(relaxed = true)

        every { session.isSuccess } returns true

        val skillConditions = SkillConditionGroupImpl(
            mapOf(ConditionPhase.CAST_POINT to listOf(mockCondition))
        )

        every { mockCondition.newSession() } returns session

        val result = testGroup(skillConditions, mockContext)
        assert(result.isSuccess)
        verify(exactly = 1) { mockCondition.newSession() }
        verify(exactly = 1) { session.onSuccess() }
        verify(exactly = 0) { session.onFailure() }
    }

    @Test
    fun `test skill condition failure`() {
        val mockContext = mockk<SkillContext>()
        val mockCondition = mockk<SkillCondition>(relaxed = true)
        val session = mockk<SkillConditionSession>(relaxed = true)

        every { session.isSuccess } returns false

        every { mockCondition.newSession() } returns session

        val skillConditions = SkillConditionGroupImpl(
            mapOf(ConditionPhase.CAST_POINT to listOf(mockCondition))
        )

        val result = testGroup(skillConditions, mockContext)
        assert(!result.isSuccess)
        verify(exactly = 1) { mockCondition.newSession() }
        verify(exactly = 0) { session.onSuccess() }
        verify(exactly = 1) { session.onFailure() }
    }
}
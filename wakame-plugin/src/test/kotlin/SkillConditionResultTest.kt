import cc.mewcraft.wakame.ecs.external.ComponentMap
import cc.mewcraft.wakame.skill2.condition.ConditionPhase
import cc.mewcraft.wakame.skill2.condition.SkillCondition
import cc.mewcraft.wakame.skill2.condition.SkillConditionGroup
import cc.mewcraft.wakame.skill2.condition.SkillConditionGroupImpl
import cc.mewcraft.wakame.skill2.condition.SkillConditionSession
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

    private fun testGroup(group: SkillConditionGroup, componentMap: ComponentMap): SkillConditionSession {
        val session = group.newSession(ConditionPhase.CAST_POINT, componentMap)
        if (session.isSuccess) {
            session.onSuccess(componentMap)
        } else {
            session.onFailure(componentMap)
        }

        return session
    }

    @Test
    fun `test skill condition result`() {
        val mockComponentMap = mockk<ComponentMap>()
        val mockCondition = mockk<SkillCondition>(relaxed = true)
        val session = mockk<SkillConditionSession>(relaxed = true)

        every { session.isSuccess } returns true

        val skillConditions = SkillConditionGroupImpl(
            mapOf(ConditionPhase.CAST_POINT to listOf(mockCondition))
        )

        every { mockCondition.newSession(mockComponentMap) } returns session

        val result = testGroup(skillConditions, mockComponentMap)
        assert(result.isSuccess)
        verify(exactly = 1) { mockCondition.newSession(mockComponentMap) }
        verify(exactly = 1) { session.onSuccess(mockComponentMap) }
        verify(exactly = 0) { session.onFailure(mockComponentMap) }
    }

    @Test
    fun `test skill condition failure`() {
        val mockComponentMap = mockk<ComponentMap>()
        val mockCondition = mockk<SkillCondition>(relaxed = true)
        val session = mockk<SkillConditionSession>(relaxed = true)

        every { session.isSuccess } returns false

        every { mockCondition.newSession(mockComponentMap) } returns session

        val skillConditions = SkillConditionGroupImpl(
            mapOf(ConditionPhase.CAST_POINT to listOf(mockCondition))
        )

        val result = testGroup(skillConditions, mockComponentMap)
        assert(!result.isSuccess)
        verify(exactly = 1) { mockCondition.newSession(mockComponentMap) }
        verify(exactly = 0) { session.onSuccess(mockComponentMap) }
        verify(exactly = 1) { session.onFailure(mockComponentMap) }
    }
}
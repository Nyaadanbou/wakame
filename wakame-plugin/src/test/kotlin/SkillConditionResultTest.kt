import cc.mewcraft.wakame.skill.condition.SkillCondition
import cc.mewcraft.wakame.skill.condition.SkillConditionGroup
import cc.mewcraft.wakame.skill.condition.SkillConditionGroupImpl
import cc.mewcraft.wakame.skill.context.SkillCastContext
import io.mockk.*
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
                modules(testEnvironment())
            }
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            stopKoin()
        }
    }

    private fun testGroup(group: SkillConditionGroup, context: SkillCastContext): Boolean {
        if (group.newSession(context)) {
            group.cost(context)
            return true
        } else {
            group.notifyFailure(context)
            return false
        }
    }

    @Test
    fun `test skill condition result`() {
        val mockContext = mockk<SkillCastContext>()
        val mockCondition = mockk<SkillCondition>(relaxed = true)
        val skillConditions = SkillConditionGroupImpl(
            listOf(mockCondition)
        )

        every { mockCondition.newSession(mockContext) } returns true

        val result = testGroup(skillConditions, mockContext)
        assert(result)
        verify(exactly = 1) { mockCondition.newSession(mockContext) }
        verify(exactly = 1) { mockCondition.cost(mockContext) }
        verify(exactly = 0) { mockCondition.onFailure(mockContext) }
    }

    @Test
    fun `test skill condition failure`() {
        val mockContext = mockk<SkillCastContext>()
        val mockCondition = mockk<SkillCondition>(relaxed = true)

        every { mockCondition.newSession(mockContext) } returns false

        val skillConditions = SkillConditionGroupImpl(
            listOf(mockCondition)
        )

        val result = testGroup(skillConditions, mockContext)
        assert(!result)
        verify(exactly = 1) { mockCondition.newSession(mockContext) }
        verify(exactly = 0) { mockCondition.cost(mockContext) }
        verify(exactly = 1) { mockCondition.onFailure(mockContext) }
    }

    @Test
    fun `test skill condition priority`() {
        val mockContext = mockk<SkillCastContext>()
        val mockCondition1 = mockk<SkillCondition>(relaxed = true)
        val mockCondition2 = mockk<SkillCondition>(relaxed = true)
        val mockCondition3 = mockk<SkillCondition>(relaxed = true)

        every { mockCondition1.newSession(mockContext) } returns false
        every { mockCondition1.compareTo(any()) } returns -1 // Lower priority

        every { mockCondition2.newSession(mockContext) } returns false
        every { mockCondition2.compareTo(any()) } returns 1 // Higher priority

        every { mockCondition3.newSession(mockContext) } returns true
        every { mockCondition3.compareTo(any()) } returns 1 // Higher priority

        val skillConditions = SkillConditionGroupImpl(
            listOf(mockCondition1, mockCondition2, mockCondition3)
        )

        val result = testGroup(skillConditions, mockContext)
        assert(!result)

        verify(exactly = 1) { mockCondition1.newSession(mockContext) }
        verify(exactly = 0) { mockCondition1.cost(mockContext) }
        verify(exactly = 0) { mockCondition1.onFailure(mockContext) }

        verify(exactly = 1) { mockCondition2.newSession(mockContext) }
        verify(exactly = 0) { mockCondition2.cost(mockContext) }
        verify(exactly = 1) { mockCondition2.onFailure(mockContext) }

        verify(exactly = 1) { mockCondition3.newSession(mockContext) }
        verify(exactly = 0) { mockCondition3.cost(mockContext) }
        verify(exactly = 0) { mockCondition3.onFailure(mockContext) }
    }
}
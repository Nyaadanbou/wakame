import cc.mewcraft.wakame.skill.condition.SkillCastContext
import cc.mewcraft.wakame.skill.condition.SkillCondition
import cc.mewcraft.wakame.skill.condition.SkillConditionGroup
import cc.mewcraft.wakame.skill.condition.SortedSkillConditionGroup
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest

class ConfiguredSkillConditionResultTest : KoinTest {
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
        if (group.test(context)) {
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
        val skillConditions = SortedSkillConditionGroup(
            listOf(mockCondition)
        )

        every { mockCondition.test(mockContext) } returns true

        val result = testGroup(skillConditions, mockContext)
        assert(result)
        verify(exactly = 1) { mockCondition.test(mockContext) }
        verify(exactly = 1) { mockCondition.cost(mockContext) }
        verify(exactly = 0) { mockCondition.notifyFailure(mockContext) }
    }

    @Test
    fun `test skill condition failure`() {
        val mockContext = mockk<SkillCastContext>()
        val mockCondition = mockk<SkillCondition>(relaxed = true)

        every { mockCondition.test(mockContext) } returns false

        val skillConditions = SortedSkillConditionGroup(
            listOf(mockCondition)
        )

        val result = testGroup(skillConditions, mockContext)
        assert(!result)
        verify(exactly = 1) { mockCondition.test(mockContext) }
        verify(exactly = 0) { mockCondition.cost(mockContext) }
        verify(exactly = 1) { mockCondition.notifyFailure(mockContext) }
    }

    @Test
    fun `test skill condition priority`() {
        val mockContext = mockk<SkillCastContext>()
        val mockCondition1 = mockk<SkillCondition>(relaxed = true)
        val mockCondition2 = mockk<SkillCondition>(relaxed = true)
        val mockCondition3 = mockk<SkillCondition>(relaxed = true)

        every { mockCondition1.test(mockContext) } returns false
        every { mockCondition1.priority } returns SkillCondition.Priority.LOWEST
        every { mockCondition2.test(mockContext) } returns false
        every { mockCondition2.priority } returns SkillCondition.Priority.HIGH
        every { mockCondition3.test(mockContext) } returns true
        every { mockCondition3.priority } returns SkillCondition.Priority.HIGHEST

        val skillConditions = SortedSkillConditionGroup(
            listOf(mockCondition1, mockCondition2, mockCondition3)
        )

        val result = testGroup(skillConditions, mockContext)
        assert(!result)

        verify(exactly = 1) { mockCondition1.test(mockContext) }
        verify(exactly = 0) { mockCondition1.cost(mockContext) }
        verify(exactly = 0) { mockCondition1.notifyFailure(mockContext) }
        verify(exactly = 1) { mockCondition2.test(mockContext) }
        verify(exactly = 0) { mockCondition2.cost(mockContext) }
        verify(exactly = 1) { mockCondition2.notifyFailure(mockContext) }
        verify(exactly = 1) { mockCondition3.test(mockContext) }
        verify(exactly = 0) { mockCondition3.cost(mockContext) }
        verify(exactly = 0) { mockCondition3.notifyFailure(mockContext) }
    }
}
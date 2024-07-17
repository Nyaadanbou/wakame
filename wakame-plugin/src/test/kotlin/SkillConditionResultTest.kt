import cc.mewcraft.wakame.skill.condition.*
import cc.mewcraft.wakame.skill.condition.SkillConditionGroupImpl
import cc.mewcraft.wakame.skill.context.SkillContext
import com.google.common.collect.ImmutableMultimap
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
        val session = group.newSession(ConditionTime.BEFORE_CAST, context)
        if (session.isSuccess) {
            session.onSuccess(context)
        } else {
            session.onFailure(context)
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
            ImmutableMultimap.of(ConditionTime.BEFORE_CAST, mockCondition)
        )

        every { mockCondition.newSession(mockContext) } returns session

        val result = testGroup(skillConditions, mockContext)
        assert(result.isSuccess)
        verify(exactly = 1) { mockCondition.newSession(mockContext) }
        verify(exactly = 1) { session.onSuccess(mockContext) }
        verify(exactly = 0) { session.onFailure(mockContext) }
    }

    @Test
    fun `test skill condition failure`() {
        val mockContext = mockk<SkillContext>()
        val mockCondition = mockk<SkillCondition>(relaxed = true)
        val session = mockk<SkillConditionSession>(relaxed = true)

        every { session.isSuccess } returns false

        every { mockCondition.newSession(mockContext) } returns session

        val skillConditions = SkillConditionGroupImpl(
            ImmutableMultimap.of(ConditionTime.BEFORE_CAST, mockCondition)
        )

        val result = testGroup(skillConditions, mockContext)
        assert(!result.isSuccess)
        verify(exactly = 1) { mockCondition.newSession(mockContext) }
        verify(exactly = 0) { session.onSuccess(mockContext) }
        verify(exactly = 1) { session.onFailure(mockContext) }
    }
}
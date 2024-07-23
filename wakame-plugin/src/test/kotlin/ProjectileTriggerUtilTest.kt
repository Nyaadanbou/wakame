import cc.mewcraft.wakame.skill.trigger.SequenceTrigger
import cc.mewcraft.wakame.skill.trigger.SingleTrigger
import org.junit.jupiter.api.Test

class ProjectileTriggerUtilTest {

    @Test
    fun `test generate combos`() {
        val sequences = SequenceTrigger.generate(listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK), 3)
        val verifyResult = listOf(
            SequenceTrigger.of(listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.LEFT_CLICK, SingleTrigger.LEFT_CLICK)),
            SequenceTrigger.of(listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK)),
            SequenceTrigger.of(listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK, SingleTrigger.LEFT_CLICK)),
            SequenceTrigger.of(listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK, SingleTrigger.RIGHT_CLICK)),
            SequenceTrigger.of(listOf(SingleTrigger.RIGHT_CLICK, SingleTrigger.LEFT_CLICK, SingleTrigger.LEFT_CLICK)),
            SequenceTrigger.of(listOf(SingleTrigger.RIGHT_CLICK, SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK)),
            SequenceTrigger.of(listOf(SingleTrigger.RIGHT_CLICK, SingleTrigger.RIGHT_CLICK, SingleTrigger.LEFT_CLICK)),
            SequenceTrigger.of(listOf(SingleTrigger.RIGHT_CLICK, SingleTrigger.RIGHT_CLICK, SingleTrigger.RIGHT_CLICK))
        )
        assert(sequences == verifyResult)
    }
}
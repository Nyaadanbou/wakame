import cc.mewcraft.wakame.skill.trigger.Trigger
import cc.mewcraft.wakame.skill.trigger.Trigger.LeftClick
import cc.mewcraft.wakame.skill.trigger.Trigger.RightClick
import cc.mewcraft.wakame.util.SkillTriggerUtil.generateCombinations
import org.junit.jupiter.api.Test

class TriggerUtilTest {

    @Test
    fun `test generate combos`() {
        val combos = listOf(LeftClick, RightClick).generateCombinations(3)
        val verifyResult = listOf(
            Trigger.Combo(listOf(LeftClick, LeftClick, LeftClick)),
            Trigger.Combo(listOf(LeftClick, LeftClick, RightClick)),
            Trigger.Combo(listOf(LeftClick, RightClick, LeftClick)),
            Trigger.Combo(listOf(LeftClick, RightClick, RightClick)),
            Trigger.Combo(listOf(RightClick, LeftClick, LeftClick)),
            Trigger.Combo(listOf(RightClick, LeftClick, RightClick)),
            Trigger.Combo(listOf(RightClick, RightClick, LeftClick)),
            Trigger.Combo(listOf(RightClick, RightClick, RightClick))
        )
        assert(combos == verifyResult)
    }
}
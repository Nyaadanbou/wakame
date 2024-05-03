import cc.mewcraft.wakame.skill.SkillTrigger
import cc.mewcraft.wakame.skill.SkillTrigger.LeftClick
import cc.mewcraft.wakame.skill.SkillTrigger.RightClick
import cc.mewcraft.wakame.util.SkillTriggerUtil.generateCombinations
import org.junit.jupiter.api.Test

class SkillTriggerUtilTest {

    @Test
    fun `test generate combos`() {
        val combos = listOf(LeftClick, RightClick).generateCombinations(3)
        val verifyResult = listOf(
            SkillTrigger.Combo(listOf(LeftClick, LeftClick, LeftClick)),
            SkillTrigger.Combo(listOf(LeftClick, LeftClick, RightClick)),
            SkillTrigger.Combo(listOf(LeftClick, RightClick, LeftClick)),
            SkillTrigger.Combo(listOf(LeftClick, RightClick, RightClick)),
            SkillTrigger.Combo(listOf(RightClick, LeftClick, LeftClick)),
            SkillTrigger.Combo(listOf(RightClick, LeftClick, RightClick)),
            SkillTrigger.Combo(listOf(RightClick, RightClick, LeftClick)),
            SkillTrigger.Combo(listOf(RightClick, RightClick, RightClick))
        )
        assert(combos == verifyResult)
    }
}
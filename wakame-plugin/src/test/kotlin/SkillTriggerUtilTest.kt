import cc.mewcraft.wakame.registry.SkillRegistry.COMBO_TRIGGERS
import cc.mewcraft.wakame.skill.SkillTrigger
import cc.mewcraft.wakame.util.SkillTriggerUtil.generateCombinations
import org.junit.jupiter.api.Test

class SkillTriggerUtilTest {

    @Test
    fun `test generate combos`() {
        val combos = COMBO_TRIGGERS.generateCombinations(3)
        val verifyResult = listOf(
            SkillTrigger.Combo(listOf(SkillTrigger.LeftClick, SkillTrigger.LeftClick, SkillTrigger.LeftClick)),
            SkillTrigger.Combo(listOf(SkillTrigger.LeftClick, SkillTrigger.LeftClick, SkillTrigger.RightClick)),
            SkillTrigger.Combo(listOf(SkillTrigger.LeftClick, SkillTrigger.RightClick, SkillTrigger.LeftClick)),
            SkillTrigger.Combo(listOf(SkillTrigger.LeftClick, SkillTrigger.RightClick, SkillTrigger.RightClick)),
            SkillTrigger.Combo(listOf(SkillTrigger.RightClick, SkillTrigger.LeftClick, SkillTrigger.LeftClick)),
            SkillTrigger.Combo(listOf(SkillTrigger.RightClick, SkillTrigger.LeftClick, SkillTrigger.RightClick)),
            SkillTrigger.Combo(listOf(SkillTrigger.RightClick, SkillTrigger.RightClick, SkillTrigger.LeftClick)),
            SkillTrigger.Combo(listOf(SkillTrigger.RightClick, SkillTrigger.RightClick, SkillTrigger.RightClick))
        )
        assert(combos == verifyResult)
    }
}
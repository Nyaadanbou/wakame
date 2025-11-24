package cc.mewcraft.wakame.integration.skill

import org.bukkit.entity.Player

interface SkillIntegration {

    /**
     * 施放 [id] 对应的技能.
     *
     * @param player 施放技能的玩家
     * @param id 技能的唯一标识, 格式取决于实现
     */
    fun castBlockSkill(player: Player, id: String)

    /**
     * 施放 [line] 对应的技能.
     *
     * @param player 施放技能的玩家
     * @param line 技能的内联配置, 格式取决于实现
     */
    fun castInlineSkill(player: Player, line: String)

    /**
     * 检查玩家 [player] 的技能 [id] 是否处于冷却中.
     *
     * @param player 需要检查的玩家
     * @param id 需要检查的技能的唯一标识, 格式取决于实现
     * @return 如果技能处于冷却中则返回 true, 否则返回 false
     */
    fun isCooldown(player: Player, id: String): Boolean

    /**
     * This companion object holds current [SkillIntegration] implementation.
     */
    companion object : SkillIntegration {

        private val NO_OP: SkillIntegration = object : SkillIntegration {
            override fun castBlockSkill(player: Player, id: String) = Unit
            override fun castInlineSkill(player: Player, line: String) = Unit
            override fun isCooldown(player: Player, id: String): Boolean = false
        }

        private var implementation: SkillIntegration = NO_OP

        fun setImplementation(impl: SkillIntegration) {
            implementation = impl
        }

        override fun castBlockSkill(player: Player, id: String) {
            return implementation.castBlockSkill(player, id)
        }

        override fun castInlineSkill(player: Player, line: String) {
            return implementation.castInlineSkill(player, line)
        }

        override fun isCooldown(player: Player, id: String): Boolean {
            return implementation.isCooldown(player, id)
        }
    }
}
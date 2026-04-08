package cc.mewcraft.wakame.integration.skill

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.item.property.impl.Castable
import cc.mewcraft.wakame.util.decorate
import org.bukkit.entity.Player
import org.slf4j.Logger

interface SkillIntegration {

    /**
     * 施放 [id] 对应的技能.
     *
     * @param player 施放技能的玩家
     * @param id 技能的唯一标识, 格式取决于实现
     */
    fun castBlockSkill(player: Player, id: String, ctx: Castable? = null)

    /**
     * 施放 [line] 对应的技能.
     *
     * @param player 施放技能的玩家
     * @param line 技能的内联配置, 格式取决于实现
     */
    fun castInlineSkill(player: Player, line: String, ctx: Castable? = null)

    /**
     * 检查玩家 [player] 的技能 [id] 是否处于冷却中.
     *
     * @param player 需要检查的玩家
     * @param id 需要检查的技能的唯一标识, 格式取决于实现
     * @return 如果技能处于冷却中则返回 true, 否则返回 false
     */
    fun isCooldown(player: Player, id: String, ctx: Castable? = null): Boolean

    /**
     * This companion object holds current [SkillIntegration] implementation.
     */
    companion object : SkillIntegration {

        private val NO_OP: SkillIntegration = object : SkillIntegration {
            override fun castBlockSkill(player: Player, id: String, ctx: Castable?) = Unit
            override fun castInlineSkill(player: Player, line: String, ctx: Castable?) = Unit
            override fun isCooldown(player: Player, id: String, ctx: Castable?): Boolean = false
        }

        private var implementation: SkillIntegration = NO_OP
        private val logger: Logger = LOGGER.decorate(SkillIntegration::class)

        fun setImplementation(impl: SkillIntegration) {
            implementation = impl
        }

        override fun castBlockSkill(player: Player, id: String, ctx: Castable?) {
            logger.info("Casting block skill: $id")
            try {
                implementation.castBlockSkill(player, id, ctx)
            } catch (e: Exception) {
                logger.error("An error occurred while casting block skill: $id", e)
            }
        }

        override fun castInlineSkill(player: Player, line: String, ctx: Castable?) {
            logger.info("Casting inline skill: $line")
            try {
                implementation.castInlineSkill(player, line, ctx)
            } catch (e: Exception) {
                logger.error("An error occurred while casting inline skill: $line", e)
            }
        }

        override fun isCooldown(player: Player, id: String, ctx: Castable?): Boolean {
            logger.info("Checking skill cooldown: $id")
            try {
                return implementation.isCooldown(player, id, ctx)
            } catch (e: Exception) {
                logger.error("An error occurred while checking skill cooldown: $id", e)
                return true
            }
        }
    }
}
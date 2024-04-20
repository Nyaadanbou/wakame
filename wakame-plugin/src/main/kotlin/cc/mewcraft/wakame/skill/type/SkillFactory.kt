package cc.mewcraft.wakame.skill.type

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.skill.Skill
import net.kyori.adventure.key.Key

interface SkillFactory<T : Skill> {
    /**
     * Create a new instance of the skill base on a certain skill type
     */
    fun create(config: ConfigProvider, key: Key): T
}
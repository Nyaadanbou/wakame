package cc.mewcraft.wakame.skill.type

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.skill.Skill
import net.kyori.adventure.key.Key

interface SkillFactory<T : Skill> {
    fun create(config: ConfigProvider, key: Key): T
}
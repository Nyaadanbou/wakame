package cc.mewcraft.wakame.skill2.external

import cc.mewcraft.wakame.skill.Skill
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table

data class SkillComponentMap(
    val componentTable: Table<Skill, ExternalKey<*>, ExternalComponent<*>> = HashBasedTable.create(),
) {
    operator fun <T : ExternalComponent<*>> get(skill: Skill, externalKey: ExternalKey<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return componentTable.get(skill, externalKey) as? T
    }

    operator fun <T : ExternalComponent<*>> set(skill: Skill, externalKey: ExternalKey<T>, value: () -> T) {
        componentTable.put(skill, externalKey, value())
    }
}
package cc.mewcraft.wakame.skill.context

sealed interface SkillCastContext {
    fun <T : Any> set(key: SkillCastContextKey<T>, value: T)
    fun <T : Any> optional(key: SkillCastContextKey<T>): T?
    fun <T : Any> get(key: SkillCastContextKey<T>): T = optional(key) ?: throw IllegalArgumentException("No value for key: $key")
    fun <T : Any> has(key: SkillCastContextKey<T>): Boolean
}


package cc.mewcraft.wakame.skill.state

class IllegalSkillStateException(
    override val message: String? = null,
    override val cause: Throwable? = null
): Exception()
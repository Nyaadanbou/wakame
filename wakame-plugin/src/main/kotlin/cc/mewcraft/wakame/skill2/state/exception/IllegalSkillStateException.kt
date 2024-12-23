package cc.mewcraft.wakame.skill2.state.exception

class IllegalSkillStateException(
    override val message: String? = null,
    override val cause: Throwable? = null
): Exception()
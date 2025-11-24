package cc.mewcraft.wakame.integration.skill

interface SkillIntegration {

    /**
     * 根据 ID 查找对应的 [SkillWrapper.Block].
     *
     * @param id mechanic 的唯一标识, 格式取决实现
     * @return 对应的 [SkillWrapper.Block] 或 null
     */
    fun lookupBlockSkill(id: String): SkillWrapper.Block

    /**
     * 根据 ID 查找对应的 [SkillWrapper.Inline].
     *
     * @param line mechanic 的唯一标识, 格式取决实现
     * @return 对应的 [SkillWrapper.Inline] 或 null
     */
    fun lookupInlineSkill(line: String): SkillWrapper.Inline

    /**
     * This companion object holds current [SkillIntegration] implementation.
     */
    companion object : SkillIntegration {

        private val NO_OP: SkillIntegration = object : SkillIntegration {
            override fun lookupBlockSkill(id: String): SkillWrapper.Block = SkillWrapper.DEFAULT_BLOCK
            override fun lookupInlineSkill(line: String): SkillWrapper.Inline = SkillWrapper.DEFAULT_INLINE
        }

        private var implementation: SkillIntegration = NO_OP

        fun setImplementation(impl: SkillIntegration) {
            implementation = impl
        }

        override fun lookupBlockSkill(id: String): SkillWrapper.Block {
            return implementation.lookupBlockSkill(id)
        }

        override fun lookupInlineSkill(line: String): SkillWrapper.Inline {
            return implementation.lookupInlineSkill(line)
        }
    }
}
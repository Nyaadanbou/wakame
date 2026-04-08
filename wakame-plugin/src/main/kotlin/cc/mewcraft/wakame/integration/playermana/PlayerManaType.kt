package cc.mewcraft.wakame.integration.playermana


// 关于玩家魔法值的相关代码

enum class PlayerManaType {
    /**
     * 无限魔法值.
     */
    INFINITY,
    /**
     * 使用 AuraSkills 的魔法值系统.
     */
    AURA_SKILLS,
}
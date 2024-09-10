package cc.mewcraft.wakame.item.components.cells

import cc.mewcraft.wakame.item.components.cells.cores.*

/**
 * 核心的所有类型.
 */
object CoreType {
    /**
     * 虚拟核心.
     *
     * 用来引导系统在生成萌芽物品时, 不要把当前词条栏写入物品.
     * 因此这个核心永远不会出现在游戏内的物品上, 不然就是 BUG.
     */
    val VIRTUAL: CoreKind<VirtualCore> = SimpleVirtualCore

    /**
     * 空的核心.
     *
     * 当一个词条栏里没有核心时 (但词条栏本身存在), 里面实际上存放了一颗空核心.
     * 玩家概念上的“词条栏没有核心”, 就是技术概念上的 “词条栏里装的是空核心”.
     */
    val EMPTY: CoreKind<EmptyCore> = SimpleEmptyCore

    /**
     * 属性核心.
     *
     * 包含了 [cc.mewcraft.wakame.attribute.composite.ConstantCompositeAttribute].
     */
    val ATTRIBUTE: CoreKind<AttributeCore> = SimpleAttributeCore.Companion

    /**
     * 技能核心.
     *
     * 包含了 [cc.mewcraft.wakame.skill.ConfiguredSkill].
     */
    val SKILL: CoreKind<SkillCore> = SimpleSkillCore.Companion
}
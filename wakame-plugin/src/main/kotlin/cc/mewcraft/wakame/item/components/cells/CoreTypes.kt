package cc.mewcraft.wakame.item.components.cells

import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttribute
import cc.mewcraft.wakame.item.components.cells.cores.empty.CoreEmpty
import cc.mewcraft.wakame.item.components.cells.cores.noop.CoreNoop
import cc.mewcraft.wakame.item.components.cells.cores.skill.CoreSkill

/**
 * 核心的所有类型.
 */
object CoreTypes {
    /**
     * 无操作 (技术核心).
     *
     * 用来引导系统在生成物品时, 不要把当前词条栏写入物品.
     *
     * 因此这个核心永远不会出现在游戏内的物品上.
     */
    val NOP: CoreType<CoreNoop> = CoreNoop

    /**
     * 空 (技术核心).
     *
     * 当一个词条栏里没有核心时 (但词条栏本身存在), 里面实际上存放了一颗空核心.
     *
     * 玩家概念上的“词条栏没有核心”, 就是技术概念上的 “词条栏里装的是空核心”.
     */
    val EMPTY: CoreType<CoreEmpty> = CoreEmpty

    /**
     * 属性.
     */
    val ATTRIBUTE: CoreType<CoreAttribute> = CoreAttribute.Type

    /**
     * 技能.
     */
    val SKILL: CoreType<CoreSkill> = CoreSkill.Type
}
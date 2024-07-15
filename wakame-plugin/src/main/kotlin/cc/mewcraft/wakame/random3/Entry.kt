package cc.mewcraft.wakame.random3

import net.kyori.adventure.key.Key

/**
 * 代表配置文件中“样本列表”的一个节点.
 */
// FIXME 这东西好像没啥用
data class Entry<S, C : SelectionContext>(
    val key: Key,
    val sample: Sample<S, C>,
)
package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.item.property.impl.GenericCastableTrigger
import org.bukkit.entity.Player
import cc.mewcraft.wakame.item.property.impl.Castable as CastableProp

/**
 * 组合键序列的输入处理器.
 *
 * 用于将组合键序列的处理逻辑从 [Castable] 行为中解耦出来.
 * 实现方需注册到 [Castable.sequenceComboHandler].
 */
fun interface SequenceComboHandler {
    /**
     * 处理一次组合键输入.
     *
     * @param player 执行输入的玩家
     * @param castableMap 当前物品上的所有 castable 条目
     * @param input 玩家本次的输入 (左键或右键)
     */
    fun handleInput(player: Player, castableMap: Map<String, CastableProp>, input: GenericCastableTrigger)
}

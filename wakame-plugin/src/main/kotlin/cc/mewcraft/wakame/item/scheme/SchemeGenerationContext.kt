package cc.mewcraft.wakame.item.scheme

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.Core
import cc.mewcraft.wakame.item.Curse
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.random.BasicSelectionContext
import cc.mewcraft.wakame.random.SelectionContextWatcher
import cc.mewcraft.wakame.rarity.Rarity
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player

/**
 * 代表一个物品生成过程的上下文。
 *
 * 该对象的生命周期从 **物品生成开始** 到 **物品生成结束**。对于一个物品来说，物品的生成从头到尾应该只有一个
 * [SchemeGenerationContext] 实例会被创建和使用。
 *
 * 该接口所有函数和变量都是非线程安全！我们暂时不考虑并发。
 */
class SchemeGenerationContext(
    /**
     * 玩家的等级。
     */
    val playerLevel: Int,
    /**
     * 玩家对象。
     */
    val playerObject: Player? = null,
) : BasicSelectionContext() {
    /**
     * 已经生成的物品等级。
     */
    var itemLevel: Int by SelectionContextWatcher(1)

    /**
     * 已经生成的 [Rarity]. // TODO implement ObservableSet
     */
    val rarities: MutableSet<Rarity> by SelectionContextWatcher(ObjectArraySet(2)) // 一个物品有且只有一个稀有度，但我们依然用 Set

    /**
     * 已经生成的 [Element].
     */
    val elements: MutableSet<Element> by SelectionContextWatcher(ObjectArraySet(4)) // 一个物品可以有多个元素，因此用 Set

    /**
     * 已经生成的 [Kizami].
     */
    val kizamis: MutableSet<Kizami> by SelectionContextWatcher(ObjectArraySet(4)) // 一个物品可以有多个铭刻，因此用 Set

    /**
     * 已经生成的 [Core].
     */
    val coreKeys: MutableSet<Key> by SelectionContextWatcher(ObjectArraySet(8)) // 一个物品可以有多个词条（当然

    /**
     * 已经生成的 [Curse].
     */
    val curseKeys: MutableSet<Key> by SelectionContextWatcher(ObjectArraySet(8))
}

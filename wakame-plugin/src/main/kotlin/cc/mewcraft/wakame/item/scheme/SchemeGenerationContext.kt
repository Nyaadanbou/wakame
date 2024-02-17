package cc.mewcraft.wakame.item.scheme

import cc.mewcraft.wakame.crate.BinaryCrate
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.Core
import cc.mewcraft.wakame.item.Curse
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.random.BasicSelectionContext
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.util.WatchedPrimitive
import cc.mewcraft.wakame.util.WatchedSet
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
     * 盲盒对象。如果是盲盒触发的物品生成，就把盲盒传进来。
     */
    val crateObject: BinaryCrate? = null,
    /**
     * 玩家对象。如果是玩家触发的物品生成，就把玩家传进来。
     */
    val playerObject: Player? = null,
) : BasicSelectionContext() {
    /**
     * 已经生成的物品等级。
     */
    var itemLevel: Int by WatchedPrimitive(1)

    /**
     * 已经生成的 [Rarity].
     */
    val rarities: MutableCollection<Rarity> by WatchedSet(ObjectArraySet(2)) // 一个物品有且只有一个稀有度，但我们依然用 Set

    /**
     * 已经生成的 [Element].
     */
    val elements: MutableCollection<Element> by WatchedSet(ObjectArraySet(4)) // 一个物品可以有多个元素，因此用 Set

    /**
     * 已经生成的 [Kizami].
     */
    val kizamis: MutableCollection<Kizami> by WatchedSet(ObjectArraySet(4)) // 一个物品可以有多个铭刻，因此用 Set

    /**
     * 已经生成的 [Core].
     */
    val coreKeys: MutableCollection<Key> by WatchedSet(ObjectArraySet(8)) // 一个物品可以有多个词条（当然

    /**
     * 已经生成的 [Curse].
     */
    val curseKeys: MutableCollection<Key> by WatchedSet(ObjectArraySet(8))
}

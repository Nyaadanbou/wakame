package cc.mewcraft.wakame.entity.player

import cc.mewcraft.wakame.ecs.bridge.EComponent
import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.serverPlayer
import io.papermc.paper.adventure.PaperAdventure
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.minecraft.world.item.ItemCooldowns
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import kotlin.math.max

/**
 * 封装了一名玩家的物品冷却的状态.
 *
 * 当我们说一个冷却是“激活”状态时, 意味着玩家“无法使用”物品.
 * 当我们说一个冷却是“未激活”状态时, 意味着玩家“可以使用”物品.
 *
 * 注意: 本接口并不控制玩家能否使用物品, 这部分逻辑应该由其他代码实现.
 *
 * @see Player.itemCooldownContainer 访问该对象
 */
sealed interface ItemCooldownContainer : EComponent<ItemCooldownContainer> {

    companion object : EComponentType<ItemCooldownContainer>() {

        /**
         * 创建一个基于 NMS 实现的 [ItemCooldownContainer] 实例.
         * 调用该实现上的函数将影响 NMS 中 ServerPlayer 对象的状态!
         */
        fun minecraft(player: Player): ItemCooldownContainer {
            return MinecraftItemCooldownContainer(player)
        }

        /**
         * 创建一个独立的 [ItemCooldownContainer] 实例.
         */
        fun standalone(): ItemCooldownContainer {
            return StandaloneItemCooldownContainer()
        }

    }

    override fun type(): EComponentType<ItemCooldownContainer> = ItemCooldownContainer

    /**
     * “激活”指定冷却. 如果已存在将覆盖原有的冷却状态.
     */
    fun activate(id: Identifier, ticks: Int)

    /**
     * “激活”指定冷却. 如果已存在将覆盖原有的冷却状态.
     * 如果 [entry] 为 `null` 将使用默认的冷却时间.
     */
    fun activate(id: Identifier, entry: RegistryEntry<AttackSpeed>?)

    /**
     * 查询指定冷却是否为“激活”状态.
     */
    fun isActive(id: Identifier): Boolean

    /**
     * 返回指定冷却的剩余冷却时间与总冷却时间的比例 (0.0-1.0).
     * 值为 1.0 表示冷却刚刚激活, 值为 0.0 表示冷却已成未激活.
     */
    fun getRemainingRatio(id: Identifier): Float

    /**
     * 获取指定冷却的剩余时间, 单位: tick.
     * 即: 离冷却变为“未激活”状态的剩余时间.
     */
    fun getRemainingTicks(id: Identifier): Int

    /**
     * 重置指定冷却的状态, 使其直接变为“未激活”.
     */
    fun reset(id: Identifier)
}

// ------------
// 内部实现
// ------------

/**
 * 该实现将冷却状态全部委托给 [net.minecraft.server.level.ServerPlayer.cooldowns].
 * 所有函数的执行效果都具有副作用, 且会被客户端感知 (i.e., NMS会向客户端发送物品冷却的封包).
 */
private class MinecraftItemCooldownContainer(
    private val player: Player,
) : ItemCooldownContainer {

    private val delegate: ItemCooldowns
        get() = player.serverPlayer.cooldowns

    override fun activate(id: Identifier, ticks: Int) {
        val resLoc = PaperAdventure.asVanilla(id)
        delegate.addCooldown(resLoc, ticks, true)
    }

    override fun activate(id: Identifier, entry: RegistryEntry<AttackSpeed>?) {
        val entry2 = entry ?: BuiltInRegistries.ATTACK_SPEED.getDefaultEntry()
        activate(id, entry2.unwrap().cooldown)
    }

    override fun isActive(id: Identifier): Boolean {
        return getRemainingRatio(id) > 0f
    }

    override fun getRemainingRatio(id: Identifier): Float {
        val resLoc = PaperAdventure.asVanilla(id)
        val cooldownMap = delegate
        val cooldownInst = cooldownMap.cooldowns[resLoc]
        if (cooldownInst != null) {
            val f = cooldownInst.endTime - cooldownInst.startTime
            val g = cooldownInst.endTime - cooldownMap.tickCount
            return (g.toFloat() / f.toFloat()).coerceIn(0f, 1f)
        } else {
            return 0f
        }
    }

    override fun getRemainingTicks(id: Identifier): Int {
        val resLoc = PaperAdventure.asVanilla(id)
        val cooldownMap = delegate
        val cooldownInst = cooldownMap.cooldowns[resLoc]
        if (cooldownInst != null) {
            return max(cooldownInst.endTime - cooldownMap.tickCount, 0)
        } else {
            return 0
        }
    }

    override fun reset(id: Identifier) {
        val resLoc = PaperAdventure.asVanilla(id)
        delegate.removeCooldown(resLoc)
    }
}

/**
 * 该实现使用独立的数据结构来存储冷却状态, 不会影响任何其他对象的状态.
 */
private class StandaloneItemCooldownContainer : ItemCooldownContainer {
    /**
     * 记录了每个冷却变为“未激活”的时间戳.
     * - K: 冷却的索引
     * - V: 冷却变为"未激活"的游戏刻
     */
    private val inactiveTimestamps = Object2IntOpenHashMap<Identifier>()

    override fun activate(id: Identifier, ticks: Int) {
        inactiveTimestamps[id] = Bukkit.getServer().currentTick + ticks
    }

    override fun activate(id: Identifier, entry: RegistryEntry<AttackSpeed>?) {
        val entry2 = entry ?: BuiltInRegistries.ATTACK_SPEED.getDefaultEntry()
        activate(id, entry2.unwrap().cooldown)
    }

    override fun isActive(id: Identifier): Boolean {
        return inactiveTimestamps.getInt(id) > Bukkit.getServer().currentTick
    }

    override fun getRemainingRatio(id: Identifier): Float {
        val remainingTicks = getRemainingTicks(id)
        val totalCooldown = inactiveTimestamps.getInt(id) - Bukkit.getServer().currentTick + remainingTicks
        return if (totalCooldown <= 0) 0f else remainingTicks.toFloat() / totalCooldown
    }

    override fun getRemainingTicks(id: Identifier): Int {
        val diff = inactiveTimestamps.getInt(id) - Bukkit.getServer().currentTick
        return if (diff < 0) 0 else diff
    }

    override fun reset(id: Identifier) {
        inactiveTimestamps.put(id, 0)
    }
}

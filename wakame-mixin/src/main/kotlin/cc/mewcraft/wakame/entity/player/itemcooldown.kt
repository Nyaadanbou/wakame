package cc.mewcraft.wakame.entity.player

import cc.mewcraft.wakame.ecs.bridge.EComponent
import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.serverPlayer
import io.papermc.paper.adventure.PaperAdventure
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.kyori.adventure.key.Key
import net.minecraft.world.item.ItemCooldowns
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import kotlin.math.max

/**
 * 封装了一名玩家的物品冷却的状态.
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
     * “激活”指定的冷却. 如果已存在将覆盖原有的冷却状态.
     */
    fun activate(id: Identifier, ticks: Int)

    /**
     * “激活”指定的冷却. 如果已存在将覆盖原有的冷却状态.
     */
    fun activate(id: Identifier, entry: AttackSpeed)

    /**
     * 查询指定的冷却是否为“激活”状态.
     */
    fun isActive(id: Identifier): Boolean

    /**
     * 返回指定的冷却的 *未完成冷却时长* 占 *总冷却时长* 的比例.
     */
    fun getPercent(id: Identifier): Float

    /**
     * 查询离冷却变为“未激活”状态的剩余时间, 单位: tick.
     */
    fun getRemainingTicks(id: Identifier): Int

    /**
     * 重置指定冷却的状态, 使其变为“未激活”.
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

    override fun activate(id: Identifier, level: AttackSpeed) {
        activate(id, level.cooldown)
    }

    override fun isActive(id: Identifier): Boolean {
        return getPercent(id) > 0f
    }

    override fun getPercent(id: Identifier): Float {
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
    private val inactiveTimestamps = Object2IntOpenHashMap<Key>()

    override fun activate(id: Identifier, ticks: Int) {
        inactiveTimestamps[id] = Bukkit.getServer().currentTick + ticks
    }

    override fun activate(key: Key, level: AttackSpeed) {
        activate(key, level.cooldown)
    }

    override fun isActive(key: Key): Boolean {
        return inactiveTimestamps.getInt(key) > Bukkit.getServer().currentTick
    }

    override fun getPercent(id: Identifier): Float {
        val remainingTicks = getRemainingTicks(id)
        val totalCooldown = inactiveTimestamps.getInt(id) - Bukkit.getServer().currentTick + remainingTicks
        return if (totalCooldown <= 0) 0f else remainingTicks.toFloat() / totalCooldown
    }

    override fun getRemainingTicks(key: Key): Int {
        val diff = inactiveTimestamps.getInt(key) - Bukkit.getServer().currentTick
        return if (diff < 0) 0 else diff
    }

    override fun reset(key: Key) {
        inactiveTimestamps.put(key, 0)
    }
}

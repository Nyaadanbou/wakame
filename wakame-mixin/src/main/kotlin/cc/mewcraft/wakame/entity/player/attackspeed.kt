package cc.mewcraft.wakame.entity.player

import cc.mewcraft.wakame.ecs.bridge.EComponent
import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.util.*
import cc.mewcraft.wakame.util.adventure.toSimpleString
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.annotations.ApiStatus
import java.util.stream.Stream

/**
 * 代表一种攻击速度.
 *
 * @property cooldown 攻击冷却时长, 单位: tick
 */
class AttackSpeed
@ApiStatus.Internal
constructor(
    override val displayName: Component,
    override val displayStyles: Array<StyleBuilderApplicable>,
    val cooldown: Int,
) : Keyed, Examinable, PlayerFriendlyNamed {

    override fun key(): Identifier {
        return BuiltInRegistries.ATTACK_SPEED.getId(this) ?: Identifiers.of("unregistered")
    }

    override fun examinableProperties(): Stream<out ExaminableProperty?> {
        return Stream.of(
            ExaminableProperty.of("key", key()),
            ExaminableProperty.of("displayName", displayName),
            ExaminableProperty.of("displayStyles", displayStyles),
            ExaminableProperty.of("cooldown", cooldown),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * 封装了一名玩家的攻击冷却的状态.
 */
sealed interface AttackCooldownContainer : EComponent<AttackCooldownContainer> {

    companion object : EComponentType<AttackCooldownContainer>() {

        /**
         * 创建一个基于 NMS 实现的 [AttackCooldownContainer] 实例.
         * 调用该实现上的函数将影响 NMS 中 ServerPlayer 对象的状态!
         */
        fun minecraft(player: Player): AttackCooldownContainer {
            return MinecraftAttackCooldownContainer(player)
        }

        /**
         * 创建一个独立的 [AttackCooldownContainer] 实例.
         */
        fun standalone(): AttackCooldownContainer {
            return StandaloneAttackCooldownContainer()
        }

    }

    override fun type(): EComponentType<AttackCooldownContainer> = AttackCooldownContainer

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
 * 该实现使用 [net.minecraft.server.level.ServerPlayer.cooldowns] 来存储冷却状态.
 * 所有函数的执行效果都具有副作用, 且会被客户端感知 (i.e., NMS会向客户端发送物品冷却的封包).
 */
private class MinecraftAttackCooldownContainer(
    private val player: Player,
) : AttackCooldownContainer {

    override fun activate(id: Identifier, ticks: Int) {
        player.addItemCooldown(id, ticks)
    }

    override fun activate(id: Identifier, level: AttackSpeed) {
        activate(id, level.cooldown)
    }

    override fun isActive(id: Identifier): Boolean {
        return player.isItemCooldownActive(id)
    }

    override fun getPercent(id: Identifier): Float {
        return player.getItemCooldownPercent(id)
    }

    override fun getRemainingTicks(id: Identifier): Int {
        return player.getItemCooldownRemainingTicks(id)
    }

    override fun reset(id: Identifier) {
        player.removeItemCooldown(id)
    }
}

/**
 * 该实现使用独立的数据结构来存储冷却状态, 不会影响任何其他对象的状态.
 */
private class StandaloneAttackCooldownContainer : AttackCooldownContainer {
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

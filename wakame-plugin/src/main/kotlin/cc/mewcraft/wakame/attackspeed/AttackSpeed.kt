package cc.mewcraft.wakame.attackspeed

import cc.mewcraft.wakame.user.User
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import net.kyori.adventure.key.Key
import org.bukkit.Server
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

fun AttackSpeed(user: User<Player>): AttackSpeed {
    return PlayerAttackSpeed(user.uniqueId)
}

sealed interface AttackSpeed {
    /**
     * 添加冷却.
     */
    fun setCooldown(key: Key, level: AttackSpeedLevel)

    /**
     * 是否处于冷却中.
     */
    fun isCooldown(key: Key): Boolean

    /**
     * 获取剩余冷却时间.
     */
    fun getCooldown(key: Key): Long

    /**
     * 移除冷却.
     */
    fun removeCooldown(key: Key)
}

private class PlayerAttackSpeed(
    private val uniqueId: UUID
) : AttackSpeed {
    // Key: 冷却物品的 Key, Value: 冷却结束的 Tick
    private val cooldowns = Object2LongOpenHashMap<Key>()

    override fun setCooldown(key: Key, level: AttackSpeedLevel) {
        cooldowns[key] = PlayerAttackSpeedSupport.server.currentTick + level.cooldown.toLong()
    }

    override fun isCooldown(key: Key): Boolean {
        if (cooldowns.containsKey(key)) {
            if (cooldowns.getLong(key) > PlayerAttackSpeedSupport.server.currentTick) {
                return true
            }
            cooldowns.removeLong(key)
        }
        return false
    }

    override fun getCooldown(key: Key): Long {
        return cooldowns.getLong(key) - PlayerAttackSpeedSupport.server.currentTick
    }

    override fun removeCooldown(key: Key) {
        cooldowns.removeLong(key)
    }
}

private object PlayerAttackSpeedSupport : KoinComponent {
    val server: Server by inject()
}
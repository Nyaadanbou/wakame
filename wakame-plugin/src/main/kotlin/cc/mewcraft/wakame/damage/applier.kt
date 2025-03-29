package cc.mewcraft.wakame.damage

import org.bukkit.entity.LivingEntity
import org.jetbrains.annotations.ApiStatus

/**
 * API for applying damage to entities.
 */
// 本接口用来给 [单纯造成伤害] 的逻辑提供抽象,
// 以保证伤害系统的核心代码的简洁和可维护性.
interface DamageApplier {
    /**
     * Applies damage to [victim].
     *
     * @param victim The entity to apply damage to.
     * @param source The entity that caused the damage.
     * @param amount The amount of damage to apply.
     */
    fun damage(victim: LivingEntity, source: LivingEntity?, amount: Double)

    /**
     * 伴生对象, 提供 [DamageManagerApi] 的实例.
     */
    companion object {

        @get:JvmName("getInstance")
        lateinit var INSTANCE: DamageApplier
            private set

        @ApiStatus.Internal
        fun register(instance: DamageApplier) {
            this.INSTANCE = instance
        }

    }
}

// ------------
// 内部实现
// ------------

internal object BukkitDamageApplier : DamageApplier {
    override fun damage(victim: LivingEntity, source: LivingEntity?, amount: Double) {
        // 这里仅仅用于触发一下 Bukkit 的 EntityDamageEvent.
        // 伤害数值填多少都无所谓, 最后都会被事件监听逻辑重新计算.
        victim.damage(amount, source)
    }
}
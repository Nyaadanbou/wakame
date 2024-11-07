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
    companion object Provider {
        private var instance: DamageApplier? = null

        @JvmStatic
        fun instance(): DamageApplier {
            return instance ?: throw IllegalStateException("DamageApplier has not been initialized")
        }

        @ApiStatus.Internal
        fun register(instance: DamageApplier) {
            this.instance = instance
        }

        @ApiStatus.Internal
        fun unregister() {
            instance = null
        }
    }
}
package cc.mewcraft.wakame.damage

import org.bukkit.entity.LivingEntity
import org.jetbrains.annotations.ApiStatus

/**
 * API for applying damage to entities.
 */
interface DamageApplierApi {
    /**
     * Applies damage to a victim.
     *
     * @param victim The entity to apply damage to.
     * @param source The entity that caused the damage.
     * @param amount The amount of damage to apply.
     */
    fun doDamage(victim: LivingEntity, source: LivingEntity?, amount: Double)

    /**
     * 伴生对象, 提供 [DamageManagerApi] 的实例.
     */
    companion object Provider {
        private var instance: DamageApplierApi? = null

        @JvmStatic
        fun instance(): DamageApplierApi {
            return instance ?: throw IllegalStateException("DamageManager has not been initialized")
        }

        @ApiStatus.Internal
        fun register(instance: DamageApplierApi) {
            this.instance = instance
        }

        @ApiStatus.Internal
        fun unregister() {
            instance = null
        }
    }
}
@file:JvmName("LogicApi")

package cc.mewcraft.wakame.damage

import org.bukkit.entity.LivingEntity
import org.jetbrains.annotations.ApiStatus

/**
 * @see DamageManagerApi.hurt
 */
fun LivingEntity.hurt(damageMetadata: DamageMetadata, source: LivingEntity? = null, knockback: Boolean = false) {
    DamageManagerApi.INSTANCE.hurt(this, damageMetadata, source, knockback)
}

interface DamageManagerApi {

    /**
     * 对 [victim] 造成由 [metadata] 指定的自定义伤害.
     *
     * 当 [damager] 为 `null` 时, 伤害属于无源, 不会产生击退效果.
     *
     * @param victim 受到伤害的实体
     * @param metadata 伤害的元数据
     * @param damager 造成伤害的实体
     * @param knockback 是否产生击退效果
     */
    fun hurt(
        victim: LivingEntity,
        metadata: DamageMetadata,
        damager: LivingEntity? = null,
        knockback: Boolean = false,
    )

    /**
     * 伴生对象, 提供 [DamageManagerApi] 的实例.
     */
    companion object {

        @get:JvmName("getInstance")
        lateinit var INSTANCE: DamageManagerApi
            private set

        @ApiStatus.Internal
        fun register(instance: DamageManagerApi) {
            this.INSTANCE = instance
        }

    }

}
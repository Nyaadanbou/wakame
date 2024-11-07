package cc.mewcraft.wakame.damage

import org.bukkit.entity.LivingEntity
import org.jetbrains.annotations.ApiStatus

interface DamageManagerApi {
    /**
     * 对 [victim] 造成由 [damageMetadata] 指定的萌芽伤害.
     * 当 [source] 为 `null` 时, 伤害属于无源, 不会产生击退效果.
     *
     * @param victim 受到伤害的实体
     * @param damageMetadata 伤害的元数据
     * @param source 造成伤害的实体
     * @param knockback 是否产生击退效果
     */
    fun hurt(
        victim: LivingEntity, damageMetadata: DamageMetadata,
        source: LivingEntity? = null, knockback: Boolean = false,
    )

    /**
     * 伴生对象, 提供 [DamageManagerApi] 的实例.
     */
    companion object Provider {
        private var instance: DamageManagerApi? = null

        @JvmStatic
        fun instance(): DamageManagerApi {
            return instance ?: throw IllegalStateException("DamageManager has not been initialized")
        }

        @ApiStatus.Internal
        fun register(instance: DamageManagerApi) {
            this.instance = instance
        }

        @ApiStatus.Internal
        fun unregister() {
            instance = null
        }
    }
}
@file:JvmName("LogicApi")

package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.entity.attribute.AttributeMapSnapshot
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.event.bukkit.PreprocessDamageEvent
import cc.mewcraft.wakame.event.bukkit.PreprocessDamageEvent.Phase
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
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

// ------------
// 内部实现
// ------------

@ApiStatus.Internal
fun DamageContext(event: EntityDamageEvent): DamageContext {
    val damage = event.damage
    val damagee = event.entity as? LivingEntity ?: error("The damagee must be a living entity")
    val damageSource = event.damageSource
    val damageCause = event.cause
    return DamageContext(damage, damagee, damageSource, damageCause)
}

/**
 * 伤害信息的封装.
 */
@ApiStatus.Internal
class DamageContext(
    val damage: Double,
    val damagee: LivingEntity,
    val damageSource: DamageSource,
    val damageCause: DamageCause,
) {
    override fun toString(): String {
        return "DamageContext(damage=$damage, damagee=$damagee, damageCause=$damageCause, damageType=${damageSource.damageType}, causingEntity=${damageSource.causingEntity}, directEntity=${damageSource.directEntity}, damageLocation=${damageSource.damageLocation})"
    }
}

/**
 * 用于方便构建 [PreprocessDamageEvent].
 */
@ApiStatus.Internal
object PreprocessDamageEventFactory {

    fun launchProjectile(causingEntity: Player): PreprocessDamageEvent {
        return PreprocessDamageEvent(
            phase = Phase.SEARCHING_TARGET,
            causingEntity = causingEntity,
            causingAttributes = causingEntity.attributeContainer.getSnapshot(),
            _damageeEntity = null,
            _damageSource = null
        )
    }

    fun searchingTarget(causingEntity: Player): PreprocessDamageEvent {
        return PreprocessDamageEvent(
            phase = Phase.SEARCHING_TARGET,
            causingEntity = causingEntity,
            causingAttributes = causingEntity.attributeContainer.getSnapshot(),
            _damageeEntity = null,
            _damageSource = null
        )
    }

    fun actuallyDamage(causingEntity: Player, damageeEntity: LivingEntity): PreprocessDamageEvent {
        return PreprocessDamageEvent(
            phase = Phase.ACTUALLY_DAMAGE,
            causingEntity = causingEntity,
            causingAttributes = causingEntity.attributeContainer.getSnapshot(),
            _damageeEntity = damageeEntity,
            _damageSource = DamageSource.builder(DamageType.GENERIC)
                .withCausingEntity(causingEntity)
                .withDirectEntity(causingEntity)
                .withDamageLocation(damageeEntity.location)
                .build(),
        )
    }

    fun actuallyDamage(causingEntity: Player, damageContext: DamageContext): PreprocessDamageEvent {
        return PreprocessDamageEvent(
            phase = Phase.ACTUALLY_DAMAGE,
            causingEntity = causingEntity,
            causingAttributes = causingEntity.attributeContainer.getSnapshot(),
            _damageeEntity = damageContext.damagee,
            _damageSource = damageContext.damageSource,
        )
    }

    fun actuallyDamage(causingEntity: Player, causingAttributes: AttributeMapSnapshot, damageContext: DamageContext): PreprocessDamageEvent {
        return PreprocessDamageEvent(
            phase = Phase.ACTUALLY_DAMAGE,
            causingEntity = causingEntity,
            causingAttributes = causingAttributes,
            _damageeEntity = damageContext.damagee,
            _damageSource = damageContext.damageSource,
        )
    }
}
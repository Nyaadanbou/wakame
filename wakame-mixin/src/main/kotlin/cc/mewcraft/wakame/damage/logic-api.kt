@file:JvmName("LogicApi")

package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap
import org.bukkit.damage.DamageSource
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier.ABSORPTION
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier.BASE
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier.BLOCKING
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier.RESISTANCE
import org.jetbrains.annotations.ApiStatus


fun LivingEntity.hurt(metadata: DamageMetadata, damager: LivingEntity? = null, knockback: Boolean = false) {
    DamageManagerApi.INSTANCE.hurt(this, metadata, damager, knockback)
}

fun LivingEntity.hurt(metadata: DamageMetadata, source: DamageSource, knockback: Boolean): Boolean {
    return DamageManagerApi.INSTANCE.hurt(this, metadata, source, knockback)
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
        knockback: Boolean = true,
    )

    /**
     * 对 [victim] 造成由 [metadata] 指定的自定义伤害.
     *
     * @param victim 受到伤害的实体
     * @param metadata 伤害的元数据
     * @param source 伤害来源信息
     * @param knockback 是否产生击退效果
     * @return 是否真的造成了伤害及其附加效果. 诸如生物无敌, 免疫该伤害, 伤害事件被取消等情况会返回 false.
     */
    fun hurt(
        victim: LivingEntity,
        metadata: DamageMetadata,
        source: DamageSource,
        knockback: Boolean = true,
    ): Boolean

    /**
     * 将插入到原版伤害系统触发 [EntityDamageEvent] 之后、造成实际伤害效果(扣血, 装备损失耐久等)之前的位置.
     * @return 最终要被记录到 LivingEntity#lastHurt 变量中的值
     */
    fun injectDamageLogic(event: EntityDamageEvent, isDuringInvulnerable: Boolean): Float

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
fun RawDamageContext(event: EntityDamageEvent): RawDamageContext {
    val damage = event.damage
    val damagee = event.entity as? LivingEntity ?: error("The damagee must be a living entity")
    val damageSource = event.damageSource
    return RawDamageContext(damage, damagee, damageSource)
}

/**
 * 对 Bukkit 伤害事件所含伤害信息的封装.
 * 用于兼容原版的各种伤害场景.
 * 其中不含 Koish 自定义伤害的信息.
 */
@ApiStatus.Internal
class RawDamageContext(
    val damage: Double,
    val damagee: LivingEntity,
    val damageSource: DamageSource
) {
    override fun toString(): String {
        return "DamageContext(damage=$damage, damagee=$damagee, damageType=${damageSource.damageType}, causingEntity=${damageSource.causingEntity}, directEntity=${damageSource.directEntity}, damageLocation=${damageSource.damageLocation})"
    }
}

/**
 * 对最终伤害的信息封装.
 */
@ApiStatus.Internal
class FinalDamageContext(
    /**
     * 攻击阶段的伤害信息.
     */
    val damageMetadata: DamageMetadata,

    /**
     * 防御阶段的伤害信息.
     */
    val defenseMetadata: DefenseMetadata,

    /**
     * 原版 [BASE] 修饰器将要被修改为的值.
     */
    val baseModifierValue: Double,

    /**
     * 原版 [BLOCKING] 修饰器将要被修改为的值.
     * 空值意味着不修改.
     */
    val blockingModifierValue: Double?,

    /**
     * 原版 [RESISTANCE] 修饰器将要被修改为的值.
     * 空值意味着不修改.
     */
    val resistanceModifierValue: Double?,

    /**
     * 原版 [ABSORPTION] 修饰器将要被修改为的值.
     * 空值意味着不修改.
     */
    val absorptionModifierValue: Double?,

    /**
     * 各元素的最终伤害值.
     * 真正意义上的"最终", 可直接显示给玩家.
     */
    val finalDamageMap: Reference2DoubleMap<RegistryEntry<Element>>
)
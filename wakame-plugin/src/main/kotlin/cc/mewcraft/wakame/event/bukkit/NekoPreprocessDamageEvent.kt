package cc.mewcraft.wakame.event.bukkit

import cc.mewcraft.wakame.damage.DamageContext
import cc.mewcraft.wakame.entity.attribute.AttributeMapSnapshot
import cc.mewcraft.wakame.user.attributeContainer
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * 该事件发生在攻击方的属性 [causingAttributes] 被传递到伤害系统的计算逻辑之前.
 *
 * - 监听该事件可以修改 *攻击方* 的属性以影响伤害结果. 属性为快照, 因此仅影响本次伤害.
 * - 只有玩家造成的伤害会触发该事件, 因此该事件始终包含一个 [Player] 类型的 [causingEntity].
 *
 * ### 其他
 * 值得一提的是, 伤害系统在设计之初就不支持直接修改最终的伤害值. 所有关于伤害的修改都必须通过属性来实现.
 * 如此设计可以让不同系统(如技能,附魔,武器效果)对伤害的修饰处于同一框架之内, 使得每个修饰都可被精确描述.
 * 如果无法使用现有框架来实现的一个新的伤害机制, 则应该优先考虑添加新的属性类型, 而不是曲线救国.
 *
 * @property phase 伤害计算的阶段
 * @property causingEntity 造成伤害的玩家
 * @property causingAttributes 造成伤害的玩家的属性(快照)
 * @property damagee 受到伤害的实体. 为 null 意为还未造成实际伤害.
 * @property damageSource 更多的伤害信息. 为 null 意为还未造成实际伤害.
 */
// FIXME #366: 让 preprocess 支持 cancel 以尽早的停止伤害计算节省资源?
class NekoPreprocessDamageEvent
internal constructor(
    val phase: Phase,
    val causingEntity: Player,
    val causingAttributes: AttributeMapSnapshot, // 注意, 这里的属性与 causingEntity 直接返回的属性可能不一样
    private val _damageeEntity: LivingEntity?,
    private val _damageSource: DamageSource?,
) : Event() {

    val damagee: LivingEntity
        get() = _damageeEntity ?: throw IllegalStateException("damagee is null")

    val damageSource: DamageSource
        get() = _damageSource ?: throw IllegalStateException("damageSource is null")

    enum class Phase {
        /**
         * 调试指令.
         */
        DEBUG_COMMAND,
        /**
         * 发射弹射物时.
         */
        LAUNCH_PROJECTILE,
        /**
         * 玩家左键攻击.
         */
        DIRECT_ATTACK,
        /**
         * 玩家横扫之刃攻击.
         */
        SWEEP_ATTACK,
        /**
         * 棍造成实际伤害前, 搜索目标.
         */
        CUDGEL_SEARCHING_TARGET,
        /**
         * 棍造成实际伤害.
         */
        CUDGEL_ACTUALLY_DAMAGE,
        /**
         * 太刀居合斩.
         */
        LAI_SLASH,
        /**
         * 太刀气刃斩.
         */
        SPIRIT_BLADE_SLASH,
        /**
         * 太刀气刃大回旋斩.
         */
        ROUND_SLASH,
        /**
         * 太刀横斩.
         */
        HORIZONTAL_SLASH,

        /**
         * “寻找目标”, 在这个阶段代码需要读取属性, 但还未确定受伤实体.
         *
         * 目前存在的实际场景:
         * - 部分近战武器在发起攻击时需要先读取属性计算出“攻击范围” (此时该事件触发, 但还未知受伤实体), 然后再根据攻击范围造成 AoE 伤害
         * - 弓/弩/三叉戟在射出箭矢的一瞬间, 需要计算弹射物将要造成的最终伤害 (此时该事件触发, 但还未知受伤实体)
         */
        SEARCHING_TARGET,

        /**
         * “造成伤害”, 在这个阶段代码需要读取属性, 且已经确定受伤实体.
         *
         * 接着说上面的场景:
         * - 根据范围来计算出需要受到伤害的实体, 然后再读取属性计算伤害 (此时该事件触发, 已知受伤实体)
         * - 弹射物打到实体上, 此时需要使用先前已经计算出的伤害来造成伤害 (这个比较特殊, 这里暂时不触发该事件, 因为伤害已在发射弹射物时确定)
         */
        ACTUALLY_DAMAGE,
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }

        internal fun launchProjectile(causingEntity: Player): NekoPreprocessDamageEvent {
            return NekoPreprocessDamageEvent(
                phase = Phase.SEARCHING_TARGET,
                causingEntity = causingEntity,
                causingAttributes = causingEntity.attributeContainer.getSnapshot(),
                _damageeEntity = null,
                _damageSource = null
            )
        }

        internal fun searchingTarget(causingEntity: Player): NekoPreprocessDamageEvent {
            return NekoPreprocessDamageEvent(
                phase = Phase.SEARCHING_TARGET,
                causingEntity = causingEntity,
                causingAttributes = causingEntity.attributeContainer.getSnapshot(),
                _damageeEntity = null,
                _damageSource = null
            )
        }

        internal fun actuallyDamage(causingEntity: Player, damageeEntity: LivingEntity): NekoPreprocessDamageEvent {
            return NekoPreprocessDamageEvent(
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

        internal fun actuallyDamage(causingEntity: Player, damageContext: DamageContext): NekoPreprocessDamageEvent {
            return NekoPreprocessDamageEvent(
                phase = Phase.ACTUALLY_DAMAGE,
                causingEntity = causingEntity,
                causingAttributes = causingEntity.attributeContainer.getSnapshot(),
                _damageeEntity = damageContext.damagee,
                _damageSource = damageContext.damageSource,
            )
        }

        internal fun actuallyDamage(causingEntity: Player, causingAttributes: AttributeMapSnapshot, damageContext: DamageContext): NekoPreprocessDamageEvent {
            return NekoPreprocessDamageEvent(
                phase = Phase.ACTUALLY_DAMAGE,
                causingEntity = causingEntity,
                causingAttributes = causingAttributes,
                _damageeEntity = damageContext.damagee,
                _damageSource = damageContext.damageSource,
            )
        }
    }
}

package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.commons.provider.immutable.provider
import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKey
import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.skill.tick.SkillTicker
import cc.mewcraft.wakame.skill.tick.TickResult
import cc.mewcraft.wakame.util.Key
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import me.lucko.helper.Events
import net.kyori.adventure.key.Key
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDismountEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.entity.Arrow as ArrowEntity

/**
 * 代表一个弹射物.
 */
interface Projectile : Skill {
    /**
     * 弹射物的类型.
     */
    enum class Type {
        ARROW,
    }

    /**
     * 弹射物的效果触发器.
     */
    enum class Trigger {
        /**
         * 当此弹射物技能被触发时.
         */
        START,

        /**
         * 当此弹射物被 tick 时.
         */
        TICK,

        /**
         * 当此弹射物击中生物时.
         */
        HIT_ENTITY,

        /**
         * 当此弹射消失时.
         */
        DISAPPEAR,

        /**
         * 当此弹射物击中方块时.
         */
        HIT_BLOCK,
    }

    /**
     * 弹射物的类型.
     */
    val type: Type

    /**
     * 弹射物的持续时间, 以 Tick 作为单位.
     */
    val durationTick: Long

    /**
     * 初始位移速度.
     *
     * 水平方向的力（支持正负）,
     * 值为正为玩家面向的方向, 反之则为玩家背向的方向.
     */
    val initialVelocity: Float

    /**
     * 可移动的最大距离.
     */
    val maximumDistance: Int

    /**
     * 弹射物作用范围.
     */
    val range: Double

    /**
     * 可穿透的次数. 0 表示不可穿透.
     */
    val penetration: Int

    /**
     * 是否会被重力影响.
     */
    val gravity: Boolean

    /**
     * 弹射物的伤害.
     */
    val damage: Evaluable<*>

    /**
     * 触发的效果.
     */
    val effects: Map<Trigger, Skill>

    companion object Factory : SkillFactory<Projectile> {
        override fun create(key: Key, config: ConfigProvider): Projectile {
            // TODO: 读取配置文件而不是全都都默认值
            val projectileType = config.optionalEntry<Type>("projectile_type").orElse(Type.ARROW)
            val durationTick = config.optionalEntry<Long>("duration_tick").orElse(0)
            val initialVelocity = config.optionalEntry<Float>("initial_velocity").orElse(2F)
            val maximumDistance = config.optionalEntry<Int>("maximum_distance").orElse(10)
            val range = config.optionalEntry<Double>("range").orElse(1.0)
            val penetration = config.optionalEntry<Int>("penetration").orElse(0)
            val gravity = config.optionalEntry<Boolean>("gravity").orElse(true)
            val effects = provider {
                mapOf(
                    Trigger.HIT_ENTITY to SkillRegistry.INSTANCES[Key(Namespaces.SKILL, "melee/kill_entity")]
                )
            }
            val damage = config.optionalEntry<Evaluable<*>>("damage").orElse(Evaluable.StringEval("0"))

            return DefaultImpl(
                key, config,
                type = projectileType,
                durationTick = durationTick,
                initialVelocity = initialVelocity,
                maximumDistance = maximumDistance,
                range = range,
                penetration = penetration,
                gravity = gravity,
                effects = effects,
                damage = damage
            )
        }
    }

    private class DefaultImpl(
        override val key: Key,
        config: ConfigProvider,
        type: Provider<Type>,
        durationTick: Provider<Long>,
        initialVelocity: Provider<Float>,
        maximumDistance: Provider<Int>,
        range: Provider<Double>,
        penetration: Provider<Int>,
        gravity: Provider<Boolean>,
        damage: Provider<Evaluable<*>>,
        effects: Provider<Map<Trigger, Skill>>
    ) : Projectile, SkillBase(key, config) {
        override val type: Type by type
        override val durationTick: Long by durationTick
        override val initialVelocity: Float by initialVelocity
        override val maximumDistance: Int by maximumDistance
        override val range: Double by range
        override val penetration: Int by penetration
        override val gravity: Boolean by gravity
        override val damage: Evaluable<*> by damage
        override val effects: Map<Trigger, Skill> by effects

        override fun cast(context: SkillCastContext): SkillTick {
            return Tick(context)
        }

        private inner class Tick(
            override val context: SkillCastContext
        ) : SkillTick {
            override val skill: Skill = this@DefaultImpl

            override fun tick(): TickResult {
                val location = context.optional(SkillCastContextKey.TARGET_LOCATION) ?: return TickResult.INTERRUPT
                val projectile = when (type) {
                    Type.ARROW -> Arrow(location)
                }
                if (!projectile.summon()) {
                    return TickResult.INTERRUPT
                }

                return TickResult.ALL_DONE
            }

            private inner class Arrow(
                val summonLocation: Target.Location
            ) {
                fun summon(): Boolean {
                    val location = summonLocation.bukkitLocation
                    val world = location.world
                    val arrowEntity: ArrowEntity =
                        world.spawnArrow(location, location.direction, initialVelocity, 0.0f)
                    arrowEntity.setGravity(gravity)
                    arrowEntity.velocity = location.direction.multiply(initialVelocity)
                    // TODO: 更多的属性设置

                    val newTickCaster = CasterAdapter.adapt(this@Tick)
                    val parent = context.caster ?: return false
                    context.caster = CasterAdapter.composite(newTickCaster, CasterAdapter.composite(parent))
                    val startSkillTick = effects[Trigger.START]?.cast(context)

                    if (startSkillTick != null) {
                        SkillTicker.addChildren(startSkillTick)
                    }

                    // 注册事件监听器
                    registerTickEvent(arrowEntity)
                    registerHitEntityEvent(arrowEntity)
                    registerHitBlockEvent(arrowEntity)
                    registerDisappearEvent(arrowEntity)
                    return true
                }

                private fun registerTickEvent(arrow: ArrowEntity) {
                    Events.subscribe(ServerTickStartEvent::class.java)
                        .expireIf { arrow.isDead }
                        .handler {
                            val node = context.get(SkillCastContextKey.CASTER_COMPOSITE_NODE)
                            val single = node.root().value
                            println(single)
                            val newContext = SkillCastContext(CasterAdapter.adapt(arrow), TargetAdapter.adapt(arrow.location))
                            val tickSkillTick = effects[Trigger.TICK]?.cast(newContext) ?: return@handler
                            SkillTicker.addChildren(tickSkillTick)
                        }
                }

                private fun registerHitEntityEvent(arrow: ArrowEntity) {
                    Events.subscribe(ServerTickStartEvent::class.java)
                        .expireIf { arrow.isDead }
                        .handler {
                            val hitEntity = arrow.getNearbyEntities(range, range, range)
                                .firstOrNull()
                            if (hitEntity != null && hitEntity is LivingEntity) {
                                val newContext =
                                    SkillCastContext(CasterAdapter.adapt(arrow), TargetAdapter.adapt(hitEntity))
                                val hitEntitySkillTick = effects[Trigger.HIT_ENTITY]?.cast(newContext) ?: return@handler
                                SkillTicker.addChildren(hitEntitySkillTick)
                            }
                        }
                }

                private fun registerHitBlockEvent(arrow: ArrowEntity) {
                    Events.subscribe(ProjectileHitEvent::class.java)
                        .expireIf { arrow.isDead }
                        .handler {
                            if (it.entity != arrow) return@handler
                            val hitBlock = it.hitBlock
                            if (hitBlock != null) {
                                val newContext =
                                    SkillCastContext(CasterAdapter.adapt(arrow), TargetAdapter.adapt(hitBlock.location))
                                val hitBlockSkillTick = effects[Trigger.HIT_BLOCK]?.cast(newContext) ?: return@handler
                                SkillTicker.addChildren(hitBlockSkillTick)
                            }
                        }
                }

                private fun registerDisappearEvent(arrow: ArrowEntity) {
                    Events.subscribe(EntityDismountEvent::class.java)
                        .expireIf { arrow.isDead }
                        .handler {
                            if (it.entity != arrow) return@handler
                            val newContext = SkillCastContext(CasterAdapter.adapt(arrow))
                            val disappearSkillTick = effects[Trigger.DISAPPEAR]?.cast(newContext) ?: return@handler
                            SkillTicker.addChildren(disappearSkillTick)
                        }
                }
            }
        }
    }
}
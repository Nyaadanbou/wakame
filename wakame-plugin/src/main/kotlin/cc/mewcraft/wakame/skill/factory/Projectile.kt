package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.map
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey
import cc.mewcraft.wakame.skill.tick.AbstractPlayerSkillTick
import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.tick.TickResult
import cc.mewcraft.wakame.tick.Tickable
import cc.mewcraft.wakame.tick.Ticker
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import me.lucko.helper.Events
import net.kyori.adventure.key.Key
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerPickupArrowEvent
import org.bukkit.projectiles.ProjectileSource
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
         * 当此弹射物被玩家捡起时 (仅支持原版弹射物).
         */
        PICK_UP,

        /**
         * 当此弹射物消失时. (特指因为超过最大距离而消失, 其他情况不处理).
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
     * 弹射物存在的持续时间.
     */
    val duration: Int

    /**
     * 可穿透的次数. 0 表示不可穿透.
     */
    val penetration: Int

    /**
     * 是否会被重力影响.
     */
    val gravity: Boolean

    /**
     * 触发的效果.
     */
    val effects: Map<Trigger, Skill>

    companion object Factory : SkillFactory<Projectile> {
        override fun create(key: Key, config: ConfigProvider): Projectile {
            val projectileType = config.entry<Type>("projectile_type")
            val initialVelocity = config.optionalEntry<Float>("initial_velocity").orElse(2F)
            val maximumDistance = config.optionalEntry<Int>("maximum_distance").orElse(100)
            val duration = config.optionalEntry<Int>("duration").orElse(100)
            val penetration = config.optionalEntry<Int>("penetration").orElse(0)
            val gravity = config.optionalEntry<Boolean>("gravity").orElse(true)
            val effects = config.optionalEntry<Map<String, Key>>("effects").orElse(emptyMap()).map { map ->
                map.mapNotNull { (trigger, skillKey) ->
                    val triggerOrNull = Trigger.entries.firstOrNull { it.name.equals(trigger, ignoreCase = true) } ?: return@mapNotNull null
                    triggerOrNull to SkillRegistry.INSTANCES[skillKey]
                }.toMap()
            }

            return DefaultImpl(
                key, config,
                type = projectileType,
                initialVelocity = initialVelocity,
                maximumDistance = maximumDistance,
                penetration = penetration,
                gravity = gravity,
                duration = duration,
                effects = effects
            )
        }
    }

    private class DefaultImpl(
        override val key: Key,
        config: ConfigProvider,
        type: Provider<Type>,
        initialVelocity: Provider<Float>,
        maximumDistance: Provider<Int>,
        duration: Provider<Int>,
        penetration: Provider<Int>,
        gravity: Provider<Boolean>,
        effects: Provider<Map<Trigger, Skill>>
    ) : Projectile, SkillBase(key, config) {
        override val type: Type by type
        override val initialVelocity: Float by initialVelocity
        override val maximumDistance: Int by maximumDistance
        override val duration: Int by duration
        override val penetration: Int by penetration
        override val gravity: Boolean by gravity
        override val effects: Map<Trigger, Skill> by effects

        override fun cast(context: SkillContext): SkillTick {
            return Tick(context)
        }

        private inner class Tick(
            context: SkillContext
        ) : AbstractPlayerSkillTick(this@DefaultImpl, context) {

            override fun tickCast(tickCount: Long): TickResult {
                val target = TargetUtil.getLocation(context) ?: return TickResult.INTERRUPT
                val location = target.bukkitLocation
                val projectile = when (type) {
                    Type.ARROW -> Arrow(location)
                }
                if (!projectile.summon()) {
                    return TickResult.INTERRUPT
                }

                Ticker.addTick(
                    Tickable { tc ->
                        if (tc >= duration) {
                            projectile.remove()
                            return@Tickable TickResult.ALL_DONE
                        }
                        TickResult.CONTINUE_TICK
                    }
                )

                return TickResult.ALL_DONE
            }


            private inner class Arrow(
                val summonLocation: Location
            ) {
                private var arrowEntity: ArrowEntity? = null

                private val parent: Caster.CompositeNode?
                    get() = context[SkillContextKey.CASTER]

                fun summon(): Boolean {
                    val shooter = parent?.value<Caster.Single.Entity>()?.bukkitEntity as? ProjectileSource

                    arrowEntity = if (shooter != null) {
                        shooter.launchProjectile(ArrowEntity::class.java, null) {
                            it.setGravity(gravity)
                            it.velocity = it.velocity.normalize().multiply(initialVelocity)
                            it.pierceLevel = penetration
                        }
                    } else {
                        val world = summonLocation.world
                        world.spawnArrow(summonLocation, summonLocation.direction, initialVelocity, 0.0f)
                            .also {
                                it.setGravity(gravity)
                                it.velocity = summonLocation.direction.normalize().multiply(initialVelocity)
                                it.pierceLevel = penetration
                            }
                    }

                    val newTickCaster = CasterAdapter.adapt(this@Tick)
                    context[SkillContextKey.CASTER] = newTickCaster.toComposite(parent)
                    val startSkillTick = effects[Trigger.START]?.cast(context)

                    if (startSkillTick != null) {
                        Ticker.addTick(startSkillTick)
                    }

                    // 注册事件监听器
                    with(arrowEntity!!) {
                        registerTickEvent(this)
                        registerHitEntityEvent(this)
                        registerHitBlockEvent(this)
                        registerDisappearEvent(this)
                    }
                    return true
                }

                fun remove() {
                    arrowEntity?.remove()
                }

                private fun registerTickEvent(arrow: ArrowEntity) {
                    Events.subscribe(ServerTickStartEvent::class.java)
                        .expireIf { arrow.isDead }
                        .handler {
                            val newContext = SkillContext(CasterAdapter.adapt(arrow).toComposite(parent), TargetAdapter.adapt(arrow.location))
                            val tickSkillTick = effects[Trigger.TICK]?.cast(newContext) ?: return@handler
                            Ticker.addTick(tickSkillTick)
                        }

                    Events.subscribe(ServerTickStartEvent::class.java)
                        .expireIf { arrow.isDead }
                        .handler {
                            if (arrow.location.distance(summonLocation) > maximumDistance) {
                                val newContext = SkillContext(CasterAdapter.adapt(arrow).toComposite(parent), TargetAdapter.adapt(arrow.location))
                                val disappearSkillTick = effects[Trigger.DISAPPEAR]?.cast(newContext) ?: return@handler
                                Ticker.addTick(disappearSkillTick)
                                arrow.remove()
                            }
                        }
                }

                private fun registerHitEntityEvent(arrow: ArrowEntity) {
                    Events.subscribe(ProjectileHitEvent::class.java)
                        .expireIf { arrow.isDead }
                        .handler {
                            if (it.entity != arrow) return@handler
                            val hitEntity = it.hitEntity ?: return@handler
                            if (hitEntity is LivingEntity) {
                                val newContext = SkillContext(CasterAdapter.adapt(arrow).toComposite(parent), TargetAdapter.adapt(hitEntity))
                                val hitEntitySkillTick = effects[Trigger.HIT_ENTITY]?.cast(newContext) ?: return@handler
                                Ticker.addTick(hitEntitySkillTick)
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
                                val newContext = SkillContext(CasterAdapter.adapt(arrow).toComposite(parent), TargetAdapter.adapt(hitBlock.location))
                                val hitBlockSkillTick = effects[Trigger.HIT_BLOCK]?.cast(newContext) ?: return@handler
                                Ticker.addTick(hitBlockSkillTick)
                            }
                        }
                }

                private fun registerDisappearEvent(arrow: ArrowEntity) {
                    Events.subscribe(PlayerPickupArrowEvent::class.java)
                        .expireIf { arrow.isDead }
                        .handler {
                            if (it.arrow != arrow) return@handler
                            val newContext = SkillContext(CasterAdapter.adapt(arrow).toComposite(parent), TargetAdapter.adapt(it.player))
                            val pickupSkillTick = effects[Trigger.PICK_UP]?.cast(newContext) ?: return@handler
                            Ticker.addTick(pickupSkillTick)
                        }
                }
            }
        }
    }
}
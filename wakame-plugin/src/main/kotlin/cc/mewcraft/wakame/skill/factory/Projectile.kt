package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey
import cc.mewcraft.wakame.skill.factory.Projectile.Trigger
import cc.mewcraft.wakame.skill.factory.Projectile.Type
import cc.mewcraft.wakame.skill.tick.AbstractPlayerSkillTick
import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.tick.TickResult
import cc.mewcraft.wakame.tick.TickableBuilder
import cc.mewcraft.wakame.tick.Ticker
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import me.lucko.helper.Events
import me.lucko.helper.event.Subscription
import net.kyori.adventure.key.Key
import org.bukkit.Location
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerPickupArrowEvent
import org.bukkit.projectiles.ProjectileSource
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.ScalarSerializer
import java.util.function.Predicate
import java.lang.reflect.Type as ReflectType

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
    val effects: Map<Trigger, SkillProvider>

    companion object Factory : SkillFactory<Projectile> {
        override fun create(key: Key, config: ConfigurationNode): Projectile {
            val projectileType = config.node("projectile_type").krequire<Type>()
            val initialVelocity = config.node("initial_velocity").get<Float>() ?: 2F
            val maximumDistance = config.node("maximum_distance").get<Int>() ?: 100
            val duration = config.node("duration").get<Int>() ?: 100
            val penetration = config.node("penetration").get<Int>() ?: 0
            val gravity = config.node("gravity").get<Boolean>() ?: true
            val effects = config.node("effects").get<Map<Trigger, SkillProvider>>() ?: emptyMap()

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
        config: ConfigurationNode,
        override val type: Type,
        override val initialVelocity: Float,
        override val maximumDistance: Int,
        override val duration: Int,
        override val penetration: Int,
        override val gravity: Boolean,
        override val effects: Map<Trigger, SkillProvider>,
    ) : Projectile, SkillBase(key, config) {

        override fun cast(context: SkillContext): SkillTick<Projectile> {
            return ProjectileTick(context, this)
        }
    }
}

private class ProjectileTick(
    context: SkillContext,
    val projectile: Projectile
) : AbstractPlayerSkillTick<Projectile>(projectile, context) {

    override fun tickCast(tickCount: Long): TickResult {
        if (!checkConditions())
            return TickResult.ALL_DONE
        val target = TargetUtil.getLocation(context) ?: return TickResult.INTERRUPT
        val location = target.bukkitLocation
        val projectile = when (projectile.type) {
            Type.ARROW -> ArrowWrapper(projectile, context, location, this)
        }
        if (!projectile.summon()) {
            return TickResult.INTERRUPT
        }

        val tickable = TickableBuilder.newBuilder()
            .execute { tc ->
                if (tc >= this@ProjectileTick.projectile.duration) {
                    projectile.remove()
                    return@execute TickResult.ALL_DONE
                }
                TickResult.CONTINUE_TICK
            }

        Ticker.INSTANCE.addTick(tickable)

        return TickResult.ALL_DONE
    }
}

private class ArrowWrapper(
    val projectile: Projectile,
    val context: SkillContext,
    val summonLocation: Location,
    val tick: ProjectileTick
) {
    private var arrowEntity: Arrow? = null

    private val parent: Caster.CompositeNode?
        get() = context[SkillContextKey.CASTER]

    fun summon(): Boolean {
        val shooter = parent?.value<Caster.Single.Entity>()?.bukkitEntity as? ProjectileSource

        arrowEntity = if (shooter != null) {
            shooter.launchProjectile(Arrow::class.java, null) {
                it.setGravity(projectile.gravity)
                it.velocity = it.velocity.normalize().multiply(projectile.initialVelocity)
                it.pierceLevel = projectile.penetration
            }
        } else {
            val world = summonLocation.world
            world.spawnArrow(summonLocation, summonLocation.direction, projectile.initialVelocity, 0.0f)
                .also {
                    it.setGravity(projectile.gravity)
                    it.velocity = summonLocation.direction.normalize().multiply(projectile.initialVelocity)
                    it.pierceLevel = projectile.penetration
                }
        }

        val newTickCaster = CasterAdapter.adapt(tick)
        context[SkillContextKey.CASTER] = newTickCaster.toComposite(parent)
        val startSkillTick = projectile.effects[Trigger.START]?.get()?.cast(context)

        if (startSkillTick != null) {
            Ticker.INSTANCE.addTick(startSkillTick)
        }

        val subscriptions = buildList {
            // 注册事件监听器
            with(arrowEntity!!) {
                addAll(registerTickEvent(this))
                addAll(registerHitEntityEvent(this))
                addAll(registerHitBlockEvent(this))
                addAll(registerDisappearEvent(this))
            }
        }

        EntitySubscriptionTerminator.newBuilder<EntityRemoveFromWorldEvent>()
            .terminatorEvent(EntityRemoveFromWorldEvent::class.java)
            .addSubscriptions(subscriptions)
            .predicate { it.entity == arrowEntity }
            .build()
            .startListen()

        return true
    }

    fun remove() {
        arrowEntity?.remove()
    }

    private fun registerTickEvent(arrow: Arrow): List<Subscription> {
        return listOf(
            Events.subscribe(ServerTickStartEvent::class.java)
                .handler {
                    val newContext = SkillContext(CasterAdapter.adapt(arrow).toComposite(parent), TargetAdapter.adapt(arrow.location))
                    val tickSkillTick = projectile.effects[Trigger.TICK]?.get()?.cast(newContext) ?: return@handler
                    Ticker.INSTANCE.addTick(tickSkillTick)
                },

            Events.subscribe(ServerTickStartEvent::class.java)
                .handler {
                    if (arrow.location.distance(summonLocation) > projectile.maximumDistance) {
                        val newContext = SkillContext(CasterAdapter.adapt(arrow).toComposite(parent), TargetAdapter.adapt(arrow.location))
                        val disappearSkillTick = projectile.effects[Trigger.DISAPPEAR]?.get()?.cast(newContext) ?: return@handler
                        Ticker.INSTANCE.addTick(disappearSkillTick)
                        arrow.remove()
                    }
                }
        )
    }

    private fun registerHitEntityEvent(arrow: Arrow): List<Subscription> {
        return listOf(Events.subscribe(ProjectileHitEvent::class.java)
            .handler {
                if (it.entity != arrow) return@handler
                val hitEntity = it.hitEntity ?: return@handler
                if (hitEntity is LivingEntity) {
                    val newContext = SkillContext(CasterAdapter.adapt(arrow).toComposite(parent), TargetAdapter.adapt(hitEntity))
                    val hitEntitySkillTick = projectile.effects[Trigger.HIT_ENTITY]?.get()?.cast(newContext) ?: return@handler
                    Ticker.INSTANCE.addTick(hitEntitySkillTick)
                }
            })
    }

    private fun registerHitBlockEvent(arrow: Arrow): List<Subscription> {
        return listOf(Events.subscribe(ProjectileHitEvent::class.java)
            .handler {
                if (it.entity != arrow) return@handler
                val hitBlock = it.hitBlock
                if (hitBlock != null) {
                    val newContext = SkillContext(CasterAdapter.adapt(arrow).toComposite(parent), TargetAdapter.adapt(hitBlock.location))
                    val hitBlockSkillTick = projectile.effects[Trigger.HIT_BLOCK]?.get()?.cast(newContext) ?: return@handler
                    Ticker.INSTANCE.addTick(hitBlockSkillTick)
                }
            })
    }

    private fun registerDisappearEvent(arrow: Arrow): List<Subscription> {
        return listOf(Events.subscribe(PlayerPickupArrowEvent::class.java)
            .handler {
                if (it.arrow != arrow) return@handler
                val newContext = SkillContext(CasterAdapter.adapt(arrow).toComposite(parent), TargetAdapter.adapt(it.player))
                val pickupSkillTick = projectile.effects[Trigger.PICK_UP]?.get()?.cast(newContext) ?: return@handler
                Ticker.INSTANCE.addTick(pickupSkillTick)
            })
    }
}

internal object ProjectileTriggerSerializer : ScalarSerializer<Trigger>(typeTokenOf()) {
    override fun deserialize(type: ReflectType, obj: Any): Trigger {
        return Trigger.valueOf(obj.toString().uppercase())
    }

    override fun serialize(item: Trigger, typeSupported: Predicate<Class<*>>): Any {
        throw UnsupportedOperationException()
    }
}
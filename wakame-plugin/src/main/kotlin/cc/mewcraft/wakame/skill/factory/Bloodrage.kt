package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttribute
import cc.mewcraft.wakame.item.components.cells.cores.skill.CoreSkill
import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey
import cc.mewcraft.wakame.skill.tick.AbstractSkillTick
import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.tick.TickResult
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.krequire
import me.lucko.helper.text3.mini
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.*

/**
 * 玩家血量低于一定值的时候触发效果.
 */
interface Bloodrage : Skill, PassiveSkill {
    /**
     * 用于添加属性的唯一标识.
     */
    val uniqueId: UUID

    /**
     * 激活的条件. (MoLang) 表达式
     */
    val condition: Evaluable<*>

    /**
     * 失效时间.
     */
    val invalidTime: Long

    /**
     * 重启技能的时间.
     */
    val restartTime: Long

    /**
     * 触发的效果.
     */
    val effects: List<BloodrageEffect>

    companion object Factory : SkillFactory<Bloodrage> {
        override fun create(key: Key, config: ConfigurationNode): Bloodrage {
            val uniqueId = config.node("uuid").krequire<UUID>()
            val condition = config.node("condition").get<Evaluable<*>>() ?: Evaluable.parseNumber(1.0)
            val invalidTime = config.node("invalid_time").krequire<Long>()
            val restartTime = config.node("restart_time").krequire<Long>()
            val effects = config.node("effects").krequire<List<BloodrageEffect>>()
            return DefaultImpl(key, config, uniqueId, condition, invalidTime, restartTime, effects)
        }
    }

    private class DefaultImpl(
        key: Key,
        config: ConfigurationNode,
        override val uniqueId: UUID,
        override val condition: Evaluable<*>,
        override val invalidTime: Long,
        override val restartTime: Long,
        override val effects: List<BloodrageEffect>
    ) : Bloodrage, SkillBase(key, config) {
        override fun cast(context: SkillContext): SkillTick<Bloodrage> {
            return BloodrageTick(context, this)
        }
    }
}

private class BloodrageTick(
    context: SkillContext,
    private val bloodrage: Bloodrage
) : AbstractSkillTick<Bloodrage>(bloodrage, context) {
    companion object {
        private val BLOODRAGE_EFFECT_TIME = SkillContextKey.create<Long>("bloodrage_effect_time")
        private val BLOODRAGE_LAST_END_TIME = SkillContextKey.create<Long>("bloodrage_last_end_time")
    }

    private var lastEndTime: Long?
        get() = context[BLOODRAGE_LAST_END_TIME]
        set(value) {
            context[BLOODRAGE_LAST_END_TIME] = value
        }

    private var effectTime: Long?
        get() = context[BLOODRAGE_EFFECT_TIME]
        set(value) {
            context[BLOODRAGE_EFFECT_TIME] = value
        }

    /**
     * 被动技能不存在结束时间.
     *
     * 它类似于服务段的核心逻辑一直 tick, 直到在移除的时候结束.
     */
    override fun tick(): TickResult {
        if (!checkConditions())
            return TickResult.ALL_DONE
        val engine = context.getOrThrow(SkillContextKey.MOCHA_ENGINE)
        val effects = skill.effects
        val currentTickCount = this.tickCount

        val isNotOverInvalidTime = effectTime?.let { currentTickCount - it < bloodrage.invalidTime } ?: true // 效果的持续时间没有超过失效时间
        val isOverRestartTime = lastEndTime?.let { currentTickCount - it >= bloodrage.restartTime } ?: true // 上次结束时间距离现在超过重启时间

        if (bloodrage.condition.evaluate(engine) > 0.0 && isNotOverInvalidTime && isOverRestartTime) {
            Bukkit.broadcast("Bloodrage start!".mini)
            if (effects.all { it.apply(skill, context) }) {
                // 效果生效后, 记录生效时间.
                effectTime = currentTickCount
            }
        } else {
            Bukkit.broadcast("Bloodrage end!".mini)
            effects.forEach { it.remove(skill, context) }

            // 记录上次结束时间. FIXME: 在效果真的结束的时候记录.
            lastEndTime = currentTickCount
        }

        return TickResult.CONTINUE_TICK
    }

    override fun whenRemove() {
        val effects = bloodrage.effects
        effects.forEach { it.remove(skill, context) }
    }
}

sealed interface BloodrageEffect {
    fun apply(bloodrage: Bloodrage, context: SkillContext): Boolean

    fun remove(bloodrage: Bloodrage, context: SkillContext)
}

private data class SkillBloodrageEffect(
    val effect: ConfiguredSkill,
) : BloodrageEffect {
    override fun apply(bloodrage: Bloodrage, context: SkillContext): Boolean {
        val player = CasterUtils.getCaster<Caster.Single.Player>(context)?.bukkitPlayer ?: return false
        val user = player.toUser()
        user.skillMap.addSkill(effect)
        return true
    }

    override fun remove(bloodrage: Bloodrage, context: SkillContext) {
        val player = CasterUtils.getCaster<Caster.Single.Player>(context)?.bukkitPlayer ?: return
        val user = player.toUser()
        user.skillMap.removeSkill(effect.key)
    }
}

private data class AttributeBloodrageEffect(
    val core: CoreAttribute,
) : BloodrageEffect {
    override fun apply(bloodrage: Bloodrage, context: SkillContext): Boolean {
        val player = CasterUtils.getCaster<Caster.Single.Player>(context)?.bukkitPlayer ?: return false
        val user = player.toUser()
        val effect = core.provideAttributeModifiers(bloodrage.uniqueId)
        val attributeMap = user.attributeMap
        for ((attribute, modifier) in effect) {
            if (attributeMap.hasModifier(attribute, modifier.id))
                return false
            attributeMap.getInstance(attribute)?.addModifier(modifier)
        }
        return true
    }

    override fun remove(bloodrage: Bloodrage, context: SkillContext) {
        val player = CasterUtils.getCaster<Caster.Single.Player>(context)?.bukkitPlayer ?: return
        val user = player.toUser()
        val effect = core.provideAttributeModifiers(bloodrage.uniqueId)
        effect.forEach { (attribute, modifier) -> user.attributeMap.getInstance(attribute)?.removeModifier(modifier) }
    }
}

/**
 * The serializer of bloodrage effect.
 *
 * ## Node structure
 *
 * ```yaml
 * <effect key>
 * <impl_defined>
 * ```
 */
internal object BloodrageEffectSerializer : SchemaSerializer<BloodrageEffect> {
    override fun deserialize(type: Type, node: ConfigurationNode): BloodrageEffect {
        val key = node.node("type").krequire<Key>()
        val namespace = key.namespace()
        return when (namespace) {
            Namespaces.SKILL -> {
                val skillCore = CoreSkill(node)
                SkillBloodrageEffect(ConfiguredSkill(skillCore))
            }

            Namespaces.ATTRIBUTE -> {
                val attributeCore = CoreAttribute(node)
                AttributeBloodrageEffect(attributeCore)
            }

            else -> {
                throw SerializationException(node, type, "Unknown bloodrage effect key '$key'")
            }
        }
    }
}
package cc.mewcraft.wakame.skill.context

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.molang.MoLangSupport
import cc.mewcraft.wakame.skill.Caster
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.user.toUser
import org.bukkit.inventory.ItemStack
import team.unnamed.mocha.MochaEngine

/**
 * 技能条件执行的上下文.
 */
sealed interface SkillCastContext {
    companion object {
        fun empty(): SkillCastContext = EmptySkillCastContext
    }

    fun <T : Any> set(key: SkillCastContextKey<T>, value: T)
    fun <T : Any> optional(key: SkillCastContextKey<T>): T?
    fun <T : Any> get(key: SkillCastContextKey<T>): T = optional(key) ?: throw IllegalArgumentException("No value for key: `$key`")
    fun <T : Any> has(key: SkillCastContextKey<T>): Boolean

    // FIXME: 2024.7.5 删除这些属性
    var caster: Caster?
    var target: Target?
    var nekoStack: NekoStack?
    var itemStack: ItemStack?
    var mochaEngine: MochaEngine<*>?
}

fun SkillCastContext(caster: Caster, target: Target? = null, nekoStack: NekoStack? = null): SkillCastContext {
    val context = SkillCastContextImpl()
    with(context) {
        // 技能必须有 caster
        this.caster = caster

        // 可有可无的 target
        if (target != null) {
            this.target = target
        }

        // 可有可无的 item
        if (nekoStack != null) {
            this.nekoStack = nekoStack
        }

        // 技能必须有 mocha engine
        this.mochaEngine = MoLangSupport.createEngine()
    }
    return context
}

/* Internals */

private data object EmptySkillCastContext : SkillCastContext {
    override fun <T : Any> set(key: SkillCastContextKey<T>, value: T) {}
    override fun <T : Any> optional(key: SkillCastContextKey<T>): T? {
        return null
    }
    override fun <T : Any> has(key: SkillCastContextKey<T>): Boolean {
        return false
    }

    override var caster: Caster? = null
    override var target: Target? = null
    override var nekoStack: NekoStack? = null
    override var itemStack: ItemStack? = null
    override var mochaEngine: MochaEngine<*>? = null
}

private class SkillCastContextImpl : SkillCastContext {
    private val storage: MutableMap<SkillCastContextKey<*>, Any> = HashMap()

    override fun <T : Any> set(key: SkillCastContextKey<T>, value: T) {
        storage[key] = value
    }

    override fun <T : Any> optional(key: SkillCastContextKey<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return storage[key] as T?
    }

    override fun <T : Any> has(key: SkillCastContextKey<T>): Boolean {
        return storage.containsKey(key)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SkillCastContextImpl

        return storage == other.storage
    }

    override fun hashCode(): Int {
        return storage.hashCode()
    }

    override var caster: Caster?
        get() = optional(SkillCastContextKey.CASTER)
        set(value) {
            value ?: return
            set(SkillCastContextKey.CASTER, value)
            when (value) {
                is Caster.Single.Player -> {
                    set(SkillCastContextKey.CASTER_PLAYER, value)
                    set(SkillCastContextKey.CASTER_ENTITY, value)
                    set(SkillCastContextKey.USER, value.bukkitPlayer.toUser())
                    set(SkillCastContextKey.CASTER_AUDIENCE, value.bukkitPlayer)
                }

                is Caster.Single.Entity -> {
                    set(SkillCastContextKey.CASTER_ENTITY, value)
                    set(SkillCastContextKey.CASTER_AUDIENCE, value.bukkitEntity)
                }

                is Caster.Single.Skill -> {
                    set(SkillCastContextKey.CASTER_SKILL, value)
                }

                is Caster.CompositeNode -> {
                    set(SkillCastContextKey.CASTER_COMPOSITE_NODE, value)
                }
            }
        }

    override var target: Target?
        get() = optional(SkillCastContextKey.TARGET)
        set(value) {
            value ?: return
            set(SkillCastContextKey.TARGET, value)
            when (value) {
                is Target.LivingEntity -> {
                    set(SkillCastContextKey.TARGET_LIVING_ENTITY, value)
                    set(SkillCastContextKey.TARGET_AUDIENCE, value.bukkitEntity)
                }

                is Target.Location -> {
                    set(SkillCastContextKey.TARGET_LOCATION, value)
                }

                is Target.Void -> {}
            }
        }

    override var nekoStack: NekoStack?
        get() = optional(SkillCastContextKey.NEKO_STACK)
        set(value) {
            value ?: return
            set(SkillCastContextKey.NEKO_STACK, value)
            set(SkillCastContextKey.ITEM_STACK, value.handle)
        }

    override var itemStack: ItemStack?
        get() = optional(SkillCastContextKey.ITEM_STACK)
        set(value) {
            value ?: return
            set(SkillCastContextKey.ITEM_STACK, value)
        }

    override var mochaEngine: MochaEngine<*>?
        get() = optional(SkillCastContextKey.MOCHA_ENGINE)
        set(value) {
            value ?: return
            optional(SkillCastContextKey.USER)?.let { value.bindInstance(UserContext::class.java, UserContext(it), "user", "player") }
            set(SkillCastContextKey.MOCHA_ENGINE, value)
        }

}
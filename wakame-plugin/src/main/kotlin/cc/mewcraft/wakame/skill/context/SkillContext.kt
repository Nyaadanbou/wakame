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
sealed interface SkillContext {
    companion object {
        fun empty(): SkillContext = EmptySkillContext
    }

    fun <T : Any> set(key: SkillContextKey<T>, value: T)
    fun <T : Any> optional(key: SkillContextKey<T>): T?
    fun <T : Any> get(key: SkillContextKey<T>): T = optional(key) ?: throw IllegalArgumentException("No value for key: `$key`")
    fun <T : Any> has(key: SkillContextKey<T>): Boolean

    // FIXME: 2024.7.5 删除这些属性
    var caster: Caster?
    var target: Target?
    var nekoStack: NekoStack?
    var itemStack: ItemStack?
    var mochaEngine: MochaEngine<*>?
}

fun SkillContext(caster: Caster, target: Target? = null, nekoStack: NekoStack? = null): SkillContext {
    val context = SkillContextImpl()
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

private data object EmptySkillContext : SkillContext {
    override fun <T : Any> set(key: SkillContextKey<T>, value: T) {}
    override fun <T : Any> optional(key: SkillContextKey<T>): T? {
        return null
    }
    override fun <T : Any> has(key: SkillContextKey<T>): Boolean {
        return false
    }

    override var caster: Caster? = null
    override var target: Target? = null
    override var nekoStack: NekoStack? = null
    override var itemStack: ItemStack? = null
    override var mochaEngine: MochaEngine<*>? = null
}

private class SkillContextImpl : SkillContext {
    private val storage: MutableMap<SkillContextKey<*>, Any> = HashMap()

    override fun <T : Any> set(key: SkillContextKey<T>, value: T) {
        storage[key] = value
    }

    override fun <T : Any> optional(key: SkillContextKey<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return storage[key] as T?
    }

    override fun <T : Any> has(key: SkillContextKey<T>): Boolean {
        return storage.containsKey(key)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SkillContextImpl

        return storage == other.storage
    }

    override fun hashCode(): Int {
        return storage.hashCode()
    }

    override var caster: Caster?
        get() = optional(SkillContextKey.CASTER)
        set(value) {
            value ?: return
            set(SkillContextKey.CASTER, value)
            when (value) {
                is Caster.Single.Player -> {
                    set(SkillContextKey.CASTER_PLAYER, value)
                    set(SkillContextKey.CASTER_ENTITY, value)
                    set(SkillContextKey.USER, value.bukkitPlayer.toUser())
                    set(SkillContextKey.CASTER_AUDIENCE, value.bukkitPlayer)
                }

                is Caster.Single.Entity -> {
                    set(SkillContextKey.CASTER_ENTITY, value)
                    set(SkillContextKey.CASTER_AUDIENCE, value.bukkitEntity)
                }

                is Caster.Single.Skill -> {
                    set(SkillContextKey.CASTER_SKILL, value)
                }

                is Caster.CompositeNode -> {
                    set(SkillContextKey.CASTER_COMPOSITE_NODE, value)
                }
            }
        }

    override var target: Target?
        get() = optional(SkillContextKey.TARGET)
        set(value) {
            value ?: return
            set(SkillContextKey.TARGET, value)
            when (value) {
                is Target.LivingEntity -> {
                    set(SkillContextKey.TARGET_LIVING_ENTITY, value)
                    set(SkillContextKey.TARGET_AUDIENCE, value.bukkitEntity)
                }

                is Target.Location -> {
                    set(SkillContextKey.TARGET_LOCATION, value)
                }

                is Target.Void -> {}
            }
        }

    override var nekoStack: NekoStack?
        get() = optional(SkillContextKey.NEKO_STACK)
        set(value) {
            value ?: return
            set(SkillContextKey.NEKO_STACK, value)
            set(SkillContextKey.ITEM_STACK, value.handle)
        }

    override var itemStack: ItemStack?
        get() = optional(SkillContextKey.ITEM_STACK)
        set(value) {
            value ?: return
            set(SkillContextKey.ITEM_STACK, value)
        }

    override var mochaEngine: MochaEngine<*>?
        get() = optional(SkillContextKey.MOCHA_ENGINE)
        set(value) {
            value ?: return
            optional(SkillContextKey.USER)?.let { value.bindInstance(UserContext::class.java, UserContext(it), "user", "player") }
            set(SkillContextKey.MOCHA_ENGINE, value)
        }

}
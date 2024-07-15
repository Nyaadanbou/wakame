package cc.mewcraft.wakame.skill.context

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.molang.MoLangSupport
import cc.mewcraft.wakame.skill.Caster
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.skill.toComposite
import cc.mewcraft.wakame.skill.value
import cc.mewcraft.wakame.user.toUser
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.inventory.ItemStack
import team.unnamed.mocha.MochaEngine

/**
 * 技能条件执行的上下文.
 *
 * ## 警告
 * 永远不要永久持有该对象!
 */
sealed interface SkillContext {
    companion object {
        fun empty(): SkillContext = EmptySkillContext
    }

    operator fun <T : Any> set(key: SkillContextKey<T>, value: T)
    operator fun <T : Any> get(key: SkillContextKey<T>): T?
    fun <T : Any> getOrThrow(key: SkillContextKey<T>): T = get(key) ?: throw IllegalArgumentException("Key '$key' not found in skill context")
    operator fun <T : Any> contains(key: SkillContextKey<T>): Boolean
}

fun SkillContext(caster: Caster.CompositeNode, target: Target? = null, nekoStack: NekoStack? = null): SkillContext {
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

fun SkillContext(caster: Caster.Single, target: Target? = null, nekoStack: NekoStack? = null): SkillContext {
    return SkillContext(caster.toComposite(), target, nekoStack)
}

/* Internals */

private data object EmptySkillContext : SkillContext {
    override fun <T : Any> set(key: SkillContextKey<T>, value: T) {}
    override fun <T : Any> get(key: SkillContextKey<T>): T? {
        return null
    }

    override fun <T : Any> contains(key: SkillContextKey<T>): Boolean {
        return false
    }
}

private class SkillContextImpl : SkillContext {
    private val storage: MutableMap<SkillContextKey<*>, Any> = Reference2ObjectOpenHashMap()

    override fun <T : Any> set(key: SkillContextKey<T>, value: T) {
        when (key) {
            SkillContextKey.CASTER -> {
                caster = value as Caster.CompositeNode
                return
            }

            SkillContextKey.TARGET -> {
                target = value as Target
                return
            }

            SkillContextKey.NEKO_STACK -> {
                nekoStack = value as NekoStack
                return
            }

            SkillContextKey.ITEM_STACK -> {
                itemStack = value as ItemStack
                return
            }

            SkillContextKey.MOCHA_ENGINE -> {
                mochaEngine = value as MochaEngine<*>
                return
            }
        }
        storage[key] = value
    }

    override fun <T : Any> get(key: SkillContextKey<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return storage[key] as T?
    }

    override fun <T : Any> contains(key: SkillContextKey<T>): Boolean {
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

    var caster: Caster.CompositeNode?
        get() = get(SkillContextKey.CASTER)
        set(value) {
            value ?: return
            storage[SkillContextKey.CASTER] = value
            val casterPlayer = caster?.value<Caster.Single.Player>() ?: return
            val bukkitPlayer = casterPlayer.bukkitPlayer
            if (bukkitPlayer?.isConnected == false)
                throw IllegalStateException("Player ${bukkitPlayer.name} is not connected")
            bukkitPlayer?.toUser()?.let { storage[SkillContextKey.USER] = it }
        }

    var target: Target?
        get() = get(SkillContextKey.TARGET)
        set(value) {
            value ?: return
            storage[SkillContextKey.TARGET] = value
        }

    var nekoStack: NekoStack?
        get() = get(SkillContextKey.NEKO_STACK)
        set(value) {
            value ?: return
            storage[SkillContextKey.NEKO_STACK] = value
        }

    var itemStack: ItemStack?
        get() = get(SkillContextKey.ITEM_STACK)
        set(value) {
            value ?: return
            storage[SkillContextKey.ITEM_STACK] = value
        }

    var mochaEngine: MochaEngine<*>?
        get() = get(SkillContextKey.MOCHA_ENGINE)
        set(value) {
            value ?: return
            get(SkillContextKey.USER)?.let { value.bindInstance(UserContext::class.java, UserContext(it), "user", "player") }
            storage[SkillContextKey.MOCHA_ENGINE] = value
        }

}
package cc.mewcraft.wakame.skill.context

import cc.mewcraft.wakame.item.binary.BukkitNekoStack
import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.binary.PlayNekoStack
import cc.mewcraft.wakame.skill.Caster
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.user.toUser
import org.bukkit.inventory.ItemStack

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
}

fun SkillCastContext(caster: Caster, target: Target? = null, nekoStack: PlayNekoStack? = null): SkillCastContext {
    val context = SkillCastContextImpl()
    with(context) {
        // 技能必须有 caster
        setCaster(caster)

        // 可有可无的 target
        if (target != null) {
            setTarget(target)
        }

        // 可有可无的 item
        if (nekoStack != null) {
            setNekoStack(nekoStack)
        }
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

    fun setCaster(caster: Caster) {
        set(SkillCastContextKey.CASTER, caster)
        when (caster) {
            is Caster.Player -> {
                set(SkillCastContextKey.CASTER_PLAYER, caster)
                set(SkillCastContextKey.CASTER_ENTITY, caster)
                set(SkillCastContextKey.USER, caster.bukkitPlayer.toUser())
            }

            is Caster.Entity -> {
                set(SkillCastContextKey.CASTER_ENTITY, caster)
            }
        }
    }

    fun setTarget(target: Target) {
        set(SkillCastContextKey.TARGET, target)
        when (target) {
            is Target.LivingEntity -> {
                set(SkillCastContextKey.TARGET_LIVING_ENTITY, target)
            }

            is Target.Location -> {
                set(SkillCastContextKey.TARGET_LOCATION, target)
            }

            is Target.Void -> {}
        }
    }

    fun setNekoStack(nekoStack: NekoStack) {
        if (nekoStack is BukkitNekoStack) {
            set(SkillCastContextKey.ITEM_STACK, nekoStack.itemStack)
        }
        set(SkillCastContextKey.NEKO_STACK, nekoStack)
    }

    fun setItemStack(itemStack: ItemStack) {
        set(SkillCastContextKey.ITEM_STACK, itemStack)
    }
}
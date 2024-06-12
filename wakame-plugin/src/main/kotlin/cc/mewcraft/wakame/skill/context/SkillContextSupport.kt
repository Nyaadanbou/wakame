package cc.mewcraft.wakame.skill.context

import cc.mewcraft.wakame.skill.Caster
import cc.mewcraft.wakame.skill.Target
import org.bukkit.inventory.ItemStack

object SkillCastContextBuilder {
    fun create(builder: SkillCastContext.() -> Unit): SkillCastContext {
        val context = SkillCastContextImpl()
        context.builder()
        return context
    }

    fun createPlayerSkillCastContext(caster: Caster.Player, target: Target, itemStack: ItemStack): SkillCastContext {
        return create {
            setCaster(caster)
            setTarget(target)
            setItemStack(itemStack)
        }
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
}
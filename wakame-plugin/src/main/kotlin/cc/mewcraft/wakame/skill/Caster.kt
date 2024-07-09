package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.user.User
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.Collections
import java.util.WeakHashMap
import org.bukkit.entity.Entity as BukkitEntity
import org.bukkit.entity.Player as BukkitPlayer

/**
 * 技能的施法者.
 */
sealed interface Caster {

    sealed interface Single : Caster {
        /**
         * 代表一个生物施法者, 不包括玩家.
         */
        interface Entity : Single {
            val bukkitEntity: BukkitEntity
        }

        /**
         * 代表一个玩家施法者.
         */
        interface Player : Entity {
            val bukkitPlayer: BukkitPlayer
        }

        /**
         * 代表一个技能施法者.
         */
        interface Skill : Single {
            val skillTick: SkillTick
        }
    }

    /**
     * 代表一个复合施法者, 由一个父施法者和多个子施法者组成.
     */
    interface CompositeNode : Caster {
        val parent: CompositeNode?

        val children: Set<CompositeNode>?

        fun addChild(child: CompositeNode)

        fun removeChild(child: CompositeNode)

        fun <T : Single> value(clazz: Class<T>): T?

        fun <T : Single> valueNonNull(clazz: Class<T>): T {
            return requireNotNull(value(clazz)) { "Value of type $clazz is null." }
        }

        fun <T : Single> root(clazz: Class<T>): T?

        fun <T : Single> rootNonNull(clazz: Class<T>): T {
            return requireNotNull(root(clazz)) { "Root value of type $clazz is null." }
        }
    }
}

fun Caster.Single.toComposite(parent: Caster.CompositeNode? = null): Caster.CompositeNode {
    return CasterAdapter.composite(this, parent)
}

inline fun <reified T : Caster.Single> Caster.CompositeNode.value(): T? {
    return value(T::class.java)
}

inline fun <reified T : Caster.Single> Caster.CompositeNode.valueNonNull(): T {
    return valueNonNull(T::class.java)
}

inline fun <reified T : Caster.Single> Caster.CompositeNode.root(): T? {
    return root(T::class.java)
}

inline fun <reified T : Caster.Single> Caster.CompositeNode.rootNonNull(): T {
    return rootNonNull(T::class.java)
}

object CasterAdapter {
    fun adapt(user: User<Player>): Caster.Single.Player {
        return PlayerCaster(user.player)
    }

    fun adapt(player: Player): Caster.Single.Player {
        return PlayerCaster(player)
    }

    fun adapt(entity: BukkitEntity): Caster.Single.Entity {
        return EntityCaster(entity)
    }

    fun adapt(skillTick: SkillTick): Caster.Single.Skill {
        return SkillCaster(skillTick)
    }

    fun composite(
        value: Caster,
        parent: Caster.CompositeNode? = null,
    ): Caster.CompositeNode {
        return when (value) {
            is Caster.Single -> CompositeNodeCaster(parent, value)
            is Caster.CompositeNode -> value
        }
    }
}

/* Implementations */

private data class PlayerCaster(
    override val bukkitPlayer: BukkitPlayer
) : Caster.Single.Player {
    override val bukkitEntity: Entity
        get() = bukkitPlayer
}

private data class EntityCaster(
    override val bukkitEntity: BukkitEntity
) : Caster.Single.Entity {
    init {
        require(bukkitEntity !is BukkitPlayer) { "EntityCaster should not be a player." }
    }
}

private data class SkillCaster(
    override val skillTick: SkillTick
) : Caster.Single.Skill

private class CompositeNodeCaster(
    override var parent: Caster.CompositeNode?,
    private val value: Caster.Single
) : Caster.CompositeNode {
    override var children: MutableSet<Caster.CompositeNode>? = null

    override fun addChild(child: Caster.CompositeNode) {
        if (children == null) {
            children = Collections.newSetFromMap(WeakHashMap(1))
        }
        if (child is CompositeNodeCaster) {
            child.parent = this
        }

        children!!.add(child)
    }

    override fun removeChild(child: Caster.CompositeNode) {
        if (child is CompositeNodeCaster) {
            child.parent = null
        }
        children?.remove(child)
    }

    override fun <T : Caster.Single> value(clazz: Class<T>): T? {
        return if (clazz.isInstance(value)) {
            clazz.cast(value)
        } else {
            null
        }
    }

    override fun <T : Caster.Single> root(clazz: Class<T>): T? {
        var current: Caster.CompositeNode = this
        while (current.parent != null) {
            current = current.parent as Caster.CompositeNode
        }
        return current.value(clazz)
    }
}
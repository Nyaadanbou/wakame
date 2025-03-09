package cc.mewcraft.wakame.ability.character

import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.adventure.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import java.util.*
import java.util.stream.Stream
import org.bukkit.entity.Entity as BukkitEntity
import org.bukkit.entity.Player as BukkitPlayer

/**
 * 技能的施法者. 一个施法者代表着一个复合结构, 包含了施法者本身, 以及施法者的父节点和子节点.
 */
sealed interface Caster {
    val uniqueId: UUID

    val entity: BukkitEntity

    val player: BukkitPlayer?
        get() = entity as? BukkitPlayer
}

object CasterAdapter {
    fun adapt(user: User<*>): Caster {
        return SimpleCaster(user.player())
    }

    fun adapt(player: Player): Caster {
        return SimpleCaster(player)
    }

    fun adapt(entity: BukkitEntity): Caster {
        if (entity is BukkitPlayer) {
            return adapt(entity)
        }
        return SimpleCaster(entity)
    }
}

/* Implementations */

@JvmInline
private value class SimpleCaster(
    override val entity: BukkitEntity,
) : Caster, Examinable {
    init {
        require(entity !is BukkitPlayer) { "EntityCaster should not be a player." }
    }

    override val uniqueId: UUID
        get() = entity.uniqueId

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("entity", entity)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}
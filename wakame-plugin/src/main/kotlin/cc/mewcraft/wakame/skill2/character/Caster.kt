package cc.mewcraft.wakame.skill2.character

import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import java.lang.ref.WeakReference
import java.util.UUID
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
    fun adapt(user: User<Player>): Caster {
        return PlayerCaster(user.player)
    }

    fun adapt(player: Player): Caster {
        return PlayerCaster(player)
    }

    fun adapt(entity: BukkitEntity): Caster {
        if (entity is BukkitPlayer) {
            return adapt(entity)
        }
        return EntityCaster(entity)
    }
}

/* Implementations */

private class PlayerCaster(
    bukkitPlayer: BukkitPlayer
) : Caster, Examinable {

    init {
        require(bukkitPlayer.isConnected) { "Player should be connected." }
    }

    private val weakBukkitPlayer: WeakReference<BukkitPlayer> = WeakReference(bukkitPlayer)

    override val uniqueId: UUID = bukkitPlayer.uniqueId

    override val entity: BukkitEntity
        get() = requireNotNull(weakBukkitPlayer.get()) { "Player should not be null." }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("entity", entity)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

private class EntityCaster(
    bukkitEntity: BukkitEntity
) : Caster, Examinable {
    init {
        require(bukkitEntity !is BukkitPlayer) { "EntityCaster should not be a player." }
    }

    private val weakBukkitEntity: WeakReference<BukkitEntity> = WeakReference(bukkitEntity)

    override val uniqueId: UUID = bukkitEntity.uniqueId

    override val entity: BukkitEntity
        get() = requireNotNull(weakBukkitEntity.get()) { "Entity should not be null." }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("entity", entity)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}
package cc.mewcraft.wakame.ability.character

import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.adventure.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.lang.ref.WeakReference
import java.util.*
import java.util.stream.Stream
import org.bukkit.Location as BukkitLocation
import org.bukkit.entity.LivingEntity as BukkitLivingEntity

sealed interface Target {
    val bukkitLocation: BukkitLocation
    val uniqueId: UUID?
    val bukkitEntity: BukkitLivingEntity?
}

object TargetAdapter {
    fun adapt(user: User<Player>): Target {
        return BukkitLivingEntityTarget(user.player())
    }

    fun adapt(entity: BukkitLivingEntity): Target {
        return BukkitLivingEntityTarget(entity)
    }

    fun adapt(location: BukkitLocation): Target {
        return BukkitLocationTarget(location)
    }

    fun adapt(caster: Caster): Target {
        val entity = caster.entity as? BukkitLivingEntity ?: throw IllegalArgumentException("Caster must be a living entity")
        return BukkitLivingEntityTarget(entity)
    }
}

/* Implementations */

@JvmInline
private value class BukkitLocationTarget(
    override val bukkitLocation: Location,
) : Target, Examinable {
    override val uniqueId: UUID?
        get() = null
    override val bukkitEntity: LivingEntity?
        get() = null

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("location", bukkitLocation)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

private class BukkitLivingEntityTarget(
    entity: BukkitLivingEntity
) : Target, Examinable {
    private val weakEntity: WeakReference<BukkitLivingEntity> = WeakReference(entity)
    override val uniqueId: UUID = entity.uniqueId
    override val bukkitEntity: BukkitLivingEntity
        get() = weakEntity.get() ?: error("LivingEntity $uniqueId not found")
    override val bukkitLocation: Location
        get() = bukkitEntity.location

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("entity", bukkitEntity)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BukkitLivingEntityTarget) return false

        if (uniqueId != other.uniqueId) return false

        return true
    }

    override fun hashCode(): Int {
        return uniqueId.hashCode()
    }
}
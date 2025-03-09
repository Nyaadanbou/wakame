package cc.mewcraft.wakame.ability.character

import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.util.stream.Stream
import org.bukkit.Location as BukkitLocation
import org.bukkit.entity.LivingEntity as BukkitLivingEntity

sealed interface Target {
    val bukkitLocation: BukkitLocation
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

@JvmInline
private value class BukkitLivingEntityTarget(
    override val bukkitEntity: LivingEntity,
) : Target, Examinable {
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
}
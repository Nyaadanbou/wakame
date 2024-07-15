@file:Suppress("UNCHECKED_CAST")

package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import java.lang.ref.WeakReference
import java.util.stream.Stream

class BukkitLocationTarget(
    override val bukkitLocation: Location
) : Target.Location, Examinable {
    override fun <T : Target> value(clazz: Class<T>): T? {
        return when (clazz) {
            Target.Location::class.java -> this as T
            else -> null
        }
    }

    override fun <T : Target> valueNonNull(clazz: Class<T>): T {
        return requireNotNull(value(clazz)) { "Target is not $clazz" }
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("location", bukkitLocation)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

class BukkitLivingEntityTarget(
    bukkitEntity: LivingEntity
) : Target.LivingEntity, Examinable {
    private val weakBukkitEntity: WeakReference<LivingEntity> = WeakReference(bukkitEntity)

    override val bukkitEntity: LivingEntity?
        get() = weakBukkitEntity.get()

    override fun <T : Target> value(clazz: Class<T>): T? {
        return when (clazz) {
            Target.LivingEntity::class.java -> this as T
            else -> null
        }
    }

    override fun <T : Target> valueNonNull(clazz: Class<T>): T {
        return requireNotNull(value(clazz)) { "Target is not $clazz" }
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("entity", bukkitEntity)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}
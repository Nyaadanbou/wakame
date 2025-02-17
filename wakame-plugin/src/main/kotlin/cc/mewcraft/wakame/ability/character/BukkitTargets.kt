@file:Suppress("UNCHECKED_CAST")

package cc.mewcraft.wakame.ability.character

import cc.mewcraft.wakame.util.adventure.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import java.lang.ref.WeakReference
import java.util.stream.Stream

class BukkitLocationTarget(
    override val bukkitLocation: Location,
) : Target.Location, Examinable {
    override val bukkitEntity: LivingEntity? = null

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
    bukkitEntity: LivingEntity,
) : Target.LivingEntity, Examinable {
    private val weakBukkitEntity: WeakReference<LivingEntity> = WeakReference(bukkitEntity)

    override val bukkitEntity: LivingEntity?
        get() = weakBukkitEntity.get()

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("entity", bukkitEntity)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}
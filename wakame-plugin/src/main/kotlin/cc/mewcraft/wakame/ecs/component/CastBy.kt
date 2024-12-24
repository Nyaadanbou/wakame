package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ability.character.Caster
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.entity.Entity as BukkitEntity

data class CastBy(
    var caster: Caster
) : Component<CastBy> {

    val entity: BukkitEntity?
        get() = caster.entity

    override fun type(): ComponentType<CastBy> = CastBy

    companion object : ComponentType<CastBy>()
}
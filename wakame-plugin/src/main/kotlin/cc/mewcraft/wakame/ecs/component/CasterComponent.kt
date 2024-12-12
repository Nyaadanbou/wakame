package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.skill2.character.Caster
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.entity.Entity as BukkitEntity

data class CasterComponent(
    var caster: Caster
) : Component<CasterComponent> {

    val entity: BukkitEntity?
        get() = caster.entity

    override fun type(): ComponentType<CasterComponent> = CasterComponent

    companion object : ComponentType<CasterComponent>()
}
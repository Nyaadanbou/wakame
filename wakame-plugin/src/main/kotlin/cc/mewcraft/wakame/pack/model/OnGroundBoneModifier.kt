package cc.mewcraft.wakame.pack.model

import org.bukkit.entity.Entity
import team.unnamed.creative.base.Vector3Float
import team.unnamed.hephaestus.bukkit.BoneView
import team.unnamed.hephaestus.bukkit.ModelView
import team.unnamed.hephaestus.view.modifier.BoneModifier
import java.lang.ref.WeakReference

class OnGroundBoneModifier(base: Entity) : BoneModifier {
    private val base: WeakReference<Entity> = WeakReference(base)

    override fun modifyPosition(original: Vector3Float): Vector3Float {
        val base = base.get() ?: return original // base removed?
        return original.y(original.y() - base.height.toFloat())
    }

    fun apply(view: ModelView) {
        view.bones().forEach { bone: BoneView -> bone.andThen(this) }
    }
}
package cc.mewcraft.wakame.pack.model

import org.bukkit.entity.Entity
import team.unnamed.creative.base.Vector3Float
import team.unnamed.hephaestus.bukkit.BoneView
import team.unnamed.hephaestus.bukkit.ModelView
import team.unnamed.hephaestus.modifier.BoneModifier
import java.lang.ref.WeakReference
import java.util.*

class OnGroundBoneModifier(base: Entity) : BoneModifier {
    private val base = WeakReference(Objects.requireNonNull(base, "base"))

    override fun modifyPosition(original: Vector3Float): Vector3Float {
        val base = base.get() ?: // base removed?
            return original
        return original.y(original.y() - base.height.toFloat())
    }

    fun apply(view: ModelView) {
        view.bones().forEach { bone: BoneView -> bone.modifying(this) }
    }
}
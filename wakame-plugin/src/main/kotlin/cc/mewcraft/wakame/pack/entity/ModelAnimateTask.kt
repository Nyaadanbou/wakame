package cc.mewcraft.wakame.pack.entity

import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.util.runTaskTimer
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import team.unnamed.hephaestus.animation.Animation
import team.unnamed.hephaestus.bukkit.BoneView
import team.unnamed.hephaestus.bukkit.ModelView
import team.unnamed.hephaestus.util.Quaternion
import team.unnamed.hephaestus.view.modifier.BoneModifier
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Init(
    stage = InitStage.POST_WORLD,
)
object ModelAnimateTask {
    private val viewsWithHeadRotationModifierAlreadyInjected: MutableSet<ModelView> = Collections.newSetFromMap(WeakHashMap())
    private val data: MutableMap<UUID, ModelViewData> = ConcurrentHashMap()

    @InitFun
    private fun init() {
        runTaskTimer(0, 1, ::run)
    }

    private fun run() {
        val now = System.currentTimeMillis()

        // reuse location instance
        val location = Location(null, 0.0, 0.0, 0.0)

        for (view in ModelRegistry.views()) {
            // Tried to tick a non-tracked view?
            val base = view.base() ?: continue

            base.getLocation(location)
            val data = data.computeIfAbsent(base.uniqueId) { _: UUID? ->
                ModelViewData(location.clone(), now)
            }

            if (now - data.lastCheckTimestamp >= 200L) {
                if (location.distanceSquared(data.lastTrackedLocation) >= 0.1) {
                    val flying = data.getAnimation("flying") ?: continue
                    // Moved!
                    view.animationPlayer().add(flying)
                    data.setAnimation(flying)
                }
            } else {
                // Didn't move
                val static = data.getAnimation("static") ?: continue
                view.animationPlayer().add(static)
                data.setAnimation(static)
            }
            data.lastTrackedLocation = location.clone()
            data.lastCheckTimestamp = now

            // tick the view
            if (base is LivingEntity) {
                // Use body yaw for living entities
                view.animationPlayer().tick(base.bodyYaw, 0f)

                // Rotate heads too
                if (viewsWithHeadRotationModifierAlreadyInjected.add(view)) {
                    view.bones()
                        .filter { bone: BoneView -> bone.bone().name().startsWith("head") }
                        .forEach { bone: BoneView ->
                            bone.andThen(object : BoneModifier {
                                override fun modifyRotation(original: Quaternion): Quaternion {
                                    return Quaternion.fromEulerRadians(0.0, Math.toRadians((base.bodyYaw - base.getYaw()).toDouble()), 0.0)
                                        .multiply(Quaternion.fromEulerRadians(-Math.toRadians(base.getPitch().toDouble()), 0.0, 0.0))
                                        .multiply(original)
                                }
                            })
                        }
                }
            } else {
                view.animationPlayer().tick(base.yaw, 0f)
            }
        }
    }

    private class ModelViewData(
        var lastTrackedLocation: Location,
        var lastCheckTimestamp: Long,
    ) {
        private var animation: Animation? = null

        fun setAnimation(animation: Animation?) {
            this.animation = animation
        }

        fun getAnimation(name: String): Animation? {
            return animation?.takeIf { it.name() == name }
        }
    }
}
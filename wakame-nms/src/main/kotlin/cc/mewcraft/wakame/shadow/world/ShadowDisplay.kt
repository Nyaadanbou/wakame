package cc.mewcraft.wakame.shadow.world

import me.lucko.shadow.*
import me.lucko.shadow.Target
import me.lucko.shadow.bukkit.BukkitShadowFactory
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.world.entity.Display

@ClassTarget(Display::class)
internal interface ShadowDisplay : Shadow {
    companion object {
        val DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID: EntityDataAccessor<Int?>
            get() {
                return BukkitShadowFactory.global().staticShadow<ShadowDisplay>().dataTransformationInterpolationStartDeltaTicksId()
            }

        val DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID: EntityDataAccessor<Int?>
            get() {
                return BukkitShadowFactory.global().staticShadow<ShadowDisplay>().dataTransformationInterpolationDurationId()
            }
    }

    @Static
    @Field
    @Target("DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID")
    fun dataTransformationInterpolationStartDeltaTicksId(): EntityDataAccessor<Int?>

    @Static
    @Field
    @Target("DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID")
    fun dataTransformationInterpolationDurationId(): EntityDataAccessor<Int?>
}
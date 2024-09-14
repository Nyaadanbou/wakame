package cc.mewcraft.wakame.shadow.world

import me.lucko.shadow.*
import me.lucko.shadow.Target
import me.lucko.shadow.bukkit.NmsClassTarget
import net.minecraft.network.syncher.EntityDataAccessor

@NmsClassTarget("world.entity.Display")
internal interface ShadowDisplay : Shadow {
    companion object {
        val DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID: EntityDataAccessor<Int?>
            get() {
                return ShadowFactory.global().staticShadow<ShadowDisplay>().dataTransformationInterpolationStartDeltaTicksId()
            }

        val DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID: EntityDataAccessor<Int?>
            get() {
                return ShadowFactory.global().staticShadow<ShadowDisplay>().dataTransformationInterpolationDurationId()
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
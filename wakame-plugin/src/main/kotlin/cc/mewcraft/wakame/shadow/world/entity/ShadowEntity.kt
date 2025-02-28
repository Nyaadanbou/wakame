package cc.mewcraft.wakame.shadow.world.entity

import me.lucko.shadow.*
import me.lucko.shadow.Target
import net.minecraft.network.chat.Component
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.world.entity.Entity
import java.util.*

@ClassTarget(Entity::class)
internal interface ShadowEntity : Shadow {
    // protected static final EntityDataAccessor<Byte> DATA_SHARED_FLAGS_ID
    @get:Field
    @get:Static
    @get:Target("DATA_SHARED_FLAGS_ID")
    val DATA_SHARED_FLAGS_ID: EntityDataAccessor<Byte>

    // private static final EntityDataAccessor<Optional<Component>> DATA_CUSTOM_NAME
    @get:Field
    @get:Static
    @get:Target("DATA_CUSTOM_NAME")
    val DATA_CUSTOM_NAME: EntityDataAccessor<Optional<Component>>

    // private static final EntityDataAccessor<Boolean> DATA_CUSTOM_NAME_VISIBLE
    @get:Field
    @get:Static
    @get:Target("DATA_CUSTOM_NAME_VISIBLE")
    val DATA_CUSTOM_NAME_VISIBLE: EntityDataAccessor<Boolean>
}
package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.EntityTags
import com.github.quillraven.fleks.entityTagOf

enum class EntityType : EntityTags by entityTagOf() {
    MECHANIC
}
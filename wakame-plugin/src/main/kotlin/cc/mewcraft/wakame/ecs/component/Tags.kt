package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.EntityTags
import com.github.quillraven.fleks.entityTagOf

enum class Tags : EntityTags by entityTagOf() {
    CAN_TICK
}
package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.EntityTags
import com.github.quillraven.fleks.entityTagOf

enum class EntityType : EntityTags by entityTagOf() {
    /**
     * 代表一个技能实体.
     */
    ABILITY,

    /**
     * 代表一个普通机制实体.
     */
    MECHANIC,

    /**
     * 代表一个元素特效实体.
     */
    ELEMENT_EFFECT,

    ;
}
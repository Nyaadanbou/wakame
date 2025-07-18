package cc.mewcraft.wakame.hook.impl.towny.component

import com.github.quillraven.fleks.EntityTags
import com.github.quillraven.fleks.entityTagOf

enum class TownEnhancementType : EntityTags by entityTagOf() {
    BUFF_FURNACE,
}
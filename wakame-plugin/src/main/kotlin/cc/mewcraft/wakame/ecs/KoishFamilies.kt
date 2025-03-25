package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.ability2.component.TargetTo
import cc.mewcraft.wakame.element.component.ElementComponent
import cc.mewcraft.wakame.element.component.ElementStackComponent
import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.World.Companion.family

object KoishFamilies {

    @JvmField
    val ELEMENT_STACK: Family = family { all(ElementComponent, ElementStackComponent, TargetTo) }

    // 用于初始化本 object 里的 val
    fun bootstrap() = Unit
}